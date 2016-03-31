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
     * Shows a list of available recipes.
     *
     * @param recipes
     */
    void showRecipes(List<RecipeDescriptor> recipes);

    interface ActionDelegate {

        // Perform actions when clicking Close button
        void onCloseClicked();

    }

}
