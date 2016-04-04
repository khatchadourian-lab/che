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

import org.eclipse.che.plugin.docker.client.json.HostConfig;

/**
 * Arguments holder for {@code startContainer} method of {@link org.eclipse.che.plugin.docker.client.DockerConnector}.
 *
 * @author Mykola Morhun
 */
public class StartContainerParams {
    /** id of container to start */
    private String     container;
    private HostConfig hostConfig;

    public StartContainerParams withContainer(String container) {
        this.container = container;
        return this;
    }

    public StartContainerParams withHostConfig(HostConfig hostConfig) {
        this.hostConfig = hostConfig;
        return this;
    }

    public String getContainer() {
        return container;
    }

    public HostConfig getHostConfig() {
        return hostConfig;
    }

}
