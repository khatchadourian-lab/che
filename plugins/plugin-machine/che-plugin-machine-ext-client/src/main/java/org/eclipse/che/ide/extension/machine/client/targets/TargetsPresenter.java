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
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.inject.Inject;
import org.eclipse.che.api.machine.gwt.client.MachineServiceClient;
import org.eclipse.che.api.machine.gwt.client.RecipeServiceClient;
import org.eclipse.che.api.machine.shared.dto.recipe.NewRecipe;
import org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.util.loging.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Targets manager presenter.
 *
 * @author Vitaliy Guliy
 */
public class TargetsPresenter implements TargetsView.ActionDelegate {

    private final TargetsView               view;
    private final AppContext                appContext;
    private final MachineServiceClient      machineServiceClient;
    private final RecipeServiceClient       recipeServiceClient;
    private final DtoFactory                dtoFactory;

    private final List<RecipeDescriptor>    recipes = new ArrayList<>();

    @Inject
    public TargetsPresenter(final TargetsView view,
                            final AppContext appContext,
                            final MachineServiceClient machineServiceClient,
                            final RecipeServiceClient recipeServiceClient,
                            final DtoFactory dtoFactory) {
        this.view = view;
        this.appContext = appContext;
        this.machineServiceClient = machineServiceClient;
        this.recipeServiceClient = recipeServiceClient;
        this.dtoFactory = dtoFactory;

        view.setDelegate(this);
    }

    /**
     * Opens Targets popup.
     */
    public void edit() {
        view.show();
        view.clear();

        recipes.clear();

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

    @Override
    public void onAddTarget(String category) {
        if (!"ssh".equalsIgnoreCase(category)) {
        }

        String RECIPE_TYPE = "ssh";

        List<String> tags = new ArrayList<>();
        tags.add("ssh");

        NewRecipe newRecipe = dtoFactory.createDto(NewRecipe.class)
                .withType(RECIPE_TYPE)
                .withScript("{\"host\": \"127.0.0.1\", \"port\": \"22\", \"username\": \"\", \"password\": \"\"}")
                .withName("Localhost")
                .withTags(tags);

        Promise<RecipeDescriptor> createRecipe = recipeServiceClient.createRecipe(newRecipe);
        createRecipe.then(new Operation<RecipeDescriptor>() {
            @Override
            public void apply(RecipeDescriptor recipeDescriptor) throws OperationException {
            }
        });

        createRecipe.catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                Log.error(TargetsPresenter.class, "Unable to create recipe");
            }
        });
    }

    @Override
    public void onRecipeSelected(RecipeDescriptor recipe) {
        // Enable fields only for SSH recipe
        if (!"ssh".equalsIgnoreCase(recipe.getType())) {
            view.showInfoPanel();
            return;
        }

        view.showPropertiesPanel();
        view.setTargetName(recipe.getName());

        try {
            JSONObject json = JSONParser.parseStrict(recipe.getScript()).isObject();
            String host = json.get("host").isString().stringValue();
            String port = json.get("port").isString().stringValue();
            String username = json.get("username").isString().stringValue();
            String password = json.get("password").isString().stringValue();

            view.setHost(host);
            view.setPort(port);
            view.setUserName(username);
            view.setPassword(password);
        } catch (Exception e) {
            view.setHost("");
            view.setPort("");
            view.setUserName("");
            view.setPassword("");
        }

        view.enableSaveButton(false);
        view.enableCancelButton(false);
    }

    private native void log(String msg) /*-{
        console.log(msg);
    }-*/;

}
