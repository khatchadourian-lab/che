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
package org.eclipse.che.plugin.machine.persistent.ssh.jsch;

import com.google.gson.Gson;
import com.google.inject.assistedinject.Assisted;
import com.jcraft.jsch.JSch;

import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.model.impl.CommandImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceProcess;
import org.eclipse.che.plugin.machine.persistent.PersistentMachineFactory;
import org.eclipse.che.plugin.machine.persistent.PersistentMachineInstance;
import org.eclipse.che.plugin.machine.persistent.PersistentMachineInstanceProvider;
import org.eclipse.che.plugin.machine.persistent.PersistentMachineProcess;
import org.eclipse.che.plugin.machine.persistent.ssh.SshMachineRecipe;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;

import static org.testng.Assert.assertTrue;

/**
 * author Alexander Garagatyi
 */
@Listeners(MockitoTestNGListener.class)
public class IntegrationTest {
    private TestPersistentMachineFactoryImpl sshMachineFactory;

    @Test(enabled = false)
    public void test() throws Exception {
        sshMachineFactory = new TestPersistentMachineFactoryImpl(1000, new JSch());

        Machine machine = MachineImpl.builder().setConfig(MachineConfigImpl.builder().setDev(false).build()).build();

        PersistentMachineInstanceProvider persistentMachineInstanceProvider = new PersistentMachineInstanceProvider(sshMachineFactory);

        SshMachineRecipe sshMachineRecipe = new SshMachineRecipe("172.19.20.12",
                                                                 22,
                                                                 "gaal",
                                                                 "pass");

        RecipeImpl recipe = new RecipeImpl().withType("ssh-machine.conf")
                                            .withScript(new Gson().toJson(sshMachineRecipe));


        Instance instance = persistentMachineInstanceProvider.createInstance(recipe, machine, new TestLineConsumer());

        InstanceProcess process = instance.createProcess(new CommandImpl("name1", "ping -c 5 google.com", null), "channel1");

        process.start(new TestLineConsumer());

        assertTrue(true);
    }

    private static class TestPersistentMachineFactoryImpl implements PersistentMachineFactory {
        private int              timeout;
        private JSch             jSch;

        public TestPersistentMachineFactoryImpl(int timeout, JSch jSch) {
            this.timeout = timeout;
            this.jSch = jSch;
        }

        @Override
        public SshClient createSshClient(SshMachineRecipe sshMachineRecipe, Map<String, String> env) {
            return new SshClient(sshMachineRecipe, env, jSch, timeout);
        }

        @Override
        public PersistentMachineInstance createInstance(@Assisted Machine machine,
                                                        @Assisted SshClient sshClient,
                                                        @Assisted LineConsumer outputConsumer)
                throws MachineException {
            return new PersistentMachineInstance(machine, sshClient, outputConsumer, this);
        }

        @Override
        public PersistentMachineProcess createInstanceProcess(@Assisted Command command,
                                                              @Assisted("outputChannel") String outputChannel,
                                                              @Assisted int pid,
                                                              @Assisted SshClient sshClient) {
            return new PersistentMachineProcess(command, outputChannel, pid, sshClient);
        }
    }

    private static class TestLineConsumer implements LineConsumer {

        @Override
        public void writeLine(String line) throws IOException {
            System.out.println(line);
        }

        @Override
        public void close() throws IOException {

        }
    }
}
