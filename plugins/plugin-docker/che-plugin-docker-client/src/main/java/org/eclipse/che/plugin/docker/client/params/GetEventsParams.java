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

import org.eclipse.che.plugin.docker.client.json.Filters;

/**
 * Arguments holder for {@code getEvents} method of {@link org.eclipse.che.plugin.docker.client.DockerConnector}.
 *
 * @author Mykola Morhun
 */
public class GetEventsParams {
    /** UNIX date in seconds. Allow omit events created before specified date */
    private Long sinceSecond;
    /** UNIX date in seconds. Allow omit events created after specified date */
    private Long untilSecond;
    /**
     * filter of needed events. Available filters: {@code event=<string>}
     * {@code image=<string>} {@code container=<string>}
     */
    private Filters filters;

    public GetEventsParams withSinceSecond(Long sinceSecond) {
        this.sinceSecond = sinceSecond;
        return this;
    }

    public GetEventsParams withUntilSecond(Long untilSecond) {
        this.untilSecond = untilSecond;
        return this;
    }

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
