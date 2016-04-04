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
 * Arguments holder for {@link org.eclipse.che.plugin.docker.client.DockerConnector#tag(TagParams)}.
 *
 * @author Mykola Morhun
 */
public class TagParams {

    private String  image;
    private String  repository;
    private String  tag;
    private Boolean force;

    /**
     * @param image
     *         image name
     */
    public TagParams withImage(String image) {
        this.image = image;
        return this;
    }

    /**
     * @param repository
     *         the repository to tag in
     */
    public TagParams withRepository(String repository) {
        this.repository = repository;
        return this;
    }

    /**
     * @param tag
     *         new tag name
     */
    public TagParams withTag(String tag) {
        this.tag = tag;
        return this;
    }

    /**
     * @param force
     *         force tagging of the image
     */
    public TagParams withForce(boolean force) {
        this.force = force;
        return this;
    }

    public String getImage() {
        return image;
    }

    public String getRepository() {
        return repository;
    }

    public String getTag() {
        return tag;
    }

    public Boolean isForce() {
        return force;
    }

}
