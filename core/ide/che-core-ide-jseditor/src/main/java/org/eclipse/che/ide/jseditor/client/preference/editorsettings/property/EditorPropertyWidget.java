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
package org.eclipse.che.ide.jseditor.client.preference.editorsettings.property;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.jseditor.client.preference.editorsettings.EditorOptions;

import javax.validation.constraints.NotNull;

/**
 * The interface provides methods to control property's widget which contains name and value of property.
 *
 * @author Roman Nikitenko
 */
public interface EditorPropertyWidget extends View<EditorPropertyWidget.ActionDelegate> {

    /**
     * Selects need values in list box.
     *
     * @param value
     *         value which will be selected
     */
    void selectPropertyValue(@NotNull String value);

    /**
     *@return property value selected in the property widget
     */
    String getSelectedValue();

    /**
     * @return unique error(warning) options id for the property widget.
     */
    EditorOptions getOptionId();

    interface ActionDelegate {
        /**
         * Performs some action when user change value of property.
         */
        void onPropertyChanged();
    }
}
