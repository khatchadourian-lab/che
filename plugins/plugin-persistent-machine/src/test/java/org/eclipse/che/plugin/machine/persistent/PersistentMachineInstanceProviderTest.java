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

import com.google.gson.Gson;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.exception.SnapshotException;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineSourceImpl;
import org.eclipse.che.api.machine.server.model.impl.ServerConfImpl;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceKey;
import org.eclipse.che.plugin.machine.persistent.ssh.SshMachineRecipe;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashSet;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * author Alexander Garagatyi
 */
@Listeners(MockitoTestNGListener.class)
public class PersistentMachineInstanceProviderTest {
    private static final int TEST_CONNECTION_TIMEOUT = 100;

    @Mock
    private PersistentMachineFactory persistentMachineFactory;
    @Mock
    private Session                  session;

    @Spy
    private JSch    jsch;

    private PersistentMachineInstanceProvider provider;
    private SshMachineRecipe                  sshMachineRecipe;
    private RecipeImpl                        recipe;
    private MachineImpl                       machine;

    @BeforeMethod
    public void setUp() throws Exception {
        provider = new PersistentMachineInstanceProvider(persistentMachineFactory);
        machine = createMachine();
        sshMachineRecipe = new SshMachineRecipe("localhost",
                                                22,
                                                "user",
                                                "password");
        recipe = new RecipeImpl().withType("ssh-machine.conf")
                                 .withScript(new Gson().toJson(sshMachineRecipe));
    }

    @Test(enabled = false)
    public void shouldReturnCorrectType() throws Exception {
        assertEquals(provider.getType(), "ssh-machine");
    }

    @Test(enabled = false)
    public void shouldReturnCorrectRecipeTypes() throws Exception {
        assertEquals(provider.getRecipeTypes(), new HashSet<>(singletonList("ssh-machine.conf")));
    }

    @Test(expectedExceptions = MachineException.class,
          expectedExceptionsMessageRegExp = "Snapshot feature is unsupported for ssh machine implementation",
          enabled = false)
    public void shouldThrowMachineExceptionOnCreateInstanceFromSnapshot() throws Exception {
        InstanceKey instanceKey = () -> Collections.EMPTY_MAP;

        provider.createInstance(instanceKey, null, null);
    }

    @Test(expectedExceptions = SnapshotException.class,
          expectedExceptionsMessageRegExp = "Snapshot feature is unsupported for ssh machine implementation",
          enabled = false)
    public void shouldThrowSnapshotExceptionOnRemoveSnapshot() throws Exception {
        provider.removeInstanceSnapshot(null);
    }

    @Test(expectedExceptions = MachineException.class,
          expectedExceptionsMessageRegExp = "Dev machine is not supported for Ssh machine implementation",
          enabled = false)
    public void shouldThrowExceptionOnDevMachineCreationFromRecipe() throws Exception {
        Machine machine = createMachine(true);

        provider.createInstance(recipe, machine, LineConsumer.DEV_NULL);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Recipe type .* is unsupported",
          enabled = false)
    public void shouldThrowExceptionOnMachineCreationFromRecipeWithIncorrectRecipeType() throws Exception {
        recipe.setType("unsupported-type");

        provider.createInstance(recipe, machine, LineConsumer.DEV_NULL);
    }

    @Test(enabled = false)
    public void shouldBeAbleToCreateSshMachineInstanceOnMachineCreationFromRecipe() throws Exception {
        when(jsch.getSession(anyString(), anyString(), anyInt())).thenReturn(session);

        Instance instance = provider.createInstance(recipe, machine, LineConsumer.DEV_NULL);

        assertNotNull(instance);
    }

    @Test(enabled = false)
    public void shouldCheckSshConnectionOnMachineCreationFromRecipe() throws Exception {
        when(jsch.getSession(anyString(), anyString(), anyInt())).thenReturn(session);

        provider.createInstance(recipe, machine, LineConsumer.DEV_NULL);

        verify(jsch).getSession(eq(sshMachineRecipe.getUsername()),
                                eq(sshMachineRecipe.getHost()),
                                eq(sshMachineRecipe.getPort()));
        verify(session).setUserInfo(argThat(new ArgumentMatcher<UserInfo>() {
            @Override
            public boolean matches(Object argument) {
                UserInfo userInfo = (UserInfo)argument;
                return sshMachineRecipe.getPassword().equals(userInfo.getPassword());
            }
        }));
        verify(session).connect(eq(TEST_CONNECTION_TIMEOUT));

    }

    @Test(expectedExceptions = MachineException.class,
          expectedExceptionsMessageRegExp = "Ssh machine creation failed because ssh of machine is inaccessible. Error: test exception message",
          enabled = false)
    public void shouldThrowMachineExceptionIfSshConnectionCheckFailsOnMachineCreation() throws Exception {
        when(jsch.getSession(anyString(), anyString(), anyInt())).thenThrow(new JSchException("test exception message"));

        provider.createInstance(recipe, machine, LineConsumer.DEV_NULL);
    }

    private MachineImpl createMachine() {
        return createMachine(false);
    }

    private MachineImpl createMachine(boolean isDev) {
        MachineConfig machineConfig = MachineConfigImpl.builder()
                                                       .setDev(isDev)
                                                       .setEnvVariables(singletonMap("testEnvVar1", "testEnvVarVal1"))
//                                                       .setLimits()
                                                       .setName("name1")
                                                       .setServers(singletonList(new ServerConfImpl("myref1",
                                                                                                    "10011/tcp",
                                                                                                    "http",
                                                                                                    null)))
                                                       .setSource(new MachineSourceImpl("ssh-machine.conf",
                                                                                        "localhost:10012/recipe"))
                                                       .setType("ssh-machine")
                                                       .build();
        return MachineImpl.builder()
                          .setConfig(machineConfig)
                          .setEnvName("env1")
                          .setId("id1")
                          .setOwner("owner1")
                          .setRuntime(null)
                          .setStatus(MachineStatus.CREATING)
                          .setWorkspaceId("wsId1")
                          .build();
    }
}
