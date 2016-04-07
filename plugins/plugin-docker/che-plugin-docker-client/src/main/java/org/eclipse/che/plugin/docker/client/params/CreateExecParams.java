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
 * Arguments holder for {@link org.eclipse.che.plugin.docker.client.DockerConnector#createExec(CreateExecParams)}.
 *
 * @author Mykola Morhun
 */
public class CreateExecParams {

    private String   container;
    private Boolean  detach;
    private String[] cmd;

    /**
     * Creates arguments holder with required parameters.
     *
     * @param container
     *         info about this parameter @see {@link #withContainer(String)}
     * @param cmd
     *         info about this parameter @see {@link #withCmd(String[])}
     * @return create exec arguments holder with required parameters
     */
    public static CreateExecParams from(@NotNull String container, @NotNull String[] cmd) {
        return new CreateExecParams().withContainer(container)
                                     .withCmd(cmd);
    }

    private CreateExecParams() {}

    /**
     * @param container
     *         id or name of container
     */
    public CreateExecParams withContainer(@NotNull String container) {
        requireNonNull(container);
        this.container = container;
        return this;
    }

    /**
     * @param detach
     *         is stdout & stderr detached
     */
    public CreateExecParams withDetach(boolean detach) {
        this.detach = detach;
        return this;
    }

    /**
     * @param cmd
     *         command to run specified as a string or an array of strings
     */
    public CreateExecParams withCmd(@NotNull String[] cmd) {
        requireNonNull(cmd);
        this.cmd = cmd;
        return this;
    }

    public String container() {
        return container;
    }

    public Boolean detach() {
        return detach;
    }

    public String[] cmd() {
        return cmd;
    }

}
