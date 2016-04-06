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

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.ide.jseditor.client.preference.editorsettings.EditorOptions;

import javax.validation.constraints.NotNull;

/**
 * @author Roman Nikitenko
 */
public class EditorPropertyWidgetImpl extends Composite implements EditorPropertyWidget {
    interface PropertyWidgetImplUiBinder extends UiBinder<Widget, EditorPropertyWidgetImpl> {
    }

    private static final PropertyWidgetImplUiBinder UI_BINDER = GWT.create(PropertyWidgetImplUiBinder.class);

    private EditorOptions optionId;

    String propertyName;

    @UiField
    Label     title;
    @UiField
    FlowPanel valuePanel;

    private ActionDelegate delegate;

    @Inject
    public EditorPropertyWidgetImpl(final EditorPropertyNameManager nameManager,
                                    @Assisted String propertyName,
                                    @Assisted boolean enabled) {
        initWidget(UI_BINDER.createAndBindUi(this));

        this.propertyName = propertyName;

        this.title.setText(propertyName);
        CheckBox propertyValue = new CheckBox();
        propertyValue.setValue(true);
        valuePanel.add(propertyValue);
    }

    /** {@inheritDoc} */
    @Override
    public void selectPropertyValue(@NotNull String value) {
//        for (int i = 0; i < property.getItemCount(); i++) {
//            if (property.getValue(i).equals(value)) {
//                property.setItemSelected(i, true);
//                return;
//            }
//        }
    }

//    @UiHandler("property")
//    public void onPropertyChanged(@SuppressWarnings("UnusedParameters") ChangeEvent event) {
//        delegate.onPropertyChanged();
//    }

    @Override
    public String getSelectedValue() {
//        int index = property.getSelectedIndex();
//
//        return index != -1 ? property.getValue(index) : "";
        return "";
    }

    @Override
    public EditorOptions getOptionId() {
        return optionId;
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(@NotNull ActionDelegate delegate) {
        this.delegate = delegate;
    }
}
