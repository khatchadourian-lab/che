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
 * Arguments holder for {@code stopContainer} method of {@link org.eclipse.che.plugin.docker.client.DockerConnector}.
 *
 * @author Mykola Morhun
 */
public class StopContainerParams {
    /** container identifier, either id or name */
    private String   container;
    /** time to wait for the container to stop before killing it */
    private Long     timeout;
    /** time unit of the timeout parameter */
    private TimeUnit timeunit;

    public StopContainerParams withContainer(String container) {
        this.container = container;
        return this;
    }

    public StopContainerParams withTimeout(Long timeout) {
        withTimeout(timeout, TimeUnit.SECONDS);
        return this;
    }

    public StopContainerParams withTimeout(Long timeout, TimeUnit timeunit) {
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
