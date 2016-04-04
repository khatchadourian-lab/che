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
 * Arguments holder for {@code killContainer} method of {@link org.eclipse.che.plugin.docker.client.DockerConnector}.
 *
 * @author Mykola Morhun
 */
public class KillContainerParams {
    /** container identifier, either id or name */
    private String container;

    public KillContainerParams withContainer(String container) {
        this.container = container;
        return this;
    }

    public String getContainer() {
        return container;
    }

}
