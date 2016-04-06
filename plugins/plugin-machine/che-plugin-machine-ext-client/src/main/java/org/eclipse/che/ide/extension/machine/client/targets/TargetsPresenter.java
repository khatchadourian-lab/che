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
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.machine.gwt.client.MachineManager;
import org.eclipse.che.api.machine.gwt.client.MachineServiceClient;
import org.eclipse.che.api.machine.gwt.client.RecipeServiceClient;
import org.eclipse.che.api.machine.shared.dto.LimitsDto;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.MachineSourceDto;
import org.eclipse.che.api.machine.shared.dto.SnapshotDto;
import org.eclipse.che.api.machine.shared.dto.recipe.NewRecipe;
import org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;
import org.eclipse.che.api.machine.shared.dto.recipe.RecipeUpdate;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.gwt.client.WorkspaceServiceClient;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.util.StringUtils;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.workspace.DefaultWorkspaceComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Targets manager presenter.
 *
 * @author Vitaliy Guliy
 */
public class TargetsPresenter implements TargetsView.ActionDelegate {

    private final TargetsView                           view;
    private final RecipeServiceClient                   recipeServiceClient;
    private final DtoFactory                            dtoFactory;
    private final DialogFactory                         dialogFactory;
    private final MachineManager                        machineManager;

    private final List<Target>                          targets = new ArrayList<>();
    private Target                                      selectedTarget;

    @Inject
    public TargetsPresenter(final TargetsView view,
                            final RecipeServiceClient recipeServiceClient,
                            final DtoFactory dtoFactory,
                            final DialogFactory dialogFactory,
                            final MachineManager machineManager) {
        this.view = view;
        this.recipeServiceClient = recipeServiceClient;
        this.dtoFactory = dtoFactory;
        this.dialogFactory = dialogFactory;
        this.machineManager = machineManager;

        view.setDelegate(this);
    }

    /**
     * Opens Targets popup.
     */
    public void edit() {
        view.show();
        view.clear();

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                getTargets();
            }
        });
    }

    /**
     * Fetches all recipes from the server and makes a list of targets.
     */
    private void getTargets() {
        targets.clear();

        recipeServiceClient.getAllRecipes().then(new Operation<List<RecipeDescriptor>>() {
            @Override
            public void apply(List<RecipeDescriptor> recipeList) throws OperationException {
                for (RecipeDescriptor recipe : recipeList) {
                    // Display only "ssh" recipes
                    if (!"ssh".equalsIgnoreCase(recipe.getType())) {
                        continue;
                    }

                    Target target = new Target(recipe.getName(), recipe.getType(), recipe);
                    targets.add(target);

                    try {
                        JSONObject json = JSONParser.parseStrict(recipe.getScript()).isObject();

                        String host = json.get("host").isString().stringValue();
                        String port = json.get("port").isString().stringValue();
                        String username = json.get("username").isString().stringValue();
                        String password = json.get("password").isString().stringValue();

                        target.setHost(host);
                        target.setPort(port);
                        target.setUserName(username);
                        target.setPassword(password);

                    } catch (Exception e) {
                        Log.error(TargetsPresenter.class, "Unable to parse recipe JSON. " + e.getMessage());
                    }

                }
                view.showTargets(targets);
            }
        });
    }

    @Override
    public void onCloseClicked() {
        view.hide();
    }

    @Override
    public void onAddTarget(String category) {
        Target target = new Target("[new target]", "ssh");
        target.setHost("127.0.0.1");
        target.setPort("22");
        target.setUserName("root");
        target.setPassword("root");
        targets.add(target);

        view.showTargets(targets);
        view.selectTarget(target);

        view.enableSaveButton(false);
        view.enableCancelButton(false);
    }

    @Override
    public void onTargetSelected(Target target) {
        if ("docker".equalsIgnoreCase(target.getType())) {
            view.showInfoPanel();

        } else if ("ssh".equalsIgnoreCase(target.getType())) {
            view.showPropertiesPanel();
            view.setTargetName(target.getName());

            view.setHost(target.getHost());
            view.setPort(target.getPort());
            view.setUserName(target.getUserName());
            view.setPassword(target.getPassword());

            view.selectTargetName();
        } else {
            view.showInfoPanel();
        }

        view.enableSaveButton(false);
        view.enableCancelButton(false);

        selectedTarget = target;
    }

    @Override
    public void onTargetNameChanged(String value) {
        selectedTarget.setName(value);
        updateButtons();
    }

    @Override
    public void onHostChanged(String value) {
        selectedTarget.setHost(value);
        updateButtons();
    }

    @Override
    public void onPortChanged(String value) {
        selectedTarget.setPort(value);
        updateButtons();
    }

    @Override
    public void onUserNameChanged(String value) {
        selectedTarget.setUserName(value);
        updateButtons();
    }

    @Override
    public void onPasswordChanged(String value) {
        selectedTarget.setPassword(value);
        updateButtons();
    }

    private void updateButtons() {
        view.enableCancelButton(true);

        if (StringUtils.isNullOrEmpty(view.getTargetName()) ||
                StringUtils.isNullOrEmpty(view.getHost()) ||
                StringUtils.isNullOrEmpty(view.getPort())) {
            view.enableSaveButton(false);
        } else {
            view.enableSaveButton(true);
        }
    }

    @Override
    public void onSaveClicked() {
        // Save only SSH target
        if (!"ssh".equals(selectedTarget.getType())) {
            return;
        }

        if (selectedTarget.getRecipe() == null) {
            createTarget();
        } else {
            updateTarget();
        }
    }

    /**
     * Creates a new target.
     */
    private void createTarget() {
        List<String> tags = new ArrayList<>();
        tags.add("ssh");

        NewRecipe newRecipe = dtoFactory.createDto(NewRecipe.class)
                .withName(selectedTarget.getName())
                .withType("ssh")
                .withScript("{\"" +
                        "host\": \"" + selectedTarget.getHost() + "\", " +
                        "\"port\": \"" + selectedTarget.getPort() + "\", " +
                        "\"username\": \"" + selectedTarget.getUserName() + "\", " +
                        "\"password\": \"" + selectedTarget.getPassword() + "\"}")
                .withTags(tags);

        Promise<RecipeDescriptor> createRecipe = recipeServiceClient.createRecipe(newRecipe);
        createRecipe.then(new Operation<RecipeDescriptor>() {
            @Override
            public void apply(RecipeDescriptor recipe) throws OperationException {
                selectedTarget.setRecipe(recipe);
                view.showTargets(targets);
                view.selectTarget(selectedTarget);

                view.enableSaveButton(false);
                view.enableCancelButton(false);
            }
        });

        createRecipe.catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                dialogFactory.createMessageDialog("Error", arg.toString(), null).show();
            }
        });
    }

    /**
     * Updates as existent target.
     */
    private void updateTarget() {
        RecipeUpdate recipeUpdate = dtoFactory.createDto(RecipeUpdate.class)
                .withId(selectedTarget.getRecipe().getId())
                .withName(view.getUserName())
                .withType(selectedTarget.getRecipe().getType())
                .withTags(selectedTarget.getRecipe().getTags())
                .withDescription(selectedTarget.getRecipe().getDescription())
                .withPermissions(selectedTarget.getRecipe().getPermissions())
                .withScript("{\"" +
                        "host\": \"" + selectedTarget.getHost() + "\", " +
                        "\"port\": \"" + selectedTarget.getPort() + "\", " +
                        "\"username\": \"" + selectedTarget.getUserName() + "\", " +
                        "\"password\": \"" + selectedTarget.getPassword() + "\"}");

        Promise<RecipeDescriptor> updateRecipe = recipeServiceClient.updateRecipe(recipeUpdate);
        updateRecipe.then(new Operation<RecipeDescriptor>() {
            @Override
            public void apply(RecipeDescriptor recipe) throws OperationException {
                selectedTarget.setRecipe(recipe);

                view.showTargets(targets);
                view.selectTarget(selectedTarget);

                view.enableSaveButton(false);
                view.enableCancelButton(false);
            }
        });

        updateRecipe.catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                dialogFactory.createMessageDialog("Error", arg.toString(), null).show();
            }
        });
    }

    @Override
    public void onCancelClicked() {
    }

    @Override
    public void onConnectClicked() {
        if (selectedTarget.getRecipe() == null) {
            return;
        }

        String recipeURl = selectedTarget.getRecipe().getLink("get recipe script").getHref();
        machineManager.startSSHMachine(recipeURl, selectedTarget.getName());
    }

//    private native void log(String msg) /*-{
//        console.log(msg);
//    }-*/;

}
