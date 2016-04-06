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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.jseditor.client.JsEditorConstants;
import org.eclipse.che.ide.jseditor.client.preference.editorsettings.EditorOptions;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.eclipse.che.ide.jseditor.client.preference.editorsettings.EditorOptions.AUTO_COMPLETE_COMMENTS;
import static org.eclipse.che.ide.jseditor.client.preference.editorsettings.EditorOptions.AUTO_PAIR_ANGLE_BRACKETS;
import static org.eclipse.che.ide.jseditor.client.preference.editorsettings.EditorOptions.AUTO_PAIR_BRACES;
import static org.eclipse.che.ide.jseditor.client.preference.editorsettings.EditorOptions.AUTO_PAIR_PARENTHESES;
import static org.eclipse.che.ide.jseditor.client.preference.editorsettings.EditorOptions.AUTO_PAIR_QUOTATIONS;
import static org.eclipse.che.ide.jseditor.client.preference.editorsettings.EditorOptions.AUTO_PAIR_SQUARE_BRACKETS;
import static org.eclipse.che.ide.jseditor.client.preference.editorsettings.EditorOptions.EXPAND_TAB;
import static org.eclipse.che.ide.jseditor.client.preference.editorsettings.EditorOptions.SHOW_ANNOTATION_RULER;
import static org.eclipse.che.ide.jseditor.client.preference.editorsettings.EditorOptions.SHOW_CONTENT_ASSIST_AUTOMATICALLY;
import static org.eclipse.che.ide.jseditor.client.preference.editorsettings.EditorOptions.SHOW_FOLDING_RULER;
import static org.eclipse.che.ide.jseditor.client.preference.editorsettings.EditorOptions.SHOW_LINE_NUMBER_RULER;
import static org.eclipse.che.ide.jseditor.client.preference.editorsettings.EditorOptions.SHOW_OCCURRENCES;
import static org.eclipse.che.ide.jseditor.client.preference.editorsettings.EditorOptions.SHOW_OVERVIEW_RULER;
import static org.eclipse.che.ide.jseditor.client.preference.editorsettings.EditorOptions.SHOW_ZOOM_RULER;
import static org.eclipse.che.ide.jseditor.client.preference.editorsettings.EditorOptions.SMART_INDENTATION;
import static org.eclipse.che.ide.jseditor.client.preference.editorsettings.EditorOptions.TAB_SIZE;
/**
 * The class contains editor properties IDs which match to properties' names.
 *
 * @author Roman Nikitenko
 */

@Singleton
public class EditorPropertyNameManager {

    private final Map<EditorOptions, String> names;

    @Inject
    public EditorPropertyNameManager(JsEditorConstants locale) {
        names = new HashMap<>();

        names.put(EXPAND_TAB, locale.propertyExpandTab());
        names.put(TAB_SIZE, locale.propertyTabSize());
        names.put(AUTO_PAIR_PARENTHESES, locale.propertyAutoPairParentheses());
        names.put(AUTO_PAIR_BRACES, locale.propertyAutoPairBraces());
        names.put(AUTO_PAIR_SQUARE_BRACKETS, locale.propertyAutoPairSquareBrackets());
        names.put(AUTO_PAIR_ANGLE_BRACKETS, locale.propertyAutoPairAngelBrackets());
        names.put(AUTO_PAIR_QUOTATIONS, locale.propertyAutoPairQuotations());
        names.put(AUTO_COMPLETE_COMMENTS, locale.propertyAutoCompleteComments());
        names.put(SMART_INDENTATION, locale.propertySmartIndentation());
        names.put(SHOW_ANNOTATION_RULER, locale.propertyShowAnnotationRuler());
        names.put(SHOW_LINE_NUMBER_RULER, locale.propertyShowLineNumberRuler());
        names.put(SHOW_FOLDING_RULER, locale.propertyShowFoldingRuler());
        names.put(SHOW_OVERVIEW_RULER, locale.propertyShowOverviewRuler());
        names.put(SHOW_ZOOM_RULER, locale.propertyShowZoomRuler());
        names.put(SHOW_OCCURRENCES, locale.propertyShowOccurrences());
        names.put(SHOW_CONTENT_ASSIST_AUTOMATICALLY, locale.propertyShowContentAssistAutomatically());
    }

    /**
     * Returns property name using special id. Method can throw {@link IllegalArgumentException} if name not found.
     *
     * @param propertyId
     *         id for which name will be returned
     * @return name of property
     */
    @NotNull
    public String getName(@NotNull EditorOptions propertyId) {
        String name = names.get(propertyId);

        if (name == null) {
            throw new IllegalArgumentException(getClass() + "property name is not found...");
        }

        return name;
    }

    public Set<EditorOptions> getKeys() {
        return names.keySet();
    }

    public Collection<String> getNames() {
        return names.values();
    }
}
