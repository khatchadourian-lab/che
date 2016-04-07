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

import javax.validation.constraints.NotNull;

import static java.util.Objects.requireNonNull;

/**
 * Arguments holder for {@link org.eclipse.che.plugin.docker.client.DockerConnector#commit(CommitParams)}.
 *
 * @author Mykola Morhun
 */
public class CommitParams {

    private String container;
    private String repository;
    private String tag;
    private String comment;
    private String author;

    /**
     * Creates arguments holder with required parameters.
     *
     * @param container
     *         info about this parameter @see {@link #withContainer(String)}
     * @param repository
     *         info about this parameter @see {@link #withRepository(String)}
     * @return push arguments holder with required parameters
     */
    public static CommitParams from(@NotNull String container, @NotNull String repository) {
        return new CommitParams().withContainer(container)
                                 .withRepository(repository);
    }

    private CommitParams() {}

    /**
     * @param container
     *         id or name of container
     */
    public CommitParams withContainer(@NotNull String container) {
        requireNonNull(container);
        this.container = container;
        return this;
    }

    /**
     * @param repository
     *         full repository name
     */
    public CommitParams withRepository(@NotNull String repository) {
        requireNonNull(repository);
        this.repository = repository;
        return this;
    }

    /**
     * @param tag
     *         tag of the image
     */
    public CommitParams withTag(String tag) {
        this.tag = tag;
        return this;
    }

    /**
     * @param comment
     *         commit message
     */
    public CommitParams withComment(String comment) {
        this.comment = comment;
        return this;
    }

    /**
     * @param author
     *         author of the commit
     */
    public CommitParams withAuthor(String author) {
        this.author = author;
        return this;
    }

    public String container() {
        return container;
    }

    public String repository() {
        return repository;
    }

    public String tag() {
        return tag;
    }

    public String comment() {
        return comment;
    }

    public String author() {
        return author;
    }

}
