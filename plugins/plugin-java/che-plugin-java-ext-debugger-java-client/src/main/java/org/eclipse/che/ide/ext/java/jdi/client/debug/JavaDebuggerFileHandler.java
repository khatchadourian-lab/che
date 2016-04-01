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
package org.eclipse.che.ide.ext.java.jdi.client.debug;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.api.project.node.Node;
import org.eclipse.che.ide.api.project.node.settings.NodeSettings;
import org.eclipse.che.ide.api.project.tree.VirtualFile;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.navigation.service.JavaNavigationService;
import org.eclipse.che.ide.ext.java.client.project.node.JavaNodeManager;
import org.eclipse.che.ide.ext.java.client.project.node.jar.JarFileNode;
import org.eclipse.che.ide.ext.java.jdi.shared.Location;
import org.eclipse.che.ide.ext.java.shared.JarEntry;
import org.eclipse.che.ide.ext.java.shared.OpenDeclarationDescriptor;
import org.eclipse.che.ide.jseditor.client.document.Document;
import org.eclipse.che.ide.jseditor.client.text.TextPosition;
import org.eclipse.che.ide.jseditor.client.texteditor.TextEditor;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.project.node.FileReferenceNode;

import javax.validation.constraints.NotNull;

import static org.eclipse.che.ide.api.event.FileEvent.FileOperation.OPEN;

/**
 * Responsible to open files in editor when debugger stopped at breakpoint.
 *
 * @author Anatoliy Bazko
 */
public class JavaDebuggerFileHandler {

    private final DebuggerManager          debuggerManager;
    private final EditorAgent              editorAgent;
    private final DtoFactory               dtoFactory;
    private final EventBus                 eventBus;
    private final JavaNodeManager          javaNodeManager;
    private final ProjectExplorerPresenter projectExplorer;
    private final JavaNavigationService    javaNavigationService;

    @Inject
    public JavaDebuggerFileHandler(DebuggerManager debuggerManager,
                                   EditorAgent editorAgent,
                                   DtoFactory dtoFactory,
                                   EventBus eventBus,
                                   JavaNodeManager javaNodeManager,
                                   ProjectExplorerPresenter projectExplorer,
                                   JavaNavigationService javaNavigationService) {
        this.debuggerManager = debuggerManager;
        this.editorAgent = editorAgent;
        this.dtoFactory = dtoFactory;
        this.eventBus = eventBus;
        this.javaNodeManager = javaNodeManager;
        this.projectExplorer = projectExplorer;
        this.javaNavigationService = javaNavigationService;
    }

    /**
     * Open java class by breakpoint {@code location}, scroll to the debug line and do some actions from {@code callback}
     * @param location of the next debug line
     * @param projectConfig project configuration of the project connected to debug session
     * @param callback some action which should be performed after opening file
     */
    public void openFile(final Location location, final ProjectConfigDto projectConfig, final AsyncCallback<VirtualFile> callback) {
        if (debuggerManager.getActiveDebugger() != debuggerManager.getDebugger(JavaDebugger.LANGUAGE)) {
            callback.onFailure(null);
            return;
        }
        VirtualFile activeFile = null;
        final EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
        if (activeEditor != null) {
            activeFile = activeEditor.getEditorInput().getFile();
        }
        final String className = location.getClassName();
        final int lineNumber = location.getLineNumber();

        if (activeFile == null || !className.equals(activeFile.getPath())) {
        javaNavigationService.findDeclaration(projectConfig.getPath(), location.getClassName())
                             .then(new Operation<OpenDeclarationDescriptor>() {
                                 @Override
                                 public void apply(OpenDeclarationDescriptor declaration) throws OperationException {
                                     if (declaration.isBinary()) {
                                         openExternalResource(location, projectConfig, callback, declaration.getLibId());
                                     } else {
                                         openFile(declaration.getPath(), location.getLineNumber(),  callback);
                                     }
                                 }
                             })
                             .catchError(new Operation<PromiseError>() {
                                 @Override
                                 public void apply(PromiseError arg) throws OperationException {
                                     openExternalResource(location, projectConfig, callback, null);
                                 }
                             });
        } else {
            scrollEditorToExecutionPoint((TextEditor)activeEditor, lineNumber);
            callback.onSuccess(activeFile);
        }
    }

    /**
     * Tries to open file from the project.
     * If fails then method will try to find resource from external dependencies.
     */
    private void openFile(@NotNull String filePath, final int debugLine, final AsyncCallback<VirtualFile> callback) {
        projectExplorer.getNodeByPath(new HasStorablePath.StorablePath(filePath)).then(new Operation<Node>() {
            @Override
            public void apply(final Node node) throws OperationException {
                if (!(node instanceof FileReferenceNode)) {
                    return;
                }
                handleActivateFile((VirtualFile)node, callback, debugLine);
                eventBus.fireEvent(new FileEvent((VirtualFile)node, OPEN));
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                callback.onFailure(arg.getCause());
            }
        });
    }

    private void openExternalResource(final Location location,
                                      final ProjectConfigDto project,
                                      final AsyncCallback<VirtualFile> callback,
                                      Integer libId) {
        String className = location.getClassName();
        NodeSettings nodeSettings = javaNodeManager.getJavaSettingsProvider().getSettings();

        JarEntry jarEntry = dtoFactory.createDto(JarEntry.class);
        jarEntry.setPath(className);
        jarEntry.setName(className.substring(className.lastIndexOf(".") + 1) + ".class");
        jarEntry.setType(JarEntry.JarEntryType.CLASS_FILE);

        final JarFileNode jarFileNode = javaNodeManager.getJavaNodeFactory().newJarFileNode(jarEntry, libId, project, nodeSettings);

        handleActivateFile(jarFileNode, callback, location.getLineNumber());
        eventBus.fireEvent(new FileEvent(jarFileNode, OPEN));
    }

    public void handleActivateFile(final VirtualFile virtualFile, final AsyncCallback<VirtualFile> callback, final int debugLine) {
        editorAgent.openEditor(virtualFile, new EditorAgent.OpenEditorCallback() {
            @Override
            public void onEditorOpened(final EditorPartPresenter editor) {
                new Timer() {
                    @Override
                    public void run() {
                        scrollEditorToExecutionPoint((TextEditor)editor, debugLine);
                        callback.onSuccess(virtualFile);
                    }
                }.schedule(300);
            }

            @Override
            public void onEditorActivated(final EditorPartPresenter editor) {
                new Timer() {
                    @Override
                    public void run() {
                        scrollEditorToExecutionPoint((TextEditor)editor, debugLine);
                        callback.onSuccess(virtualFile);
                    }
                }.schedule(300);
            }

            @Override
            public void onInitializationFailed() {
                callback.onFailure(null);
            }
        });
    }

    private void scrollEditorToExecutionPoint(TextEditor editor, int lineNumber) {
        Document document = editor.getDocument();

        if (document != null) {
            TextPosition newPosition = new TextPosition(lineNumber, 0);
            document.setCursorPosition(newPosition);
        }
    }
}
