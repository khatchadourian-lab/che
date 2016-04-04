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
 * Arguments holder for {@link org.eclipse.che.plugin.docker.client.DockerConnector#push(PushParams, ProgressMonitor)}.
 *
 * @author Mykola Morhun
 */
public class PushParams {

    private String repository;
    private String tag;
    private String registry;

    /**
     * @param repository
     *         full repository name
     */
    public PushParams withRepository(String repository) {
        this.repository = repository;
        return this;
    }

    /**
     * @param tag
     *         tag of the image
     */
    public PushParams withTag(String tag) {
        this.tag = tag;
        return this;
    }

    /**
     * @param registry
     *         registry url
     */
    public PushParams withRegistry(String registry) {
        this.registry = registry;
        return this;
    }

    public String getRepository() {
        return repository;
    }

    public String getTag() {
        return tag;
    }

    public String getRegistry() {
        return registry;
    }

}
