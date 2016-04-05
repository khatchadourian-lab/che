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

import org.eclipse.che.plugin.docker.client.MessageProcessor;

/**
 * Arguments holder for {@link org.eclipse.che.plugin.docker.client.DockerConnector#startExec(StartExecParams, MessageProcessor)}.
 *
 * @author Mykola Morhun
 */
public class StartExecParams {

    private String  id;
    private Boolean detach;
    private Boolean tty;

    /**
     * @param execId
     *         exec id
     */
    public StartExecParams withExecId(String execId) {
        id = execId;
        return this;
    }

    /**
     * @param detach
     *         If detach is {@code true}, API returns after starting the exec command.
     *         Otherwise, API sets up an interactive session with the exec command.
     */
    public StartExecParams withDetach(boolean detach) {
        this.detach = detach;
        return this;
    }

    /**
     * @param tty
     *         if {@code true} then will be allocated a pseudo-TTY
     */
    public StartExecParams withExecId(boolean tty) {
        this.tty = tty;
        return this;
    }

    public String getExecId() {
        return id;
    }

    public Boolean isDetach() {
        return detach;
    }

    public Boolean isTty() {
        return tty;
    }
}
