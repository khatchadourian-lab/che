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
package org.eclipse.che.plugin.docker.client.params;

import org.eclipse.che.plugin.docker.client.ProgressMonitor;
import org.eclipse.che.plugin.docker.client.dto.AuthConfigs;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Arguments holder for {@link org.eclipse.che.plugin.docker.client.DockerConnector#buildImage(BuildImageParams, ProgressMonitor)}.
 *
 * @author Mykola Morhun
 */
public class BuildImageParams {

    private String      repository;
    private AuthConfigs authConfigs;
    private Boolean     doForcePull;
    private Long        memoryLimit;
    private Long        memorySwapLimit;
    private List<File>  files;

    /**
     * @param repository
     *         full repository name to be applied to newly created image
     */
    public BuildImageParams withRepository(String repository) {
        this.repository = repository;
        return this;
    }

    /**
     * @param authConfigs
     *         authentication configuration for registries. Can be null
     */
    public BuildImageParams withAuthConfigs(AuthConfigs authConfigs) {
        this.authConfigs = authConfigs;
        return this;
    }

    /**
     * @param doForcePull
     *         if {@code true} attempts to pull the image even if an older image exists locally
     */
    public BuildImageParams withDoForcePull(boolean doForcePull) {
        this.doForcePull = doForcePull;
        return this;
    }

    /**
     * @param memoryLimit
     *         RAM memory limit for build in bytes
     */
    public BuildImageParams withMemoryLimit(long memoryLimit) {
        this.memoryLimit = memoryLimit;
        return this;
    }

    /**
     * @param memorySwapLimit
     *         total memory in bytes (memory + swap), -1 to enable unlimited swap
     */
    public BuildImageParams withMemorySwapLimit(long memorySwapLimit) {
        this.memorySwapLimit = memorySwapLimit;
        return this;
    }

    /**
     * Sets list of files for creation docker image.
     *
     * @param files
     *         files that are needed for creation docker images (e.g. file of directories used in ADD instruction in Dockerfile).
     *         One of them must be Dockerfile.
     */
    public BuildImageParams withFiles(File... files) {
        this.files = Arrays.asList(files);
        return this;
    }

    /**
     * Adds files to the file list.
     * @see {@link #withFiles(File...)}
     *
     * @param files
     *         files to add to image
     */
    public BuildImageParams addFiles(File... files) {
        this.files.addAll(Arrays.asList(files));
        return this;
    }

    /**
     * The same as {@link #addFiles(File...)}, but for single file.
     */
    public BuildImageParams addFile(File file) {
        this.files.add(file);
        return this;
    }

    public String getRepository() {
        return repository;
    }

    public AuthConfigs getAuthConfigs() {
        return authConfigs;
    }

    public Boolean isDoForcePull() {
        return doForcePull;
    }

    public Long getMemoryLimit() {
        return memoryLimit;
    }

    public Long getMemorySwapLimit() {
        return memorySwapLimit;
    }

    public List<File> getFiles() {
        return files;
    }

}
