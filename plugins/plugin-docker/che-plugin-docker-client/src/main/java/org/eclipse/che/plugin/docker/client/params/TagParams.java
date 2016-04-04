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
 * Arguments holder for {@code tag} method of {@link org.eclipse.che.plugin.docker.client.DockerConnector}.
 *
 * @author Mykola Morhun
 */
public class TagParams {
    /** image name */
    private String  image;
    /** the repository to tag in */
    private String  repository;
    /** new tag name */
    private String  tag;
    /** force tagging of the image */
    private Boolean force;

    public TagParams withImage(String image) {
        this.image = image;
        return this;
    }

    public TagParams withRepository(String repository) {
        this.repository = repository;
        return this;
    }

    public TagParams withTag(String tag) {
        this.tag = tag;
        return this;
    }

    public TagParams withForce(Boolean force) {
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
