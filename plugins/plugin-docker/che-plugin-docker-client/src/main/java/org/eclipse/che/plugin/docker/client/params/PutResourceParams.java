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
 * Arguments holder for {@code putResource} method of {@link org.eclipse.che.plugin.docker.client.DockerConnector}.
 *
 * @author Mykola Morhun
 */
public class PutResourceParams {
    /** container id */
    private String container;
    /** path to file or directory inside specified container */
    private String targetPath;
    /** stream of files from source container */
    private InputStream sourceStream;
    /**
     * If "false" then it will be an error if unpacking the given content would cause an existing
     * directory to be replaced with a non-directory or other resource and vice versa.
     */
    private Boolean noOverwriteDirNonDir;

    public PutResourceParams withContainer(String container) {
        this.container = container;
        return this;
    }

    public PutResourceParams withTargetPath(String targetPath) {
        this.targetPath = targetPath;
        return this;
    }

    public PutResourceParams withSourceStream(InputStream sourceStream) {
        this.sourceStream = sourceStream;
        return this;
    }

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
