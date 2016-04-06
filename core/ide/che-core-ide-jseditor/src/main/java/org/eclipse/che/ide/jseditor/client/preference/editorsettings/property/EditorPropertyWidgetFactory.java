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

import javax.validation.constraints.NotNull;

/**
 * The factory which creates instances of {@link EditorPropertyWidget}.
 *
 * @author Roman Nikitenko
 */
public interface EditorPropertyWidgetFactory {

    /**
     * Creates new instances of {@link EditorPropertyWidget}.
     *
     * @param optionId
     *         property id which need set to property. Each property has unique id which we get from server.
     * @return an instance of {@link EditorPropertyWidget}
     */
    EditorPropertyWidget create(@NotNull String propertyName, boolean enabled);
}
