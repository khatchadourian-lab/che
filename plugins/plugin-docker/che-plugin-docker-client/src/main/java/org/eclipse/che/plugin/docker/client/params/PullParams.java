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

/**
 * Arguments holder for {@link org.eclipse.che.plugin.docker.client.DockerConnector#pull(PullParams, ProgressMonitor)}.
 *
 * @author Mykola Morhun
 */
public class PullParams {

    private String image;
    private String tag;
    private String registry;

    /**
     * @param image
     *         full repository name to be applied to newly created image
     */
    public PullParams withImage(String image) {
        this.image = image;
        return this;
    }

    /**
     * @param tag
     *         tag of the image
     */
    public PullParams withTag(String tag) {
        this.tag = tag;
        return this;
    }

    /**
     * @param registry
     *         registry url
     */
    public PullParams withRegistry(String registry) {
        this.registry = registry;
        return this;
    }

    public String getImage() {
        return image;
    }

    public String getTag() {
        return tag;
    }

    public String getRegistry() {
        return registry;
    }

}
