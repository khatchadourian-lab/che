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

import org.eclipse.che.plugin.docker.client.MessageProcessor;

import javax.validation.constraints.NotNull;

import static java.util.Objects.requireNonNull;

/**
 * Arguments holder for {@link org.eclipse.che.plugin.docker.client.DockerConnector#attachContainer(AttachContainerParams, MessageProcessor)} .
 *
 * @author Mykola Morhun
 */
public class AttachContainerParams {

    private String  container;
    private Boolean stream;

    /**
     * Creates arguments holder with required parameters.
     *
     * @param container
     *         info about this parameter @see {@link #withContainer(String)}
     * @return attach container arguments holder with required parameters
     */
    public static AttachContainerParams from(@NotNull String container) {
        return new AttachContainerParams().withContainer(container);
    }

    private AttachContainerParams() {}

    /**
     * @param container
     *         id or name of container
     */
    public AttachContainerParams withContainer(@NotNull String container) {
        requireNonNull(container);
        this.container = container;
        return this;
    }

    /**
     * @param stream
     *         if {@code true} gets output stream from container.<br/>
     *         Note, that live stream blocks until container is running.<br/>
     *         When using the TTY setting is enabled when from container, the stream is the raw data
     *          from the process PTY and client’s stdin.
     *         When the TTY is disabled, then the stream is multiplexed to separate stdout and stderr.
     */
    public AttachContainerParams withStream(boolean stream) {
        this.stream = stream;
        return this;
    }

    public String container() {
        return container;
    }

    public Boolean stream() {
        return stream;
    }
}
