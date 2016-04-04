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

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import elemental.events.KeyboardEvent;
import org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.command.edit.EditCommandResources;
import org.eclipse.che.ide.ui.list.CategoriesList;
import org.eclipse.che.ide.ui.list.Category;
import org.eclipse.che.ide.ui.list.CategoryRenderer;
import org.eclipse.che.ide.ui.window.Window;
import org.vectomatic.dom.svg.ui.SVGImage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Vitaliy Guliy
 */
@Singleton
public class TargetsViewImpl extends Window implements TargetsView {

    interface TargetsViewImplUiBinder extends UiBinder<Widget, TargetsViewImpl> {
    }

    private EditCommandResources    commandResources;
    private IconRegistry            iconRegistry;
    private ActionDelegate          delegate;

    @UiField(provided = true)
    MachineLocalizationConstant     machineLocale;

    @UiField
    TextBox                         filterTargets;

    @UiField
    SimplePanel                     targetsPanel;

    private CategoriesList          list;

    @UiField
    FlowPanel                       hintPanel;

    @UiField
    FlowPanel                       infoPanel;

    @UiField
    FlowPanel                       propertiesPanel;

    @UiField
    TextBox                         targetName;

    @UiField
    TextBox                         host;

    @UiField
    TextBox                         port;

    @UiField
    TextBox                         userName;

    @UiField
    TextBox                         password;

    @UiField
    FlowPanel                       footer;

    private Button                  closeButton;

    private Button                  saveButton;
    private Button                  cancelButton;

    @Inject
    public TargetsViewImpl(org.eclipse.che.ide.Resources resources,
                           MachineLocalizationConstant machineLocale,
                           CoreLocalizationConstant coreLocale,
                           EditCommandResources commandResources,
                           IconRegistry iconRegistry,
                           TargetsViewImplUiBinder uiBinder) {
        this.machineLocale = machineLocale;
        this.commandResources = commandResources;
        this.iconRegistry = iconRegistry;

        setWidget(uiBinder.createAndBindUi(this));
        getWidget().getElement().getStyle().setPadding(0, Style.Unit.PX);
        setTitle(machineLocale.targetsViewTitle());

        filterTargets.getElement().setAttribute("placeholder", machineLocale.editCommandsViewPlaceholder());
        filterTargets.getElement().addClassName(commandResources.getCss().filterPlaceholder());

        list = new CategoriesList(resources);
        list.addDomHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                switch (event.getNativeKeyCode()) {
                    case KeyboardEvent.KeyCode.INSERT:
                        break;
                    case KeyboardEvent.KeyCode.DELETE:
                        break;
                }
            }
        }, KeyDownEvent.getType());
        targetsPanel.add(list);

        closeButton = createButton(coreLocale.close(), "targets.button.close",
                new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        delegate.onCloseClicked();
                    }
                });
        addButtonToFooter(closeButton);

        saveButton = createButton(coreLocale.save(), "targets.button.save", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
            }
        });
        saveButton.addStyleName(this.resources.windowCss().primaryButton());
        footer.add(saveButton);

        cancelButton = createButton(coreLocale.cancel(), "targets.button.cancel", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
            }
        });
        footer.add(cancelButton);
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void clear() {
        list.clear();

        hintPanel.setVisible(true);
        infoPanel.setVisible(false);
        propertiesPanel.setVisible(false);
    }

    @Override
    public void showHintPanel() {
        hintPanel.setVisible(true);
        infoPanel.setVisible(false);
        propertiesPanel.setVisible(false);
    }

    @Override
    public void showInfoPanel() {
        hintPanel.setVisible(false);
        infoPanel.setVisible(true);
        propertiesPanel.setVisible(false);
    }

    @Override
    public void showPropertiesPanel() {
        hintPanel.setVisible(false);
        infoPanel.setVisible(false);
        propertiesPanel.setVisible(true);
    }

    @Override
    public void showRecipes(List<RecipeDescriptor> recipes) {
        HashMap<String, List<RecipeDescriptor>> categories = new HashMap<>();
        for (RecipeDescriptor recipe : recipes) {
            List<RecipeDescriptor> categoryRecipes = categories.get(recipe.getType());
            if (categoryRecipes == null) {
                categoryRecipes = new ArrayList<>();
                categories.put(recipe.getType(), categoryRecipes);
            }
            categoryRecipes.add(recipe);
        }

        List<Category<?>> categoriesList = new ArrayList<>();
        for (Map.Entry<String, List<RecipeDescriptor>> entry : categories.entrySet()) {
            categoriesList.add(new Category<>(entry.getKey(), categoriesRenderer, entry.getValue(), categoriesEventDelegate));
        }

        ensureSSHCategoryExists(categoriesList);

        list.clear();
        list.render(categoriesList);
    }

    private void ensureSSHCategoryExists(List<Category<?>> categoriesList) {
        for (Category<?> category : categoriesList) {
            if ("ssh".equalsIgnoreCase(category.getTitle())) {
                return;
            }
        }

        categoriesList.add(new Category<>("ssh", categoriesRenderer, new ArrayList<RecipeDescriptor>(), categoriesEventDelegate));
    }

    private SpanElement renderCategoryHeader(final Category<RecipeDescriptor> category) {
        SpanElement categoryHeaderElement = Document.get().createSpanElement();
        categoryHeaderElement.setClassName(commandResources.getCss().categoryHeader());

        SpanElement iconElement = Document.get().createSpanElement();
        iconElement.getStyle().setPaddingRight(4, Style.Unit.PX);
        iconElement.getStyle().setPaddingLeft(2, Style.Unit.PX);
        categoryHeaderElement.appendChild(iconElement);

        SpanElement textElement = Document.get().createSpanElement();
        categoryHeaderElement.appendChild(textElement);
        textElement.setInnerText(category.getTitle());

        SpanElement buttonElement = Document.get().createSpanElement();
        buttonElement.appendChild(commandResources.addCommandButton().getSvg().getElement());
        categoryHeaderElement.appendChild(buttonElement);

        Event.sinkEvents(buttonElement, Event.ONCLICK);
        Event.setEventListener(buttonElement, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                event.stopPropagation();
                event.preventDefault();
                delegate.onAddTarget(category.getTitle());
            }
        });

        Icon icon = iconRegistry.getIconIfExist(category.getTitle() + ".runtime.icon");
        if (icon != null) {
            final SVGImage iconSVG = icon.getSVGImage();
            if (iconSVG != null) {
                iconElement.appendChild(iconSVG.getElement());
                return categoryHeaderElement;
            }
        }

        return categoryHeaderElement;
    }

    private final CategoryRenderer<RecipeDescriptor> categoriesRenderer =
            new CategoryRenderer<RecipeDescriptor>() {
                @Override
                public void renderElement(Element element, RecipeDescriptor data) {
                    element.setInnerText(data.getName());
                }

                @Override
                public SpanElement renderCategory(Category<RecipeDescriptor> category) {
                    return renderCategoryHeader(category);
                }
            };

    private final Category.CategoryEventDelegate<RecipeDescriptor> categoriesEventDelegate =
            new Category.CategoryEventDelegate<RecipeDescriptor>() {
                @Override
                public void onListItemClicked(Element listItemBase, RecipeDescriptor itemData) {
                    delegate.onRecipeSelected(itemData);
                }
            };

    private native void log(String msg) /*-{
        console.log(msg);
    }-*/;

    @Override
    public void setTargetName(String targetName) {
        this.targetName.setValue(targetName);
    }

    @Override
    public void setHost(String host) {
        this.host.setValue(host);
    }

    @Override
    public void setPort(String port) {
        this.port.setValue(port);
    }

    @Override
    public void setUserName(String userName) {
        this.userName.setValue(userName);
    }

    @Override
    public void setPassword(String password) {
        this.password.setValue(password);
    }

    @Override
    public void enableSaveButton(boolean enable) {
        saveButton.setEnabled(enable);
    }

    @Override
    public void enableCancelButton(boolean enable) {
        cancelButton.setEnabled(enable);
    }

}
