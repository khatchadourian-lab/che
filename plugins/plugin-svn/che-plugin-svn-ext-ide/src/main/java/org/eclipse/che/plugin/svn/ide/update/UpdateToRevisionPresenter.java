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
package org.eclipse.che.plugin.svn.ide.update;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.extension.machine.client.processes.ConsolesPanelPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.plugin.svn.ide.SubversionClientService;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.common.SubversionOutputConsoleFactory;

/**
 * Handler for the {@link org.eclipse.che.plugin.svn.ide.action.UpdateToRevisionAction} action.
 */
@Singleton
public class UpdateToRevisionPresenter extends UpdatePresenter implements UpdateToRevisionView.ActionDelegate {

    private final UpdateToRevisionView view;

    @Inject
    public UpdateToRevisionPresenter(final AppContext appContext,
                                     final DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                     final EventBus eventBus,
                                     final NotificationManager notificationManager,
                                     final SubversionOutputConsoleFactory consoleFactory,
                                     final SubversionClientService service,
                                     final SubversionExtensionLocalizationConstants constants,
                                     final ConsolesPanelPresenter consolesPanelPresenter,
                                     final UpdateToRevisionView view,
                                     final ProjectExplorerPresenter projectExplorerPart) {
        super(appContext, dtoUnmarshallerFactory, eventBus, consoleFactory, service, consolesPanelPresenter, constants,
              notificationManager, projectExplorerPart);

        this.view = view;

        this.view.setDelegate(this);
    }

    /**
     * Displays the dialog and resets its state.
     */
    public void showWindow() {
        view.setDepth("infinity");
        view.setRevision("");
        view.setIgnoreExternals(false);
        view.setIsCustomRevision(false);
        view.setIsHeadRevision(true);

        view.setEnableUpdateButton(true);
        view.setEnableCustomRevision(false);

        view.showWindow();
    }

    @Override
    public void onCancelClicked() {
        view.close();
    }

    @Override
    public void onUpdateClicked() {
        doUpdate(view.getRevision(), view.getDepth(), view.ignoreExternals(), view);
    }

    @Override
    public void onRevisionTypeChanged() {
        handleFormChange();
    }

    @Override
    public void onRevisionChanged() {
        handleFormChange();
    }

    /**
     * Helper method to enable/disable form fields based on form state changes.
     */
    private void handleFormChange() {
        view.setEnableCustomRevision(view.isCustomRevision());

        if (view.isCustomRevision() && view.getRevision().isEmpty()) {
            view.setEnableUpdateButton(false);
        } else {
            view.setEnableUpdateButton(true);
        }
    }

}
