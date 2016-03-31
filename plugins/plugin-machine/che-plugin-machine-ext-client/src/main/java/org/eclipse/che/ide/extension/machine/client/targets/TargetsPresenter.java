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
package org.eclipse.che.ide.extension.machine.client.targets;

import com.google.gwt.core.client.Scheduler;
import com.google.inject.Inject;
import org.eclipse.che.api.machine.gwt.client.MachineServiceClient;
import org.eclipse.che.api.machine.gwt.client.RecipeServiceClient;
import org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.api.app.AppContext;

import java.util.List;

/**
 * Targets manager presenter.
 *
 * @author Vitaliy Guliy
 */
public class TargetsPresenter implements TargetsView.ActionDelegate {

    private final TargetsView           view;
    private final AppContext            appContext;
    private final MachineServiceClient  machineServiceClient;
    private final RecipeServiceClient   recipeServiceClient;

    @Inject
    public TargetsPresenter(final TargetsView view,
                            final AppContext appContext,
                            final MachineServiceClient machineServiceClient,
                            final RecipeServiceClient recipeServiceClient) {
        this.view = view;
        this.appContext = appContext;
        this.machineServiceClient = machineServiceClient;
        this.recipeServiceClient = recipeServiceClient;

        view.setDelegate(this);
    }

    /**
     * Opens Targets popup.
     */
    public void edit() {
        view.show();

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                searchRecipes();
            }
        });
    }

    private void searchRecipes() {
        recipeServiceClient.getAllRecipes().then(new Operation<List<RecipeDescriptor>>() {
            @Override
            public void apply(List<RecipeDescriptor> recipes) throws OperationException {
                view.showRecipes(recipes);
            }
        });
    }

    @Override
    public void onCloseClicked() {
        view.hide();
    }

}
