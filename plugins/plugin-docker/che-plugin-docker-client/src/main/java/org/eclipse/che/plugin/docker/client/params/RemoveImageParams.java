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
 * Arguments holder for {@link org.eclipse.che.plugin.docker.client.DockerConnector#removeImage(RemoveImageParams)}.
 *
 * @author Mykola Morhun
 */
public class RemoveImageParams {

    private String  image;
    private Boolean force;

    /**
     * @param image
     *         image name
     */
    public RemoveImageParams withImage(String image) {
        this.image = image;
        return this;
    }

    /**
     * @param force
     *         force removal of the image
     */
    public RemoveImageParams withForce(boolean force) {
        this.force = force;
        return this;
    }

    public String getImage() {
        return image;
    }

    public Boolean isForce() {
        return force;
    }

}
