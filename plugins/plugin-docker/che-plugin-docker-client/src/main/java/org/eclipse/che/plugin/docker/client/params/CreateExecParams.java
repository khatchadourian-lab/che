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
 * Arguments holder for {@code createExec} method of {@link org.eclipse.che.plugin.docker.client.DockerConnector}.
 *
 * @author Mykola Morhun
 */
public class CreateExecParams {
    /** id of container */
    private String   container;
    /** is stdout & stderr detached */
    private Boolean  detach;
    /** command to run specified as a string or an array of strings */
    private String[] cmd;

    public CreateExecParams withContainer(String container) {
        this.container = container;
        return this;
    }

    public CreateExecParams withDetach(Boolean detach) {
        this.detach = detach;
        return this;
    }

    public CreateExecParams withCmd(String[] cmd) {
        this.cmd = cmd;
        return this;
    }

    public String getContainer() {
        return container;
    }

    public Boolean isDetach() {
        return detach;
    }

    public String[] getCmd() {
        return cmd;
    }

}
