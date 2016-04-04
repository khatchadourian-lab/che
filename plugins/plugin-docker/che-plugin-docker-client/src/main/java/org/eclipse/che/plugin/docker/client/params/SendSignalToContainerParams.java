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
 * Arguments holder for {@link org.eclipse.che.plugin.docker.client.DockerConnector#sendSignalToContainer(SendSignalToContainerParams)}.
 *
 * @author Mykola Morhun
 */
public class SendSignalToContainerParams {

    private String container;
    private Integer signal;

    /**
     * @param container
     *         container identifier, either id or name
     */
    public SendSignalToContainerParams withContainer(String container) {
        this.container = container;
        return this;
    }

    /**
     * @param signal
     *         code of signal, e.g. 9 in case of SIGKILL
     */
    public SendSignalToContainerParams withSignal(int signal) {
        this.signal = signal;
        return this;
    }

    public String getContainer() {
        return container;
    }

    public Integer getSignal() {
        return signal;
    }

}
