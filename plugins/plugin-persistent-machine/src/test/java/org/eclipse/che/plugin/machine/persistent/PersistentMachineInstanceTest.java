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
package org.eclipse.che.plugin.machine.persistent;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.plugin.machine.persistent.ssh.jsch.SshClient;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * author Alexander Garagatyi
 */
public class PersistentMachineInstanceTest {
    private Machine                   machine;
    private SshClient                 sshClient;
    private PersistentMachineInstance instance;
    private PersistentMachineFactory  persistentMachineFactory;

    @BeforeMethod
    public void setUp() throws Exception {
//        instance = new PersistentMachineInstance(machine, sshClient, LineConsumer.DEV_NULL, persistentMachineFactory);
    }

    @Test(expectedExceptions = MachineException.class,
          expectedExceptionsMessageRegExp = "Snapshot feature is unsupported for ssh machine implementation",
          enabled = false)
    public void shouldThrowExceptionOnSaveToSnapshot() throws Exception {
        instance.saveToSnapshot("owner1");
    }

    @Test
    public void shouldBeAbleToReturnLogger() throws Exception {
    }

    @Test
    public void shouldBeAbleToGetProcessById() throws Exception {
    }

    @Test
    public void shouldBeAbleToGetAllProcesses() throws Exception {
    }

    @Test
    public void shouldBeAbleToCreateProcess() throws Exception {
    }

    @Test
    public void shouldDoNothingOnDestroy() throws Exception {
    }

    @Test
    public void shouldReturnWHATonGetNode() throws Exception {
    }

    @Test
    public void shouldBeAbleToGetContentOfFile() throws Exception {
    }

    @Test
    public void shouldBeAbleToCopyFileFromOneMachineToAnother() throws Exception {
    }
}
