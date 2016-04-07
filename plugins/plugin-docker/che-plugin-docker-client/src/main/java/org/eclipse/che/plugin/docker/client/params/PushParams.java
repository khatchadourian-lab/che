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

import javax.validation.constraints.NotNull;

import static java.util.Objects.requireNonNull;

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
     * Creates arguments holder with required parameters.
     *
     * @param repository
     *         info about this parameter @see {@link #withRepository(String)}
     * @return push arguments holder with required parameters
     */
    public static PushParams from(@NotNull String repository) {
        return new PushParams().withRepository(repository);
    }

    private PushParams() {}

    /**
     * @param repository
     *         repository name
     */
    public PushParams withRepository(@NotNull String repository) {
        requireNonNull(repository);
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

    public String repository() {
        return repository;
    }

    public String tag() {
        return tag;
    }

    public String registry() {
        return registry;
    }

}
