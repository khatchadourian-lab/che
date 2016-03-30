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
package org.eclipse.che.api.workspace.server.event;

import org.eclipse.che.api.core.model.workspace.UsersWorkspace;
import org.eclipse.che.api.core.notification.EventOrigin;

/**
 * Informs about workspace creation.
 *
 * @author Sergii Leschenko
 */
@EventOrigin("workspace")
public class WorkspaceCreatedEvent {
    private final UsersWorkspace workspace;

    public WorkspaceCreatedEvent(UsersWorkspace workspace) {
        this.workspace = workspace;
    }

    public UsersWorkspace getWorkspace() {
        return workspace;
    }
}
