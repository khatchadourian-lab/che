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

/**
 * Arguments holder for {@code removeContainer} method of {@link org.eclipse.che.plugin.docker.client.DockerConnector}.
 *
 * @author Mykola Morhun
 */
public class RemoveContainerParams {
    /** container identifier, either id or name */
    private String container;
    /** if {@code true} kills the running container then remove it */
    private Boolean force;
    /** if {@code true} removes volumes associated to the container */
    private Boolean removeVolumes;

    public RemoveContainerParams withContainer(String container) {
        this.container = container;
        return this;
    }

    public RemoveContainerParams withForce(Boolean force) {
        this.force = force;
        return this;
    }

    public RemoveContainerParams withRemoveVolumes(Boolean removeVolumes) {
        this.removeVolumes = removeVolumes;
        return this;
    }

    public String getContainer() {
        return container;
    }

    public Boolean isForce() {
        return force;
    }

    public Boolean isRemoveVolumes() {
        return removeVolumes;
    }

}
