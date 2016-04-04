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

import org.eclipse.che.plugin.docker.client.dto.AuthConfigs;

import java.io.File;

/**
 * Arguments holder for {@code buildImage} method of {@link org.eclipse.che.plugin.docker.client.DockerConnector}.
 *
 * @author Mykola Morhun
 */
public class BuildImageParams {
    /** full repository name to be applied to newly created image */
    private String repository;
    /** authentication configuration for private registries. Can be null */
    private AuthConfigs authConfigs;
    /** is pull with force */
    private Boolean doForcePull;
    /** memory limit for build in bytes */
    private Long memoryLimit;
    /** total memory in bytes (memory + swap), -1 to enable unlimited swap */
    private Long memorySwapLimit;
    /**
     * files that are needed for creation docker images (e.g. file of directories used in ADD instruction in Dockerfile).
     * One of them must be Dockerfile.
     */
    private File[] files;

    public BuildImageParams withRepository(String repository) {
        this.repository = repository;
        return this;
    }

    public BuildImageParams withAuthConfigs(AuthConfigs authConfigs) {
        this.authConfigs = authConfigs;
        return this;
    }

    public BuildImageParams withDoForcePull(Boolean doForcePull) {
        this.doForcePull = doForcePull;
        return this;
    }

    public BuildImageParams withMemoryLimit(Long memoryLimit) {
        this.memoryLimit = memoryLimit;
        return this;
    }

    public BuildImageParams withMemorySwapLimit(Long memorySwapLimit) {
        this.memorySwapLimit = memorySwapLimit;
        return this;
    }

    public BuildImageParams withFiles(File... files) {
        this.files = files;
        return this;
    }

    public String getRepository() {
        return repository;
    }

    public AuthConfigs getAuthConfigs() {
        return authConfigs;
    }

    public Boolean isDoForcePull() {
        return doForcePull;
    }

    public Long getMemoryLimit() {
        return memoryLimit;
    }

    public Long getMemorySwapLimit() {
        return memorySwapLimit;
    }

    public File[] getFiles() {
        return files;
    }

}
