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

/**
 * Arguments holder for {@link org.eclipse.che.plugin.docker.client.DockerConnector#attachContainer(AttachContainerParams, MessageProcessor)} .
 *
 * @author Mykola Morhun
 */
public class AttachContainerParams {

    private String container;
    private Boolean stream;

    /**
     * @param container
     *         id of container
     */
    public AttachContainerParams withContainer(String container) {
        this.container = container;
        return this;
    }

    /**
     * @param stream
     *         if {@code true} gets 'live' stream from container.
     *         Note, that 'live' stream blocks until container is running.
     */
    public AttachContainerParams withStream(boolean stream) {
        this.stream = stream;
        return this;
    }

    public String getContainer() {
        return container;
    }

    public Boolean isStream() {
        return stream;
    }
}
