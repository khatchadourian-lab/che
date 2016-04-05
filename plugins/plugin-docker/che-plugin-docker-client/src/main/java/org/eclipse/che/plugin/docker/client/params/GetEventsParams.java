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
import org.eclipse.che.plugin.docker.client.json.Filters;

/**
 * Arguments holder for {@link org.eclipse.che.plugin.docker.client.DockerConnector#getEvents(GetEventsParams, MessageProcessor)}.
 *
 * @author Mykola Morhun
 */
public class GetEventsParams {

    private Long    sinceSecond;
    private Long    untilSecond;
    private Filters filters;

    /**
     * @param sinceSecond
     *         UNIX date in seconds. Allow omit events created before specified date
     */
    public GetEventsParams withSinceSecond(long sinceSecond) {
        this.sinceSecond = sinceSecond;
        return this;
    }

    /**
     * @param untilSecond
     *         UNIX date in seconds. Allow omit events created after specified date
     */
    public GetEventsParams withUntilSecond(long untilSecond) {
        this.untilSecond = untilSecond;
        return this;
    }

    /**
     * @param filters
     *         filter of needed events. Available filters: {@code event=<string>}
     *         {@code image=<string>} {@code container=<string>}
     */
    public GetEventsParams withFilters(Filters filters) {
        this.filters = filters;
        return this;
    }

    public Long getSinceSecond() {
        return sinceSecond;
    }

    public Long getUntilSecond() {
        return untilSecond;
    }

    public Filters getFilters() {
        return filters;
    }

}
