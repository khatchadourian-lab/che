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
     * @param container
     *         id or name of container
     */
    public CommitParams withContainer(String container) {
        this.container = container;
        return this;
    }

    /**
     * @param repository
     *         full repository name
     */
    public CommitParams withRepository(String repository) {
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

    public String getContainer() {
        return container;
    }

    public String getRepository() {
        return repository;
    }

    public String getTag() {
        return tag;
    }

    public String getComment() {
        return comment;
    }

    public String getAuthor() {
        return author;
    }

}
