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
    private String id;

    /**
     * @param execId
     *         exec id
     */
    public StartExecParams withExecId(String execId) {
        id = execId;
        return this;
    }

    public String getExecId() {
        return id;
    }

}
