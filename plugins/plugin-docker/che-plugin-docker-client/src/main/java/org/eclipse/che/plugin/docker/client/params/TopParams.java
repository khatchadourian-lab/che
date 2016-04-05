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
 * Arguments holder for {@link org.eclipse.che.plugin.docker.client.DockerConnector#top(TopParams)}.
 *
 * @author Mykola Morhun
 */
public class TopParams {

    private String   container;
    private String[] psArgs;

    /**
     * @param container
     *         container id or name
     */
    public TopParams withContainer(String container) {
        this.container = container;
        return this;
    }

    /**
     * @param psArgs
     *         ps arguments to use
     */
    public TopParams withPsArgs(String... psArgs) {
        this.psArgs = psArgs;
        return this;
    }

    public String getContainer() {
        return container;
    }

    public String[] getPsArgs() {
        return psArgs;
    }

}
