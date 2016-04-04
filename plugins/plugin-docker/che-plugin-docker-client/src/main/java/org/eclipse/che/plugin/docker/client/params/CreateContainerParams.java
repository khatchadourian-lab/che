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

import org.eclipse.che.plugin.docker.client.json.ContainerConfig;

/**
 * Arguments holder for {@code createContainer} method of {@link org.eclipse.che.plugin.docker.client.DockerConnector}.
 *
 * @author Mykola Morhun
 */
public class CreateContainerParams {
    /** configuration of future container */
    private ContainerConfig containerConfig;
    /** assign the specified name to the container. Must match /?[a-zA-Z0-9_-]+ */
    private String containerName;

    public CreateContainerParams withContainerConfig(ContainerConfig containerConfig) {
        this.containerConfig = containerConfig;
        return this;
    }

    public CreateContainerParams withContainerName(String containerName) {
        this.containerName = containerName;
        return this;
    }

    public ContainerConfig getContainerConfig() {
        return containerConfig;
    }

    public String getContainerName() {
        return containerName;
    }

}
