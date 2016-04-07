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

import javax.validation.constraints.NotNull;

import static java.util.Objects.requireNonNull;

/**
 * Arguments holder for {@link org.eclipse.che.plugin.docker.client.DockerConnector#getExecInfo(GetExecInfoParams)}.
 *
 * @author Mykola Morhun
 */
public class GetExecInfoParams {

    private String execId;

    /**
     * Creates arguments holder with required parameters.
     *
     * @param execId
     *         info about this parameter @see {@link #withExecId(String)}
     * @return get exec info arguments holder with required parameters
     */
    public static GetExecInfoParams from(@NotNull String execId) {
        return new GetExecInfoParams().withExecId(execId);
    }

    private GetExecInfoParams() {}

    /**
     * @param execId
     *         exec id
     */
    public GetExecInfoParams withExecId(@NotNull String execId) {
        requireNonNull(execId);
        this.execId = execId;
        return this;
    }

    public String execId() {
        return execId;
    }

}
