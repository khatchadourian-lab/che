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

import org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;
import org.eclipse.che.ide.api.mvp.View;

import java.util.List;

/**
 * View to manage targets.
 *
 * @author Vitaliy Guliy
 */
public interface TargetsView extends View<TargetsView.ActionDelegate> {

    /**
     * Shows Targets dialog.
     */
    void show();

    /**
     * Hides Targets dialog.
     */
    void hide();

    /**
     * Resets the view to its default value.
     */
    void clear();

    /**
     * Shows a list of available recipes.
     *
     * @param recipes
     */
    void showRecipes(List<RecipeDescriptor> recipes);


    void showHintPanel();

    void showInfoPanel();

    void showPropertiesPanel();


    /**
     * Sets target name.
     *
     * @param targetName
     *          target name
     */
    void setTargetName(String targetName);

    /**
     * Sets SSH host value.
     *
     * @param host
     *          host value
     */
    void setHost(String host);

    /**
     * Sets SSH port value.
     *
     * @param port
     *          port value
     */
    void setPort(String port);

    /**
     * Sets SSH user name.
     *
     * @param userName
     *          user name
     */
    void setUserName(String userName);

    /**
     * Sets SSH password.
     *
     * @param password
     *          password
     */
    void setPassword(String password);

    /**
     * Enables or disables Save button.
     *
     * @param enable
     *          enabled state
     */
    void enableSaveButton(boolean enable);

    /**
     * Enables or disables Cancel button.
     *
     * @param enable
     *          enabled state
     */
    void enableCancelButton(boolean enable);


    interface ActionDelegate {

        // Perform actions when clicking Close button
        void onCloseClicked();

        // Perform actions when clicking Add target button
        void onAddTarget(String category);

        // Perform actions when selecting a recipe
        void onRecipeSelected(RecipeDescriptor recipe);

    }

}
