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
package org.eclipse.che.ide.jseditor.client.preference.editorsettings;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.machine.gwt.client.events.WsAgentStateEvent;
import org.eclipse.che.api.machine.gwt.client.events.WsAgentStateHandler;
import org.eclipse.che.ide.jseditor.client.editortype.EditorType;
import org.eclipse.che.ide.jseditor.client.editortype.EditorTypeRegistry;
import org.eclipse.che.ide.jseditor.client.keymap.Keymap;
import org.eclipse.che.ide.jseditor.client.preference.EditorPreferenceSection;
import org.eclipse.che.ide.jseditor.client.preference.editorsettings.property.EditorPropertyNameManager;
import org.eclipse.che.ide.jseditor.client.preference.keymaps.KeymapsPreferenceView;
import org.eclipse.che.ide.util.loging.Log;

/** Presenter for the editor propertiesPanel section in the 'Preferences' menu. */
public class EditorPropertiesSectionPresenter implements EditorPreferenceSection, EditorPropertiesSectionView.ActionDelegate,
                                                         WsAgentStateHandler {

    private final EditorPropertiesSectionView view;

    private final EventBus         eventBus;

    /** Has any of the keymap preferences been changed ? */
    private boolean dirty = false;

    /** The preference page presenter. */
    private ParentPresenter parentPresenter;

    private final EditorTypeRegistry editorTypeRegistry;

    private final EditorPropertyNameManager editorPropertyNameManager;

    @Inject
    public EditorPropertiesSectionPresenter(final EditorPropertiesSectionView view,
                                            final EventBus eventBus,
                                            final EditorTypeRegistry editorTypeRegistry,
                                            final EditorPropertyNameManager editorPropertyNameManager) {
        this.view = view;
        this.eventBus = eventBus;
        this.editorTypeRegistry = editorTypeRegistry;
        this.editorPropertyNameManager = editorPropertyNameManager;

        eventBus.addHandler(WsAgentStateEvent.TYPE, this);

//        this.view.setDelegate(this);
    }

    @Override
    public void storeChanges() {
//        keymapPrefReader.storePrefs(this.keymapValuesHolder);
//        for (final Entry<EditorType, Keymap> entry : this.keymapValuesHolder) {
//            this.eventBus.fireEvent(new KeymapChangeEvent(entry.getKey().getEditorTypeKey(), entry.getValue().getKey()));
//        }
//        dirty = false;
    }

    @Override
    public void refresh() {
//        readPreferenceFromPreferenceManager();
//        view.refresh();
    }

    protected void readPreferenceFromPreferenceManager() {
//        keymapPrefReader.readPref(prefKeymaps);
//        // init the default keymap
//        for (EditorType editorType : editorTypeRegistry.getEditorTypes()) {
//            List<Keymap> editorKeymaps = Keymap.getInstances(editorType);
//            if (editorKeymaps.size() > 0) {
//                keymapValuesHolder.setKeymap(editorType, editorKeymaps.get(0));
//            }
//        }
//        for (final Entry<EditorType, Keymap> entry : prefKeymaps) {
//            keymapValuesHolder.setKeymap(entry.getKey(), entry.getValue());
//        }
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void go(final AcceptsOneWidget container) {
        container.setWidget(view);
    }

    @Override
    public void setParent(final ParentPresenter parent) {
        this.parentPresenter = parent;
    }

//    @Override
    public void editorKeymapChanged(final EditorType editorType, final Keymap keymap) {
//        if (editorType == null || keymap == null) {
//            return;
//        }
//
//        dirty = false;
//        for (final Entry<EditorType, Keymap> entry : this.keymapValuesHolder) {
//            final Keymap prefKeymap = prefKeymaps.getKeymap(entry.getKey());
//            if (entry.getValue() == null) {
//                dirty = (prefKeymap != null);
//            } else {
//                dirty = !(entry.getValue().equals(prefKeymap));
//            }
//
//            if (dirty) {
//                break;
//            }
//        }
//
//        parentPresenter.signalDirtyState();
    }

    @Override
    public void onWsAgentStarted(WsAgentStateEvent event) {
        Log.error(getClass(), "=== onWsAgentStarted === ");
        addEditorPropertiesPanel();
    }

    @Override
    public void onWsAgentStopped(WsAgentStateEvent event) {

    }

    private void addEditorPropertiesPanel() {
        for (String propertyName : editorPropertyNameManager.getNames()) {
            view.addProperty(propertyName);
        }
    }

    @Override
    public void onPropertyChanged() {
        parentPresenter.signalDirtyState();
    }
}
