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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Button;
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
import java.util.List;

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

    private Button                  closeButton;

    @Inject
    public TargetsViewImpl(org.eclipse.che.ide.Resources resources,
                           MachineLocalizationConstant machineLocale,
                           TargetsViewImplUiBinder uiBinder,
                           CoreLocalizationConstant coreLocale,
                           EditCommandResources commandResources,
                           IconRegistry iconRegistry) {
        this.machineLocale = machineLocale;
        this.commandResources = commandResources;
        this.iconRegistry = iconRegistry;

        setWidget(uiBinder.createAndBindUi(this));
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
    }

    @Override
    public void showRecipes(List<RecipeDescriptor> recipes) {
        final List<Category<?>> categoriesList = new ArrayList<>();

        Category<RecipeDescriptor> runtimesCategory =
                new Category<>("CHE RUNTIMES", categoriesRenderer, recipes, categoriesEventDelegate);
        categoriesList.add(runtimesCategory);

        List<RecipeDescriptor> sshRecipes = new ArrayList<>();

        Category<RecipeDescriptor> sshCategory =
                new Category<>("SSH SERVERS", categoriesRenderer, sshRecipes, categoriesEventDelegate);
        categoriesList.add(sshCategory);

        list.clear();
        list.render(categoriesList);
    }

    private SpanElement renderCategoryHeader(Category<RecipeDescriptor> category) {
        SpanElement categoryHeaderElement = Document.get().createSpanElement();
        categoryHeaderElement.setClassName(commandResources.getCss().categoryHeader());

        SpanElement iconElement = Document.get().createSpanElement();
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
            }
        });

//        Icon icon = iconRegistry.getIconIfExist(".commands.category.icon");
        Icon icon = iconRegistry.getIconIfExist("custom.commands.category.icon");
        if (icon != null) {
            final SVGImage iconSVG = icon.getSVGImage();
            if (iconSVG != null) {
                iconElement.appendChild(iconSVG.getElement());
                return categoryHeaderElement;
            }
        }

        return categoryHeaderElement;
    }


    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
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
                }
            };

}
