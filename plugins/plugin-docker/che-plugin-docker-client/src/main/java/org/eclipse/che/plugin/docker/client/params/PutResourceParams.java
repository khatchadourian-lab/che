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

import java.io.InputStream;

/**
 * Arguments holder for {@link org.eclipse.che.plugin.docker.client.DockerConnector#putResource(PutResourceParams)}.
 *
 * @author Mykola Morhun
 */
public class PutResourceParams {

    private String container;
    private String targetPath;
    private InputStream sourceStream;
    private Boolean noOverwriteDirNonDir;

    /**
     * @param container
     *         container id
     */
    public PutResourceParams withContainer(String container) {
        this.container = container;
        return this;
    }

    /**
     * @param targetPath
     *         path to file or directory inside specified container
     */
    public PutResourceParams withTargetPath(String targetPath) {
        this.targetPath = targetPath;
        return this;
    }

    /**
     * @param sourceStream
     *         stream of files from source container
     */
    public PutResourceParams withSourceStream(InputStream sourceStream) {
        this.sourceStream = sourceStream;
        return this;
    }

    /**
     * @param noOverwriteDirNonDir
     *         If "false" then it will be an error if unpacking the given content would cause an existing
     *          directory to be replaced with a non-directory or other resource and vice versa.
     */
    public PutResourceParams withNoOverwriteDirNonDir(Boolean noOverwriteDirNonDir) {
        this.noOverwriteDirNonDir = noOverwriteDirNonDir;
        return this;
    }

    public String getContainer() {
        return container;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public InputStream getSourceStream() {
        return sourceStream;
    }

    public Boolean isNoOverwriteDirNonDir() {
        return noOverwriteDirNonDir;
    }

}
