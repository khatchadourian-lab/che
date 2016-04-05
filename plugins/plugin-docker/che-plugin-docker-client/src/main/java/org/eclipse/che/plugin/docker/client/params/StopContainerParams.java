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

import java.util.concurrent.TimeUnit;

/**
 * Arguments holder for {@link org.eclipse.che.plugin.docker.client.DockerConnector#stopContainer(StopContainerParams)}.
 *
 * @author Mykola Morhun
 */
public class StopContainerParams {

    private String   container;
    private Long     timeout;
    private TimeUnit timeunit;

    /**
     * @param container
     *         container identifier, either id or name
     */
    public StopContainerParams withContainer(String container) {
        this.container = container;
        return this;
    }

    /**
     * @param timeout
     *         time in seconds to wait for the container to stop before killing it
     */
    public StopContainerParams withTimeout(long timeout) {
        withTimeout(timeout, TimeUnit.SECONDS);
        return this;
    }

    /**
     * @param timeout
     *         time to wait for the container to stop before killing it
     * @param timeunit
     *         time unit of the timeout parameter
     */
    public StopContainerParams withTimeout(long timeout, TimeUnit timeunit) {
        this.timeout = timeout;
        this.timeunit = timeunit;
        return this;
    }

    public String getContainer() {
        return container;
    }

    public Long getTimeout() {
        return timeout;
    }

    public TimeUnit getTimeunit() {
        return timeunit;
    }

}
