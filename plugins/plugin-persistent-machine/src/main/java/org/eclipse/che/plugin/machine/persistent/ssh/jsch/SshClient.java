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

import com.google.inject.assistedinject.Assisted;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.plugin.machine.persistent.ssh.SshMachineRecipe;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

/**
 * Client for communication with persistent machine using ssh protocol.
 *
 * author Alexander Garagatyi
 */
// todo think about replacement JSch with Apace SSHD
// todo tests for ssh library that ensures that it works as expected
public class SshClient {
    private final JSch                jsch;
    private final JschUserInfoImpl    user;
    private final String              host;
    private final int                 port;
    private final String              username;
    private final Map<String, String> envVars;
    private final int                 testConnectionTimeout;

    private Session session;

    @Inject
    public SshClient(@Assisted SshMachineRecipe sshMachineRecipe,
                     @Assisted Map<String, String> envVars,
                     JSch jsch,
                     @Named("machine.persistent.ssh.connection_timeout_ms") int testConnectionTimeoutMs) {
        this.envVars = envVars;
        this.testConnectionTimeout = testConnectionTimeoutMs;
        this.user = JschUserInfoImpl.builder()
                                    .password(sshMachineRecipe.getPassword())
                                    .promptPassword(true)
                                    .passphrase(null)
                                    .promptPassphrase(false)
                                    .promptYesNo(true)
                                    .build();
        this.jsch = jsch;
        this.host = sshMachineRecipe.getHost();
        this.port = sshMachineRecipe.getPort();
        this.username = sshMachineRecipe.getUsername();
    }

    public void openConnection() throws MachineException {
        try {
            session = jsch.getSession(username, host, port);
            session.setUserInfo(user);
            // todo remember parent pid of out shell to be able to kill all processes on client stop
            if (!session.isConnected()) {
                session.connect(testConnectionTimeout);
            }
        } catch (JSchException e) {
            throw new MachineException("Ssh machine creation failed because ssh of machine is inaccessible. Error: " +
                                       e.getLocalizedMessage());
        }
    }

    //todo add method to read env vars by client
    // ChannelExec exec = (ChannelExec)session.openChannel("exec");
    // exec.setCommand("env");
    //envVars.entrySet()
//    .stream()
//    .forEach(envVariableEntry -> exec.setEnv(envVariableEntry.getKey(),
//            envVariableEntry.getValue()));
//     todo process output

    public void closeSession() throws MachineException {
        session.disconnect();
    }

    public ChannelExec createExec(String commandLine) throws MachineException {
        try {
            ChannelExec exec = (ChannelExec)session.openChannel("exec");
            exec.setCommand(commandLine);
            envVars.entrySet()
                   .stream()
                   .forEach(envVariableEntry -> exec.setEnv(envVariableEntry.getKey(),
                                                            envVariableEntry.getValue()));
            return exec;
        } catch (JSchException e) {
            throw new MachineException("Can't establish connection to perform command execution in ssh machine. Error: " +
                                       e.getLocalizedMessage(), e);
        }
    }
}
