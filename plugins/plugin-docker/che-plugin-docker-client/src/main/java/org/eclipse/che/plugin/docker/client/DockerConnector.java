/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.docker.client;

import com.google.common.io.CharStreams;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.che.api.core.util.FileCleaner;
import org.eclipse.che.api.core.util.ValueHolder;
import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.commons.json.JsonNameConvention;
import org.eclipse.che.commons.json.JsonParseException;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.commons.lang.TarUtils;
import org.eclipse.che.commons.lang.ws.rs.ExtMediaType;
import org.eclipse.che.plugin.docker.client.connection.CloseConnectionInputStream;
import org.eclipse.che.plugin.docker.client.connection.DockerConnection;
import org.eclipse.che.plugin.docker.client.connection.DockerConnectionFactory;
import org.eclipse.che.plugin.docker.client.connection.DockerResponse;
import org.eclipse.che.plugin.docker.client.dto.AuthConfigs;
import org.eclipse.che.plugin.docker.client.json.ContainerCommitted;
import org.eclipse.che.plugin.docker.client.json.ContainerConfig;
import org.eclipse.che.plugin.docker.client.json.ContainerCreated;
import org.eclipse.che.plugin.docker.client.json.ContainerExitStatus;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.eclipse.che.plugin.docker.client.json.ContainerProcesses;
import org.eclipse.che.plugin.docker.client.json.ContainerResource;
import org.eclipse.che.plugin.docker.client.json.Event;
import org.eclipse.che.plugin.docker.client.json.ExecConfig;
import org.eclipse.che.plugin.docker.client.json.ExecCreated;
import org.eclipse.che.plugin.docker.client.json.ExecInfo;
import org.eclipse.che.plugin.docker.client.json.ExecStart;
import org.eclipse.che.plugin.docker.client.json.Filters;
import org.eclipse.che.plugin.docker.client.json.HostConfig;
import org.eclipse.che.plugin.docker.client.json.Image;
import org.eclipse.che.plugin.docker.client.json.ImageInfo;
import org.eclipse.che.plugin.docker.client.json.ProgressStatus;
import org.eclipse.che.plugin.docker.client.json.Version;
import org.eclipse.che.plugin.docker.client.params.AttachContainerParams;
import org.eclipse.che.plugin.docker.client.params.BuildImageParams;
import org.eclipse.che.plugin.docker.client.params.CommitParams;
import org.eclipse.che.plugin.docker.client.params.CreateContainerParams;
import org.eclipse.che.plugin.docker.client.params.CreateExecParams;
import org.eclipse.che.plugin.docker.client.params.GetEventsParams;
import org.eclipse.che.plugin.docker.client.params.GetExecInfoParams;
import org.eclipse.che.plugin.docker.client.params.GetResourceParams;
import org.eclipse.che.plugin.docker.client.params.InspectContainerParams;
import org.eclipse.che.plugin.docker.client.params.InspectImageParams;
import org.eclipse.che.plugin.docker.client.params.KillContainerParams;
import org.eclipse.che.plugin.docker.client.params.PullParams;
import org.eclipse.che.plugin.docker.client.params.PushParams;
import org.eclipse.che.plugin.docker.client.params.PutResourceParams;
import org.eclipse.che.plugin.docker.client.params.RemoveContainerParams;
import org.eclipse.che.plugin.docker.client.params.RemoveImageParams;
import org.eclipse.che.plugin.docker.client.params.StartContainerParams;
import org.eclipse.che.plugin.docker.client.params.StartExecParams;
import org.eclipse.che.plugin.docker.client.params.StopContainerParams;
import org.eclipse.che.plugin.docker.client.params.TagParams;
import org.eclipse.che.plugin.docker.client.params.TopParams;
import org.eclipse.che.plugin.docker.client.params.WaitContainerParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.net.UrlEscapers.urlPathSegmentEscaper;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NOT_MODIFIED;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;

/**
 * Client for docker API.
 *
 * @author andrew00x
 * @author Alexander Garagatyi
 * @author Anton Korneta
 * @author Mykola Morhun
 */
@Singleton
public class DockerConnector {
    private static final Logger LOG = LoggerFactory.getLogger(DockerConnector.class);

    private final URI                     dockerDaemonUri;
    private final InitialAuthConfig       initialAuthConfig;
    private final ExecutorService         executor;
    private final DockerConnectionFactory connectionFactory;

    @Inject
    public DockerConnector(DockerConnectorConfiguration connectorConfiguration, 
                           DockerConnectionFactory connectionFactory) {
        this.dockerDaemonUri = connectorConfiguration.getDockerDaemonUri();
        this.initialAuthConfig = connectorConfiguration.getAuthConfigs();
        this.connectionFactory = connectionFactory;
        executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
                                                         .setNameFormat("DockerApiConnector-%d")
                                                         .setDaemon(true)
                                                         .build());
    }

    /**
     * Gets system-wide information.
     *
     * @return system-wide information
     * @throws IOException
     *         when problems occurs with docker api calls
     */
    public org.eclipse.che.plugin.docker.client.json.SystemInfo getSystemInfo() throws IOException {
        return doGetSystemInfo(dockerDaemonUri);
    }

    /**
     * The same as {@link #getSystemInfo()} but with additional parameter
     *
     * @param dockerDaemonUri
     *         docker service URI
     */
    protected org.eclipse.che.plugin.docker.client.json.SystemInfo doGetSystemInfo(final URI dockerDaemonUri) throws IOException {
        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("GET")
                                                            .path("/info")) {
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (OK.getStatusCode() != status) {
                throw getDockerException(response);
            }
            return parseResponseStreamAndClose(response.getInputStream(), org.eclipse.che.plugin.docker.client.json.SystemInfo.class);
        } catch (JsonParseException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Gets docker version.
     *
     * @return information about version docker
     * @throws IOException
     *         when problems occurs with docker api calls
     */
    public Version getVersion() throws IOException {
        return doGetVersion(dockerDaemonUri);
    }

    /**
     * The same as {@link #getVersion()} but with additional parameter
     *
     * @param dockerDaemonUri
     *         docker service URI
     */
    protected Version doGetVersion(final URI dockerDaemonUri) throws IOException {
        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("GET")
                                                            .path("/version")) {
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (OK.getStatusCode() != status) {
                throw getDockerException(response);
            }
            return parseResponseStreamAndClose(response.getInputStream(), Version.class);
        } catch (JsonParseException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Lists docker images.
     *
     * @return list of docker images
     * @throws IOException
     *         when problems occurs with docker api calls
     */
    public Image[] listImages() throws IOException {
        return doListImages(dockerDaemonUri);
    }

    /**
     * The same as {@link #listImages()} but with additional parameter
     *
     * @param dockerDaemonUri
     *         docker service URI
     */
    protected Image[] doListImages(final URI dockerDaemonUri) throws IOException {
        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("GET")
                                                            .path("/images/json")) {
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (OK.getStatusCode() != status) {
                throw getDockerException(response);
            }
            return parseResponseStreamAndClose(response.getInputStream(), Image[].class);
        } catch (JsonParseException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Builds new docker image from specified dockerfile.
     *
     * @param repository
     *         full repository name to be applied to newly created image
     * @param progressMonitor
     *         ProgressMonitor for images creation process
     * @param authConfigs
     *         Authentication configuration for private registries. Can be null.
     * @param memoryLimit
     *         Memory limit for build in bytes
     * @param memorySwapLimit
     *         Total memory in bytes (memory + swap), -1 to enable unlimited swap
     * @param files
     *         files that are needed for creation docker images (e.g. file of directories used in ADD instruction in Dockerfile), one of
     *         them must be Dockerfile.
     * @return image id
     * @throws IOException
     * @throws InterruptedException
     *         if build process was interrupted
     */
    @Deprecated
    public String buildImage(String repository,
                             ProgressMonitor progressMonitor,
                             AuthConfigs authConfigs,
                             boolean doForcePull,
                             long memoryLimit,
                             long memorySwapLimit,
                             File... files) throws IOException, InterruptedException {
            return doBuildImage(new BuildImageParams().withRepository(repository)
                                                      .withAuthConfigs(authConfigs)
                                                      .withDoForcePull(doForcePull)
                                                      .withMemoryLimit(memoryLimit)
                                                      .withMemorySwapLimit(memorySwapLimit)
                                                      .withFiles(files),
                                progressMonitor,
                                dockerDaemonUri);
    }

    /**
     * Gets detailed information about docker image.
     *
     * @param params
     *         parameters holder
     * @return detailed information about {@code image}
     * @throws IOException
     *         when problems occurs with docker api calls
     */
    public ImageInfo inspectImage(InspectImageParams params) throws IOException {
        return doInspectImage(params, dockerDaemonUri);
    }

    /**
     * The same as {@link #inspectImage(InspectImageParams)} but with additional parameter
     *
     * @param dockerDaemonUri
     *         docker service URI
     */
    protected ImageInfo doInspectImage(final InspectImageParams params, final URI dockerDaemonUri) throws IOException {
        final String image = requiredNonNull(params.getImage(), "Inspect image: image parameter is null");

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("GET")
                                                            .path("/images/" + image + "/json")) {
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (OK.getStatusCode() != status) {
                throw getDockerException(response);
            }
            return parseResponseStreamAndClose(response.getInputStream(), ImageInfo.class);
        } catch (JsonParseException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Stops docker container.
     *
     * @param params
     *         parameters holder
     * @throws IOException
     *         when problems occurs with docker api calls
     */
    public void stopContainer(final StopContainerParams params) throws IOException {
        doStopContainer(params, dockerDaemonUri);
    }

    /**
     * Stops container.
     *
     * @param container
     *         container identifier, either id or name
     * @param timeout
     *         time to wait for the container to stop before killing it
     * @param timeunit
     *         time unit of the timeout parameter
     * @throws IOException
     */
    @Deprecated
    public void stopContainer(String container, long timeout, TimeUnit timeunit) throws IOException {
        doStopContainer(new StopContainerParams().withContainer(container)
                                                 .withTimeout(timeout, timeunit),
                        dockerDaemonUri);
    }

    /**
     * The same as {@link #stopContainer(StopContainerParams)} but with additional parameter
     *
     * @param dockerDaemonUri
     *         docker service URI
     */
    protected void doStopContainer(final StopContainerParams params, final URI dockerDaemonUri) throws IOException {
        final String container = requiredNonNull(params.getContainer(), "Stop container: container identifier is null");
        final Long timeout = params.getTimeunit() == null ? params.getTimeout() : params.getTimeunit().toSeconds(params.getTimeout());

        final List<Pair<String, ?>> headers = new ArrayList<>(2);
        headers.add(Pair.of("Content-Type", MediaType.TEXT_PLAIN));
        headers.add(Pair.of("Content-Length", 0));
        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/containers/" + container + "/stop")
                                                            .headers(headers)) {
            addQueryParamIfSet(connection, "t", timeout);
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (!(NO_CONTENT.getStatusCode() == status || NOT_MODIFIED.getStatusCode() == status)) {
                throw getDockerException(response);
            }
        }
    }

    /**
     * Sends specified signal to running container.
     * If signal not set, then SIGKILL will be used.
     *
     * @param params
     *         parameters holder
     * @throws IOException
     *         when problems occurs with docker api calls
     */
    public void killContainer(final KillContainerParams params) throws IOException {
        doKillContainer(params, dockerDaemonUri);
    }

    /**
     * The same as {@link #killContainer(KillContainerParams)} but with additional parameter
     *
     * @param dockerDaemonUri
     *         docker service URI
     */
    protected void doKillContainer(final KillContainerParams params, final URI dockerDaemonUri) throws IOException {
        final String container = requiredNonNull(params.getContainer(), "Send signal to container: container identifier is null");
        final Integer signal = params.getSignal();

        final List<Pair<String, ?>> headers = new ArrayList<>(2);
        headers.add(Pair.of("Content-Type", MediaType.TEXT_PLAIN));
        headers.add(Pair.of("Content-Length", 0));

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/containers/" + container + "/kill")
                                                            .headers(headers)) {
            addQueryParamIfSet(connection, "signal", signal);
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (NO_CONTENT.getStatusCode() != status) {
                throw getDockerException(response);
            }
        }
    }

    /**
     * Kills container with SIGKILL signal.
     *
     * @param container
     *         container identifier, either id or name
     * @throws IOException
     */
    @Deprecated
    public void killContainer(String container) throws IOException {
        requiredNonNull(container, "Kill container: container identifier is null");
        doKillContainer(new KillContainerParams().withContainer(container)
                                                 .withSignal(9),
                        dockerDaemonUri);
    }

    /**
     * Removes docker container.
     *
     * @param params
     *         parameters holder
     * @throws IOException
     *         when problems occurs with docker api calls
     */
    public void removeContainer(final RemoveContainerParams params) throws IOException {
        doRemoveContainer(params, dockerDaemonUri);
    }

    /**
     * Removes container.
     *
     * @param container
     *         container identifier, either id or name
     * @param force
     *         if {@code true} kills the running container then remove it
     * @param removeVolumes
     *         if {@code true} removes volumes associated to the container
     * @throws IOException
     *         when problems occurs with docker api calls
     */
    @Deprecated
    public void removeContainer(String container, boolean force, boolean removeVolumes) throws IOException {
        doRemoveContainer(new RemoveContainerParams().withContainer(container)
                                                     .withForce(force)
                                                     .withRemoveVolumes(removeVolumes),
                          dockerDaemonUri);
    }

    /**
     * The same as {@link #removeContainer(RemoveContainerParams)} but with additional parameter
     *
     * @param dockerDaemonUri
     *         docker service URI
     */
    protected void doRemoveContainer(RemoveContainerParams params, final URI dockerDaemonUri) throws IOException {
        final String container = requiredNonNull(params.getContainer(), "Remove container: container identifier is null");
        final Boolean force = params.isForce();
        final Boolean removeVolumes = params.isRemoveVolumes();

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("DELETE")
                                                            .path("/containers/" + container)) {
            addQueryParamIfSet(connection, "force", force);
            addQueryParamIfSet(connection, "v", removeVolumes);
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (NO_CONTENT.getStatusCode() != status) {
                throw getDockerException(response);
            }
        }
    }

    /**
     * Blocks until container stops, then returns the exit code
     *
     * @param params
     *         parameters holder
     * @return exit code
     * @throws IOException
     *         when problems occurs with docker api calls
     */
    public int waitContainer(final WaitContainerParams params) throws IOException {
        return doWaitContainer(params, dockerDaemonUri);
    }

    /**
     * Blocks until {@code container} stops, then returns the exit code.
     *
     * @param container
     *         container identifier, either id or name
     * @return exit code
     * @throws IOException
     *         when problems occurs with docker api calls
     */
    @Deprecated
    public int waitContainer(String container) throws IOException {
        return doWaitContainer(new WaitContainerParams().withContainer(container), dockerDaemonUri);
    }

    /**
     * The same as {@link #waitContainer(WaitContainerParams)} but with additional parameter
     *
     * @param dockerDaemonUri
     *         docker service URI
     */
    protected int doWaitContainer(WaitContainerParams params, final URI dockerDaemonUri) throws IOException {
        final String container = requiredNonNull(params.getContainer(), "Wait container: container identifier is null");

        final List<Pair<String, ?>> headers = new ArrayList<>(2);
        headers.add(Pair.of("Content-Type", MediaType.TEXT_PLAIN));
        headers.add(Pair.of("Content-Length", 0));

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/containers/" + container + "/wait")
                                                            .headers(headers)) {
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (OK.getStatusCode() != status) {
                throw getDockerException(response);
            }
            return parseResponseStreamAndClose(response.getInputStream(), ContainerExitStatus.class).getStatusCode();
        } catch (JsonParseException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Gets detailed information about docker container.
     *
     * @param container
     *         id of container
     * @return detailed information about {@code container}
     * @throws IOException
     */
    @Deprecated
    public ContainerInfo inspectContainer(String container) throws IOException {
        return doInspectContainer(new InspectContainerParams().withContainer(container), dockerDaemonUri);
    }

    /**
     * Gets detailed information about docker container.
     *
     * @param params
     *         parameters holder
     * @return detailed information about {@code container}
     * @throws IOException
     *         when problems occurs with docker api calls
     */
    public ContainerInfo inspectContainer(final InspectContainerParams params) throws IOException {
        return doInspectContainer(params, dockerDaemonUri);
    }

    /**
     * The same as {@link #inspectContainer(InspectContainerParams)} but with additional parameter
     *
     * @param dockerDaemonUri
     *         docker service URI
     */
    protected ContainerInfo doInspectContainer(InspectContainerParams params, URI dockerDaemonUri) throws IOException {
        final String container = requiredNonNull(params.getContainer(), "Inspect container: container id is null");
        final Boolean size = params.isGetContainerSize();

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("GET")
                                                            .path("/containers/" + container + "/json")) {
            addQueryParamIfSet(connection, "size", size);
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (OK.getStatusCode() != status) {
                throw getDockerException(response);
            }
            return parseResponseStreamAndClose(response.getInputStream(), ContainerInfo.class);
        } catch (JsonParseException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Attaches to the container with specified id.
     *
     * @param params
     *         parameters holder
     *         if @{code stream} parameter is {@code true} then get 'live' stream from container.
     *         Typically need to run this method in separate thread, if {@code
     *         stream} is {@code true} since this method blocks until container is running.
     * @param containerLogsProcessor
     *         output for container logs
     * @throws IOException
     *         when problems occurs with docker api calls
     */
    public void attachContainer(final AttachContainerParams params, MessageProcessor<LogMessage> containerLogsProcessor)
            throws IOException {
        doAttachContainer(params, containerLogsProcessor, dockerDaemonUri);
    }

    /**
     * The same as {@link #attachContainer(AttachContainerParams, MessageProcessor)} but with additional parameter
     *
     * @param dockerDaemonUri
     *         docker service URI
     */
    protected void doAttachContainer(final AttachContainerParams params,
                                     final MessageProcessor<LogMessage> containerLogsProcessor,
                                     final URI dockerDaemonUri) throws IOException {
        final String container = requiredNonNull(params.getContainer(), "Attach container: container id is null");
        final Boolean stream = params.isStream();

        final List<Pair<String, ?>> headers = new ArrayList<>(2);
        headers.add(Pair.of("Content-Type", MediaType.TEXT_PLAIN));
        headers.add(Pair.of("Content-Length", 0));

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/containers/" + container + "/attach")
                                                            .query("stdout", 1)
                                                            .query("stderr", 1)
                                                            .headers(headers)) {
            addQueryParamIfSet(connection, "stream", stream);
            addQueryParamIfSet(connection, "logs", stream);
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (OK.getStatusCode() != status) {
                throw getDockerException(response);
            }
            try (InputStream responseStream = response.getInputStream()) {
                new LogMessagePumper(responseStream, containerLogsProcessor).start();
            }
        }
    }

    /**
     * Attaches to the container with specified id.
     *
     * @param container
     *         id of container
     * @param containerLogsProcessor
     *         output for container logs
     * @param stream
     *         if {@code true} then get 'live' stream from container. Typically need to run this method in separate thread, if {@code
     *         stream} is {@code true} since this method blocks until container is running.
     * @throws IOException
     */
    @Deprecated
    public void attachContainer(String container, MessageProcessor<LogMessage> containerLogsProcessor, boolean stream) throws IOException {
        doAttachContainer(new AttachContainerParams().withContainer(container)
                                                     .withStream(stream),
                          containerLogsProcessor,
                          dockerDaemonUri);
    }

    /**
     * Copies file or directory {@code path} from {@code container} to the {code hostPath}.
     *
     * @param container
     *         container id
     * @param path
     *         path to file or directory inside container
     * @param hostPath
     *         path to the directory on host filesystem
     * @throws IOException
     * @deprecated since 1.20 docker api in favor of the {@link #getResource(String, String)}
     * and {@link #putResource(String, String, InputStream, boolean) putResource}
     */
    @Deprecated
    public void copy(String container, String path, File hostPath) throws IOException {
        final String entity = JsonHelper.toJson(new ContainerResource().withResource(path), FIRST_LETTER_LOWERCASE);
        final List<Pair<String, ?>> headers = new ArrayList<>(2);
        headers.add(Pair.of("Content-Type", MediaType.APPLICATION_JSON));
        headers.add(Pair.of("Content-Length", entity.getBytes().length));

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path(String.format("/containers/%s/copy", container))
                                                            .headers(headers)
                                                            .entity(entity)) {
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (OK.getStatusCode() != status) {
                throw getDockerException(response);
            }
            // TarUtils uses apache commons compress library for working with tar archive and it fails
            // (e.g. doesn't unpack all files from archive in case of coping directory) when we try to use stream from docker remote API.
            // Docker sends tar contents as sequence of chunks and seems that causes problems for apache compress library.
            // The simplest solution is spool content to temporary file and then unpack it to destination folder.
            final Path spoolFilePath = Files.createTempFile("docker-copy-spool-", ".tar");
            try (InputStream is = response.getInputStream()) {
                Files.copy(is, spoolFilePath, StandardCopyOption.REPLACE_EXISTING);
                try (InputStream tarStream = Files.newInputStream(spoolFilePath)) {
                    TarUtils.untar(tarStream, hostPath);
                }
            } finally {
                FileCleaner.addFile(spoolFilePath.toFile());
            }
        }
    }

    /**
     * Sets up an exec instance in a running container.
     *
     * @param params
     *         parameters holder
     * @return just created exec info
     * @throws IOException
     *         when problems occurs with docker api calls
     */
    public Exec createExec(final CreateExecParams params) throws IOException {
        return doCreateExec(params, dockerDaemonUri);
    }

    @Deprecated
    public Exec createExec(String container, boolean detach, String... cmd) throws IOException {
        return doCreateExec(new CreateExecParams().withContainer(container)
                                                  .withDetach(detach)
                                                  .withCmd(cmd),
                            dockerDaemonUri);
    }

    /**
     * The same as {@link #createContainer(CreateContainerParams)} but with additional parameter
     *
     * @param dockerDaemonUri
     *         docker service URI
     */
    protected Exec doCreateExec(final CreateExecParams params, final URI dockerDaemonUri) throws IOException {
        final String container = requiredNonNull(params.getContainer(), "Create exec: container id is null");
        final Boolean detach = firstNonNull(params.isDetach(), false);
        final String[] cmd = requiredNonNull(params.getCmd(), "Create exec: command is not set");

        final ExecConfig execConfig = new ExecConfig().withCmd(cmd);
        if (!detach) {
            execConfig.withAttachStderr(true).withAttachStdout(true);
        }
        final List<Pair<String, ?>> headers = new ArrayList<>(2);
        final String entity = JsonHelper.toJson(execConfig, FIRST_LETTER_LOWERCASE);
        headers.add(Pair.of("Content-Type", MediaType.APPLICATION_JSON));
        headers.add(Pair.of("Content-Length", entity.getBytes().length));

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/containers/" + container + "/exec")
                                                            .headers(headers)
                                                            .entity(entity)) {
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (status / 100 != 2) {
                throw getDockerException(response);
            }
            return new Exec(cmd, parseResponseStreamAndClose(response.getInputStream(), ExecCreated.class).getId());
        } catch (JsonParseException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Starts a previously set up exec instance.
     *
     * @param params
     *         parameters holder
     * @param execOutputProcessor
     *         processor for exec output
     * @throws IOException
     *         when problems occurs with docker api calls
     */
    public void startExec(final StartExecParams params, MessageProcessor<LogMessage> execOutputProcessor) throws IOException {
        doStartExec(params, execOutputProcessor, dockerDaemonUri);
    }

    @Deprecated
    public void startExec(String execId, MessageProcessor<LogMessage> execOutputProcessor) throws IOException {
        doStartExec(new StartExecParams().withExecId(execId), execOutputProcessor, dockerDaemonUri);
    }

    /**
     * The same as {@link #startExec(StartExecParams, MessageProcessor)} but with additional parameter
     *
     * @param dockerDaemonUri
     *         docker service URI
     */
    protected void doStartExec(StartExecParams params,
                               final MessageProcessor<LogMessage> execOutputProcessor,
                               final URI dockerDaemonUri) throws IOException {
        final String execId = requiredNonNull(params.getExecId(), "Start exec: exec id is null");
        final Boolean detach = params.isDetach();
        final Boolean tty = params.isTty();

        final ExecStart execStart = new ExecStart().withDetach(execOutputProcessor == null);
        if (detach != null) {
            execStart.withDetach(detach);
        }
        if (tty != null) {
            execStart.withTty(tty);
        }
        final String entity = JsonHelper.toJson(execStart, FIRST_LETTER_LOWERCASE);
        final List<Pair<String, ?>> headers = new ArrayList<>(2);
        headers.add(Pair.of("Content-Type", MediaType.APPLICATION_JSON));
        headers.add(Pair.of("Content-Length", entity.getBytes().length));

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/exec/" + execId + "/start")
                                                            .headers(headers)
                                                            .entity(entity)) {

            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            // According to last doc (https://docs.docker.com/reference/api/docker_remote_api_v1.15/#exec-start) status must be 201 but
            // in fact docker API returns 200 or 204 status.
            if (status / 100 != 2) {
                throw getDockerException(response);
            }
            if (status != NO_CONTENT.getStatusCode() && execOutputProcessor != null) {
                try (InputStream responseStream = response.getInputStream()) {
                    new LogMessagePumper(responseStream, execOutputProcessor).start();
                }
            }
        }
    }

    /**
     * Gets detailed information about exec
     *
     * @param params
     *         parameters holder
     * @return detailed information about {@code execId}
     * @throws IOException
     *         when problems occurs with docker api calls
     */
    public ExecInfo getExecInfo(final GetExecInfoParams params) throws IOException {
        return doGetExecInfo(params, dockerDaemonUri);
    }

    /**
     * Gets detailed information about exec
     *
     * @return detailed information about {@code execId}
     * @throws IOException
     */
    @Deprecated
    public ExecInfo getExecInfo(String execId) throws IOException {
        return doGetExecInfo(new GetExecInfoParams().withExecId(execId), dockerDaemonUri);
    }

    /**
     * The same as {@link #getExecInfo(GetExecInfoParams)} but with additional parameter
     *
     * @param dockerDaemonUri
     *         docker service URI
     */
    protected ExecInfo doGetExecInfo(final GetExecInfoParams params, final URI dockerDaemonUri) throws IOException {
        final String execId = requiredNonNull(params.getExecId(), "Get exec info: exec id is null");

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("GET")
                                                            .path("/exec/" + execId + "/json")) {
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (OK.getStatusCode() != status) {
                throw getDockerException(response);
            }
            return parseResponseStreamAndClose(response.getInputStream(), ExecInfo.class);
        } catch (Exception e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * List processes running inside the container.
     *
     * @param params
     *         parameters holder
     * @return processes running inside the container
     * @throws IOException
     *         when problems occurs with docker api calls
     */
    public ContainerProcesses top(final TopParams params) throws IOException {
        return doTop(params, dockerDaemonUri);
    }

    @Deprecated
    public ContainerProcesses top(String container, String... psArgs) throws IOException {
        return doTop(new TopParams().withContainer(container)
                                    .withPsArgs(psArgs),
                     dockerDaemonUri);
    }

    /**
     * The same as {@link #top(TopParams)} but with additional parameter
     *
     * @param dockerDaemonUri
     *         docker service URI
     */
    protected ContainerProcesses doTop(final TopParams params, final URI dockerDaemonUri) throws IOException {
        final String container = requiredNonNull(params.getContainer(), "Top: container id is null");
        final String[] psArgs = params.getPsArgs();

        final List<Pair<String, ?>> headers = new ArrayList<>(2);
        headers.add(Pair.of("Content-Type", MediaType.TEXT_PLAIN));
        headers.add(Pair.of("Content-Length", 0));
        final DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                             .method("GET")
                                                             .path("/containers/" + container + "/top")
                                                             .headers(headers);
        if (psArgs != null && psArgs.length != 0) {
            StringBuilder psArgsQueryBuilder = new StringBuilder();
            for (int i = 0, l = psArgs.length; i < l; i++) {
                if (i > 0) {
                    psArgsQueryBuilder.append('+');
                }
                psArgsQueryBuilder.append(URLEncoder.encode(psArgs[i], "UTF-8"));
            }
            connection.query("ps_args", psArgsQueryBuilder.toString());
        }

        try {
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (OK.getStatusCode() != status) {
                throw getDockerException(response);
            }
            return parseResponseStreamAndClose(response.getInputStream(), ContainerProcesses.class);
        } catch (JsonParseException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        } finally {
            connection.close();
        }
    }

    /**
     * Gets files from the specified container.
     *
     * @param params
     *         parameters holder
     * @return stream of resources from the specified container filesystem, with retention connection
     * @throws IOException
     *         when problems occurs with docker api calls
     * @apiNote this method implements 1.20 docker API and requires docker not less than 1.8.0 version
     */
    public InputStream getResource(final GetResourceParams params) throws IOException {
        return doGetResource(params, dockerDaemonUri);
    }

    /**
     * Gets files from the specified container.
     *
     * @param container
     *         container id
     * @param sourcePath
     *         path to file or directory inside specified container
     * @return stream of resources from the specified container filesystem, with retention connection
     * @throws IOException
     *         when problems occurs with docker api calls
     * @apiNote this method implements 1.20 docker API and requires docker not less than 1.8.0 version
     */
    @Deprecated
    public InputStream getResource(String container, String sourcePath) throws IOException {
       return doGetResource(new GetResourceParams().withContainer(container)
                                                   .withSourcePath(sourcePath),
                            dockerDaemonUri);
    }

    /**
     * The same as {@link #getResource(GetResourceParams)} but with additional parameter
     *
     * @param dockerDaemonUri
     *         docker service URI
     */
    protected InputStream doGetResource(final GetResourceParams params, final URI dockerDaemonUri) throws IOException {
        final String container = requiredNonNull(params.getContainer(), "Get resource: container id is null");
        final String sourcePath = requiredNonNull(params.getSourcePath(), "Get resource: source path is not set");

        DockerConnection connection = null;
        try {
            connection = connectionFactory.openConnection(dockerDaemonUri)
                                          .method("GET")
                                          .path("/containers/" + container + "/archive")
                                          .query("path", sourcePath);

            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (status != OK.getStatusCode()) {
                throw getDockerException(response);
            }
            return new CloseConnectionInputStream(response.getInputStream(), connection);
        } catch (IOException io) {
            connection.close();
            throw io;
        }
    }

    /**
     * Puts files into specified container.
     *
     * @param params
     *         parameters holder
     * @throws IOException
     *         when problems occurs with docker api calls, or during file system operations
     * @apiNote this method implements 1.20 docker API and requires docker not less than 1.8 version
     */
    public void putResource(final PutResourceParams params) throws IOException {
        doPutResource(params, dockerDaemonUri);
    }

    /**
     * Puts files into specified container.
     *
     * @param container
     *         container id
     * @param targetPath
     *         path to file or directory inside specified container
     * @param sourceStream
     *         stream of files from source container
     * @param noOverwriteDirNonDir
     *         If "false" then it will be an error if unpacking the given content would cause
     *         an existing directory to be replaced with a non-directory or other resource and vice versa.
     * @throws IOException
     *         when problems occurs with docker api calls, or during file system operations
     * @apiNote this method implements 1.20 docker API and requires docker not less than 1.8 version
     */
    @Deprecated
    public void putResource(String container,
                            String targetPath,
                            InputStream sourceStream,
                            boolean noOverwriteDirNonDir) throws IOException {
       doPutResource(new PutResourceParams().withContainer(container)
                                            .withTargetPath(targetPath)
                                            .withSourceStream(sourceStream)
                                            .withNoOverwriteDirNonDir(noOverwriteDirNonDir),
                     dockerDaemonUri);
    }

    /**
     * The same as {@link #putResource(PutResourceParams)} but with additional parameter
     *
     * @param dockerDaemonUri
     *         docker service URI
     */
    protected void doPutResource(final PutResourceParams params, final URI dockerDaemonUri) throws IOException {
        final String container = requiredNonNull(params.getContainer(), "Put resource: container id is null");
        final String targetPath = requiredNonNull(params.getTargetPath(), "Put resource: target path is not set");
        final InputStream sourceStream = params.getSourceStream();
        final Boolean noOverwriteDirNonDir = params.isNoOverwriteDirNonDir();

        File tarFile;
        long length;
        try (InputStream sourceData = sourceStream) {
            Path tarFilePath = Files.createTempFile("compressed-resources", ".tar");
            tarFile = tarFilePath.toFile();
            length = Files.copy(sourceData, tarFilePath, StandardCopyOption.REPLACE_EXISTING);
        }

        List<Pair<String, ?>> headers = Arrays.asList(Pair.of("Content-Type", ExtMediaType.APPLICATION_X_TAR),
                                                      Pair.of("Content-Length", length));
        try (InputStream tarStream = new BufferedInputStream(new FileInputStream(tarFile));
             DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("PUT")
                                                            .path("/containers/" + container + "/archive")
                                                            .query("path", targetPath)
                                                            .headers(headers)
                                                            .entity(tarStream)) {
            addQueryParamIfSet(connection, "noOverwriteDirNonDir", noOverwriteDirNonDir);
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (status != OK.getStatusCode()) {
                throw getDockerException(response);
            }
        } finally {
            FileCleaner.addFile(tarFile);
        }
    }

    /**
     * Get docker events.
     * Parameter {@code untilSecond} does nothing if {@code sinceSecond} is 0.<br>
     * If {@code untilSecond} and {@code sinceSecond} are 0 method gets new events only (streaming mode).<br>
     * If {@code untilSecond} and {@code sinceSecond} are not 0 (but less that current date)
     * methods get events that were generated between specified dates.<br>
     * If {@code untilSecond} is 0 but {@code sinceSecond} is not method gets old events and streams new ones.<br>
     * If {@code sinceSecond} is 0 no old events will be got.<br>
     * With some connection implementations method can fail due to connection timeout in streaming mode.
     *
     * @param sinceSecond
     *         UNIX date in seconds. allow omit events created before specified date.
     * @param untilSecond
     *         UNIX date in seconds. allow omit events created after specified date.
     * @param filters
     *         filter of needed events. Available filters: {@code event=<string>}
     *         {@code image=<string>} {@code container=<string>}
     * @param messageProcessor
     *         processor of all found events that satisfy specified parameters
     * @throws IOException
     */
    @Deprecated
    public void getEvents(long sinceSecond,
                          long untilSecond,
                          Filters filters,
                          MessageProcessor<Event> messageProcessor) throws IOException {
        doGetEvents(new GetEventsParams().withSinceSecond(sinceSecond)
                                         .withUntilSecond(untilSecond)
                                         .withFilters(filters),
                    messageProcessor,
                    dockerDaemonUri);
    }

    /**
     * Get docker events.
     * Parameter {@code untilSecond} does nothing if {@code sinceSecond} is 0.<br>
     * If {@code untilSecond} and {@code sinceSecond} are 0 method gets new events only (streaming mode).<br>
     * If {@code untilSecond} and {@code sinceSecond} are not 0 (but less that current date)
     * methods get events that were generated between specified dates.<br>
     * If {@code untilSecond} is 0 but {@code sinceSecond} is not method gets old events and streams new ones.<br>
     * If {@code sinceSecond} is 0 no old events will be got.<br>
     * With some connection implementations method can fail due to connection timeout in streaming mode.
     *
     * @param params
     *         parameters holder
     * @param messageProcessor
     *         processor of all found events that satisfy specified parameters
     * @throws IOException
     *         when problems occurs with docker api calls
     */
    public void getEvents(final GetEventsParams params, MessageProcessor<Event> messageProcessor) throws IOException {
        doGetEvents(params, messageProcessor, dockerDaemonUri);
    }

    /**
     * The same as {@link #getEvents(GetEventsParams, MessageProcessor)} but with additional parameter
     *
     * @param dockerDaemonUri
     *         docker service URI
     */
    protected void doGetEvents(final GetEventsParams params,
                               final MessageProcessor<Event> messageProcessor,
                               final URI dockerDaemonUri) throws IOException {
        final Long sinceSecond = params.getSinceSecond();
        final Long untilSecond = params.getUntilSecond();
        final Filters filters = params.getFilters();

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("GET")
                                                            .path("/events")) {
            addQueryParamIfSet(connection, "since", sinceSecond);
            addQueryParamIfSet(connection, "until", untilSecond);
            if (filters != null) {
                connection.query("filters", urlPathSegmentEscaper().escape(JsonHelper.toJson(filters.getFilters())));
            }
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (OK.getStatusCode() != status) {
                throw getDockerException(response);
            }

            try (InputStream responseStream = response.getInputStream()) {
                new MessagePumper<>(new JsonMessageReader<>(responseStream, Event.class), messageProcessor).start();
            }
        }
    }

    /**
     * Builds new docker image from specified dockerfile.
     *
     * @param params
     *         parameters holder
     * @param progressMonitor
     *         ProgressMonitor for images creation process
     * @return image id
     * @throws IOException
     * @throws InterruptedException
     *         if build process was interrupted
     */
    public String buildImage(BuildImageParams params, ProgressMonitor progressMonitor) throws IOException, InterruptedException {
            return doBuildImage(params, progressMonitor, dockerDaemonUri);
    }

    /**
     * The same as {@link #buildImage(BuildImageParams, ProgressMonitor)} but with additional parameter
     *
     * @param dockerDaemonUri
     *         docker service URI
     */
    protected String doBuildImage(final BuildImageParams params,
                                  final ProgressMonitor progressMonitor,
                                  final URI dockerDaemonUri) throws IOException, InterruptedException {
        final String repository = params.getRepository();
        final Boolean doForcePull = params.isDoForcePull();
        final Long memoryLimit = params.getMemoryLimit();
        final Long memorySwapLimit = params.getMemorySwapLimit();
        final List<File> filesList = requiredNonNull(params.getFiles(), "Build image: dockerfile is not set");
        AuthConfigs authConfigs = params.getAuthConfigs();

        File[] files = (File[]) filesList.toArray();

        final File tar = Files.createTempFile(null, ".tar").toFile();
        try {
            createTarArchive(tar, files);

            if (authConfigs == null) {
                authConfigs = initialAuthConfig.getAuthConfigs();
            }
            final List<Pair<String, ?>> headers = new ArrayList<>(3);
            headers.add(Pair.of("Content-Type", "application/x-compressed-tar"));
            headers.add(Pair.of("Content-Length", tar.length()));
            headers.add(Pair.of("X-Registry-Config", Base64.encodeBase64String(JsonHelper.toJson(authConfigs).getBytes())));

            try (InputStream tarInput = new FileInputStream(tar);
                 DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                                .method("POST")
                                                                .path("/build")
                                                                .query("rm", 1)
                                                                .query("forcerm", 1)
                                                                .headers(headers)
                                                                .entity(tarInput)) {
                addQueryParamIfSet(connection, "t", repository);
                addQueryParamIfSet(connection, "memory", memoryLimit);
                addQueryParamIfSet(connection, "memswap", memorySwapLimit);
                addQueryParamIfSet(connection, "pull", doForcePull);
                final DockerResponse response = connection.request();
                final int status = response.getStatus();
                if (OK.getStatusCode() != status) {
                    throw getDockerException(response);
                }
                try (InputStream responseStream = response.getInputStream()) {
                    JsonMessageReader<ProgressStatus> progressReader = new JsonMessageReader<>(responseStream, ProgressStatus.class);

                    final ValueHolder<IOException> errorHolder = new ValueHolder<>();
                    final ValueHolder<String> imageIdHolder = new ValueHolder<>();
                    // Here do some trick to be able interrupt build process. Basically for now it is not possible interrupt docker daemon while
                    // it's building images but here we need just be able to close connection to the unix socket. Thread is blocking while read
                    // from the socket stream so need one more thread that is able to close socket. In this way we can release thread that is
                    // blocking on i/o.
                    final Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                ProgressStatus progressStatus;
                                while ((progressStatus = progressReader.next()) != null) {
                                    final String buildImageId = getBuildImageId(progressStatus);
                                    if (buildImageId != null) {
                                        imageIdHolder.set(buildImageId);
                                    }
                                    progressMonitor.updateProgress(progressStatus);
                                }
                            } catch (IOException e) {
                                errorHolder.set(e);
                            }
                            synchronized (this) {
                                notify();
                            }
                        }
                    };
                    executor.execute(runnable);
                    // noinspection SynchronizationOnLocalVariableOrMethodParameter
                    synchronized (runnable) {
                        runnable.wait();
                    }
                    final IOException ioe = errorHolder.get();
                    if (ioe != null) {
                        throw ioe;
                    }
                    if (imageIdHolder.get() == null) {
                        throw new IOException("Docker image build failed");
                    }
                    return imageIdHolder.get();
                }
            }
        } finally {
            FileCleaner.addFile(tar);
        }
    }

    @Deprecated
    public void removeImage(String image, boolean force) throws IOException {
        doRemoveImage(new RemoveImageParams().withImage(image)
                                             .withForce(force),
                      dockerDaemonUri);
    }

    /**
     * Removes docker image.
     *
     * @param params
     *         parameters holder
     * @throws IOException
     *         when problems occurs with docker api calls
     */
    public void removeImage(final RemoveImageParams params) throws IOException {
        doRemoveImage(params, dockerDaemonUri);
    }

    /**
     * The same as {@link #removeImage(RemoveImageParams)} but with additional parameter
     *
     * @param dockerDaemonUri
     *         docker service URI
     */
    protected void doRemoveImage(final RemoveImageParams params, final URI dockerDaemonUri) throws IOException {
        final String image = requiredNonNull(params.getImage(), "Remove image: image name is null");
        final Boolean force = params.isForce();

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("DELETE")
                                                            .path("/images/" + image)) {
            addQueryParamIfSet(connection, "force", force);
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (OK.getStatusCode() != status) {
                throw getDockerException(response);
            }
        }
    }

    @Deprecated
    public void tag(String image, String repository, String tag) throws IOException {
       doTag(new TagParams().withImage(image)
                            .withRepository(repository)
                            .withTag(tag)
                            .withForce(false),
             dockerDaemonUri);
    }

    /**
     * Tag the docker image into a repository.
     *
     * @param params
     *         parameters holder
     * @throws IOException
     *         when problems occurs with docker api calls
     */
    public void tag(final TagParams params) throws IOException {
        doTag(params, dockerDaemonUri);
    }

    /**
     * The same as {@link #tag(TagParams)} but with additional parameter
     *
     * @param dockerDaemonUri
     *         docker service URI
     */
    protected void doTag(final TagParams params, final URI dockerDaemonUri) throws IOException {
        final String image = requiredNonNull(params.getImage(), "Tag: image name is null");
        final String repository = requiredNonNull(params.getRepository(), "Tag: repository is null");
        final String tag = params.getTag();
        final Boolean force = params.isForce();

        final List<Pair<String, ?>> headers = new ArrayList<>(3);
        headers.add(Pair.of("Content-Type", MediaType.TEXT_PLAIN));
        headers.add(Pair.of("Content-Length", 0));

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/images/" + image + "/tag")
                                                            .query("repo", repository)
                                                            .headers(headers)) {
            addQueryParamIfSet(connection, "force", force);
            addQueryParamIfSet(connection, "tag", tag);
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (status / 100 != 2) {
                throw getDockerException(response);
            }
        }
    }

    /**
     * Push docker image to the registry
     *
     * @param repository
     *         full repository name to be applied to newly created image
     * @param tag
     *         tag of the image
     * @param registry
     *         registry url
     * @param progressMonitor
     *         ProgressMonitor for images creation process
     * @return digest of just pushed image
     * @throws IOException
     *         when problems occurs with docker api calls
     * @throws InterruptedException
     *         if push process was interrupted
     */
    @Deprecated
    public String push(String repository,
                       String tag,
                       String registry,
                       final ProgressMonitor progressMonitor) throws IOException, InterruptedException {
        return doPush(new PushParams().withRepository(repository)
                                      .withTag(tag)
                                      .withRegistry(registry),
                      progressMonitor,
                      dockerDaemonUri);
    }

    /**
     * Push docker image to the registry.
     *
     * @param params
     *         parameters holder
     * @param progressMonitor
     *         ProgressMonitor for images pushing process
     * @return digest of just pushed image
     * @throws IOException
     *         when problems occurs with docker api calls
     * @throws InterruptedException
     *         if push process was interrupted
     */
    public String push(final PushParams params, final ProgressMonitor progressMonitor) throws IOException, InterruptedException {
        return doPush(params,  progressMonitor, dockerDaemonUri);
    }

    /**
     * The same as {@link #push(PushParams, ProgressMonitor)} but with additional parameter
     *
     * @param dockerDaemonUri
     *         docker service URI
     */
    protected String doPush(final PushParams params,
                            final ProgressMonitor progressMonitor,
                            final URI dockerDaemonUri) throws IOException, InterruptedException {
        final String repository = requiredNonNull(params.getRepository(), "Push: repository is null");
        final String tag = params.getTag();
        final String registry = requiredNonNull(params.getRegistry(), "Push: registry is null");

        final List<Pair<String, ?>> headers = new ArrayList<>(3);
        headers.add(Pair.of("Content-Type", MediaType.TEXT_PLAIN));
        headers.add(Pair.of("Content-Length", 0));
        headers.add(Pair.of("X-Registry-Auth", initialAuthConfig.getAuthConfigHeader()));
        final String fullRepo = registry != null ? registry + "/" + repository : repository;
        final ValueHolder<String> digestHolder = new ValueHolder<>();

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/images/" + fullRepo + "/push")
                                                            .headers(headers)) {
            addQueryParamIfSet(connection, "tag", tag);
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (OK.getStatusCode() != status) {
                throw getDockerException(response);
            }
            try (InputStream responseStream = response.getInputStream()) {
                JsonMessageReader<ProgressStatus> progressReader = new JsonMessageReader<>(responseStream, ProgressStatus.class);

                final ValueHolder<IOException> errorHolder = new ValueHolder<>();
                //it is necessary to track errors during the push, this is useful in the case when docker API returns status 200 OK,
                //but in fact we have an error (e.g docker registry is not accessible but we are trying to push).
                final ValueHolder<String> exceptionHolder = new ValueHolder<>();
                // Here do some trick to be able interrupt push process. Basically for now it is not possible interrupt docker daemon while
                // it's pushing images but here we need just be able to close connection to the unix socket. Thread is blocking while read
                // from the socket stream so need one more thread that is able to close socket. In this way we can release thread that is
                // blocking on i/o.
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String digestPrefix = firstNonNull(tag, "latest") + ": digest: ";
                            ProgressStatus progressStatus;
                            while ((progressStatus = progressReader.next()) != null && exceptionHolder.get() == null) {
                                progressMonitor.updateProgress(progressStatus);
                                if (progressStatus.getError() != null) {
                                    exceptionHolder.set(progressStatus.getError());
                                }
                                String status = progressStatus.getStatus();
                                // Here we find string with digest which has following format:
                                // <tag>: digest: <digest> size: <size>
                                // for example:
                                // latest: digest: sha256:9a70e6222ded459fde37c56af23887467c512628eb8e78c901f3390e49a800a0 size: 62189
                                if (status != null && status.startsWith(digestPrefix)) {
                                    String digest = status.substring(digestPrefix.length(), status.indexOf(" ", digestPrefix.length()));
                                    digestHolder.set(digest);
                                }
                            }
                        } catch (IOException e) {
                            errorHolder.set(e);
                        }
                        synchronized (this) {
                            notify();
                        }
                    }
                };
                executor.execute(runnable);
                // noinspection SynchronizationOnLocalVariableOrMethodParameter
                synchronized (runnable) {
                    runnable.wait();
                }
                if (exceptionHolder.get() != null) {
                    throw new DockerException(exceptionHolder.get(), 500);
                }
                if (digestHolder.get() == null) {
                    LOG.error("Docker image {}:{} was successfully pushed, but its digest wasn't obtained",
                              fullRepo,
                              firstNonNull(tag, "latest"));
                    throw new DockerException("Docker image was successfully pushed, but its digest wasn't obtained", 500);
                }
                final IOException ioe = errorHolder.get();
                if (ioe != null) {
                    throw ioe;
                }
            }
        }
        return digestHolder.get();
    }

    @Deprecated
    public String commit(String container, String repository, String tag, String comment, String author) throws IOException {
        // todo: pause container
        return doCommit(new CommitParams().withContainer(container)
                                          .withRepository(repository)
                                          .withTag(tag)
                                          .withComment(comment)
                                          .withAuthor(author),
                        dockerDaemonUri);
    }

    /**
     * Creates a new image from a container’s changes.
     *
     * @param params
     *         parameters holder
     * @return id of a new image
     * @throws IOException
     *         when problems occurs with docker api calls
     */
    public String commit(final CommitParams params) throws IOException {
        // TODO: pause container
        return doCommit(params, dockerDaemonUri);
    }

    /**
     * The same as {@link #commit(CommitParams)} but with additional parameter
     *
     * @param dockerDaemonUri
     *         docker service URI
     */
    protected String doCommit(final CommitParams params, final URI dockerDaemonUri) throws IOException {
        final String container = requiredNonNull(params.getContainer(), "Commit: source container is null");
        final String repository = requiredNonNull(params.getRepository(), "Commit: repository is null");
        final String tag = params.getTag();
        final String comment = params.getComment();
        final String author = params.getAuthor();

        final List<Pair<String, ?>> headers = new ArrayList<>(2);
        headers.add(Pair.of("Content-Type", MediaType.APPLICATION_JSON));
        final String entity = "{}";
        headers.add(Pair.of("Content-Length", entity.getBytes().length));

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/commit")
                                                            .query("container", container)
                                                            .query("repo", repository)
                                                            .headers(headers)
                                                            .entity(entity)) {
            addQueryParamIfSet(connection, "tag", tag);
            addQueryParamIfSet(connection, "comment", URLEncoder.encode(comment, "UTF-8"));
            addQueryParamIfSet(connection, "author", URLEncoder.encode(author, "UTF-8"));
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (CREATED.getStatusCode() != status) {
                throw getDockerException(response);
            }
            return parseResponseStreamAndClose(response.getInputStream(), ContainerCommitted.class).getId();
        } catch (JsonParseException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * See <a href="https://docs.docker.com/reference/api/docker_remote_api_v1.16/#create-an-image">Docker remote API # Create an
     * image</a>.
     * To pull from private registry use registry.address:port/image as image. This is not documented.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Deprecated
    public void pull(String image,
                     String tag,
                     String registry,
                     final ProgressMonitor progressMonitor) throws IOException, InterruptedException {
        doPull(new PullParams().withImage(image)
                               .withTag(tag)
                               .withRegistry(registry),
               progressMonitor,
               dockerDaemonUri);
    }

    /**
     * Pulls docker image from private registry.
     *
     * @param params
     *         parameters holder
     * @param progressMonitor
     *         ProgressMonitor for images creation process
     * @throws IOException
     *         when problems occurs with docker api calls
     * @throws InterruptedException
     *         if push process was interrupted
     */
    public void pull(final PullParams params, final ProgressMonitor progressMonitor) throws IOException, InterruptedException {
        doPull(params, progressMonitor, dockerDaemonUri);
    }

    /**
     * See <a href="https://docs.docker.com/reference/api/docker_remote_api_v1.16/#create-an-image">Docker remote API # Create an
     * image</a>.
     * To pull from private registry use registry.address:port/image as image. This is not documented.
     *
     * @param params
     *         parameters holder
     * @param progressMonitor
     *         ProgressMonitor for images creation process
     * @param dockerDaemonUri
     *         docker service URI
     * @throws IOException
     *         when problems occurs with docker api calls
     * @throws InterruptedException
     *         if push process was interrupted
     */
    protected void doPull(final PullParams params,
                          final ProgressMonitor progressMonitor,
                          final URI dockerDaemonUri) throws IOException, InterruptedException {
        final String image = requiredNonNull(params.getImage(), "Pull: image is null");
        final String tag = params.getTag();
        final String registry = params.getRegistry();

        final List<Pair<String, ?>> headers = new ArrayList<>(3);
        headers.add(Pair.of("Content-Type", MediaType.TEXT_PLAIN));
        headers.add(Pair.of("Content-Length", 0));
        headers.add(Pair.of("X-Registry-Auth", initialAuthConfig.getAuthConfigHeader()));

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/images/create")
                                                            .query("fromImage", registry != null ? registry + "/" + image : image)
                                                            .headers(headers)) {
            addQueryParamIfSet(connection, "tag", tag);
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (OK.getStatusCode() != status) {
                throw getDockerException(response);
            }
            try (InputStream responseStream = response.getInputStream()) {
                JsonMessageReader<ProgressStatus> progressReader = new JsonMessageReader<>(responseStream, ProgressStatus.class);

                final ValueHolder<IOException> errorHolder = new ValueHolder<>();
                // Here do some trick to be able interrupt pull process. Basically for now it is not possible interrupt docker daemon while
                // it's pulling images but here we need just be able to close connection to the unix socket. Thread is blocking while read
                // from the socket stream so need one more thread that is able to close socket. In this way we can release thread that is
                // blocking on i/o.
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ProgressStatus progressStatus;
                            while ((progressStatus = progressReader.next()) != null) {
                                progressMonitor.updateProgress(progressStatus);
                            }
                        } catch (IOException e) {
                            errorHolder.set(e);
                        }
                        synchronized (this) {
                            notify();
                        }
                    }
                };
                executor.execute(runnable);
                // noinspection SynchronizationOnLocalVariableOrMethodParameter
                synchronized (runnable) {
                    runnable.wait();
                }
                final IOException ioe = errorHolder.get();
                if (ioe != null) {
                    throw ioe;
                }
            }
        }
    }

    @Deprecated
    public ContainerCreated createContainer(ContainerConfig containerConfig, String containerName) throws IOException {
        return doCreateContainer(new CreateContainerParams().withContainerConfig(containerConfig)
                                                            .withContainerName(containerName),
                                 dockerDaemonUri);
    }

    /**
     * Creates docker container.
     *
     * @param params
     *         parameters holder
     * @return information about just created container
     * @throws IOException
     *         when problems occurs with docker api calls
     */
    public ContainerCreated createContainer(final CreateContainerParams params) throws IOException {
        return doCreateContainer(params, dockerDaemonUri);
    }

    /**
     * The same as {@link #createContainer(CreateContainerParams)} but with additional parameter
     *
     * @param dockerDaemonUri
     *         docker service URI
     */
    protected ContainerCreated doCreateContainer(final CreateContainerParams params, final URI dockerDaemonUri) throws IOException {
        final ContainerConfig containerConfig = requiredNonNull(params.getContainerConfig(),
                                                                "Create container: container config is not set");
        final String containerName = params.getContainerName();

        final List<Pair<String, ?>> headers = new ArrayList<>(2);
        headers.add(Pair.of("Content-Type", MediaType.APPLICATION_JSON));
        final String entity = JsonHelper.toJson(containerConfig, FIRST_LETTER_LOWERCASE);
        headers.add(Pair.of("Content-Length", entity.getBytes().length));

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/containers/create")
                                                            .headers(headers)
                                                            .entity(entity)) {
            addQueryParamIfSet(connection, "name", containerName);
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (CREATED.getStatusCode() != status) {
                throw getDockerException(response);
            }
            return parseResponseStreamAndClose(response.getInputStream(), ContainerCreated.class);
        } catch (JsonParseException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }

    @Deprecated
    public void startContainer(String container, HostConfig hostConfig) throws IOException {
        final List<Pair<String, ?>> headers = new ArrayList<>(2);
        headers.add(Pair.of("Content-Type", MediaType.APPLICATION_JSON));
        final String entity = hostConfig == null ? "{}" : JsonHelper.toJson(hostConfig, FIRST_LETTER_LOWERCASE);
        headers.add(Pair.of("Content-Length", entity.getBytes().length));

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/containers/" + container + "/start")
                                                            .headers(headers)
                                                            .entity(entity)) {
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (!(NO_CONTENT.getStatusCode() == status || NOT_MODIFIED.getStatusCode() == status)) {

                final DockerException dockerException = getDockerException(response);
                if (OK.getStatusCode() == status) {
                    // docker API 1.20 returns 200 with warning message about usage of loopback docker backend
                    LOG.warn(dockerException.getLocalizedMessage());
                } else {
                    throw dockerException;
                }
            }
        }
    }

    /**
     * Starts docker container.
     *
     * @param params
     *         parameters holder
     * @throws IOException
     *         when problems occurs with docker api calls
     */
    public void startContainer(final StartContainerParams params) throws IOException {
        doStartContainer(params, dockerDaemonUri);
    }

    /**
     * The same as {@link #startContainer(StartContainerParams)} but with additional parameter
     *
     * @param dockerDaemonUri
     *         docker service URI
     */
    protected void doStartContainer(final StartContainerParams params, final URI dockerDaemonUri) throws IOException {
        final String container = requiredNonNull(params.getContainer(), "Start container: container id is null");

        final List<Pair<String, ?>> headers = new ArrayList<>(2);
        headers.add(Pair.of("Content-Type", MediaType.APPLICATION_JSON));

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/containers/" + container + "/start")
                                                            .headers(headers)) {
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (!(NO_CONTENT.getStatusCode() == status || NOT_MODIFIED.getStatusCode() == status)) {

                final DockerException dockerException = getDockerException(response);
                if (OK.getStatusCode() == status) {
                    // docker API 1.20 returns 200 with warning message about usage of loopback docker backend
                    LOG.warn(dockerException.getLocalizedMessage());
                } else {
                    throw dockerException;
                }
            }
        }
    }

    private String getBuildImageId(ProgressStatus progressStatus) {
        final String stream = progressStatus.getStream();
        if (stream != null && stream.startsWith("Successfully built ")) {
            int endSize = 19;
            while (endSize < stream.length() && Character.digit(stream.charAt(endSize), 16) != -1) {
                endSize++;
            }
            return stream.substring(19, endSize);
        }
        return null;
    }

    private <T> T parseResponseStreamAndClose(InputStream inputStream, Class<T> clazz) throws IOException, JsonParseException {
        try (InputStream responseStream = inputStream) {
            return JsonHelper.fromJson(responseStream,
                                       clazz,
                                       null,
                                       FIRST_LETTER_LOWERCASE);
        }
    }

    protected DockerException getDockerException(DockerResponse response) throws IOException {
        try (InputStreamReader isr = new InputStreamReader(response.getInputStream())) {
            String dockerResponseContent = CharStreams.toString(isr);
            return new DockerException(
                    "Error response from docker API, status: " + response.getStatus() + ", message: " + dockerResponseContent,
                    dockerResponseContent,
                    response.getStatus());
        }
    }

    // Unfortunately we can't use generated DTO here.
    // Docker uses uppercase in first letter in names of json objects, e.g. {"Id":"123"} instead of {"id":"123"}
    protected static JsonNameConvention FIRST_LETTER_LOWERCASE = new JsonNameConvention() {
        @Override
        public String toJsonName(String javaName) {
            return Character.toUpperCase(javaName.charAt(0)) + javaName.substring(1);
        }

        @Override
        public String toJavaName(String jsonName) {
            return Character.toLowerCase(jsonName.charAt(0)) + jsonName.substring(1);
        }
    };

    private void createTarArchive(File tar, File... files) throws IOException {
        TarUtils.tarFiles(tar, 0, files);
    }

    /**
     * Adds given parameter to query if it set (not null).
     *
     * @param connection
     *         connection to docker service
     * @param queryParamName
     *         name of query parameter
     * @param paramValue
     *         value of query parameter
     */
    public void addQueryParamIfSet(DockerConnection connection, String queryParamName, Object paramValue) {
        if (paramValue != null && queryParamName != null && !queryParamName.equals("")) {
            connection.query(queryParamName, paramValue);
        }
    }

    /**
     * Adds given boolean parameter to query if it set (not null).
     * In case of {@code true} '1' will be added, in case of {@code false} '0'.
     *
     * @param connection
     *         connection to docker service
     * @param queryParamName
     *         name of query parameter
     * @param paramValue
     *         value of query parameter
     */
    public void addQueryParamIfSet(DockerConnection connection, String queryParamName, Boolean paramValue) {
        if (paramValue != null && queryParamName != null && !queryParamName.equals("")) {
            connection.query(queryParamName, paramValue ? 1 : 0);
        }
    }

    /**
     * Checks a value for null and return it if it non null.
     * If a value is null, then {@code {@link NullPointerException}} will be thrown.
     *
     * @param value
     *         the value to test for null
     * @param errorMessage
     *         message of {@code {@link NullPointerException}} if {@code value} is null
     * @return @{code value} if it non null
     * @throws NullPointerException
     *         if {@code value} is null
     */
    private static <T> T requiredNonNull(T value, String errorMessage) {
        if (value == null) {
            throw new NullPointerException(errorMessage);
        }
        return value;
    }

}
