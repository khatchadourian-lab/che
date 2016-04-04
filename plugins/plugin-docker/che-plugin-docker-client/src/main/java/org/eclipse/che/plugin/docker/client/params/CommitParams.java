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
 * Arguments holder for {@code commit} method of {@link org.eclipse.che.plugin.docker.client.DockerConnector}.
 *
 * @author Mykola Morhun
 */
public class CommitParams {
    /** source container */
    private String container;
    /** full repository name */
    private String repository;
    /** tag of the image */
    private String tag;
    /** commit message */
    private String comment;
    /** author of the commit */
    private String author;

    public CommitParams withContainer(String container) {
        this.container = container;
        return this;
    }

    public CommitParams withRepository(String repository) {
        this.repository = repository;
        return this;
    }

    public CommitParams withTag(String tag) {
        this.tag = tag;
        return this;
    }

    public CommitParams withComment(String comment) {
        this.comment = comment;
        return this;
    }

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
