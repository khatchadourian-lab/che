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

import com.google.inject.assistedinject.Assisted;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.spi.InstanceProcess;
import org.eclipse.che.plugin.machine.persistent.ssh.jsch.SshClient;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import static java.lang.String.format;

/**
 * Docker implementation of {@link InstanceProcess}
 *
 * @author Alexander Garagatyi
 */
public class PersistentMachineProcess implements InstanceProcess {
    private final int                 pid;
    private final String              commandLine;
    private final String              commandName;
    private final String              commandType;
    private final Map<String, String> attributes;
    private final String              outputChannel;
    private final SshClient           sshClient;

    private volatile boolean     started;
    private volatile ChannelExec exec;

    @Inject
    public PersistentMachineProcess(@Assisted Command command,
                                    @Assisted("outputChannel") String outputChannel,
                                    @Assisted int pid,
                                    @Assisted SshClient sshClient) {
        this.sshClient = sshClient;
        this.commandLine = command.getCommandLine();
        this.commandName = command.getName();
        this.commandType = command.getType();
        this.attributes = command.getAttributes();
        this.outputChannel = outputChannel;
        this.pid = pid;
        this.started = false;
    }

    @Override
    public int getPid() {
        return pid;
    }

    @Override
    public String getName() {
        return commandName;
    }

    @Override
    public String getCommandLine() {
        return commandLine;
    }

    @Override
    public String getType() {
        return commandType;
    }

    @Override
    public Map<String, String> getAttributes() {
        return attributes;
    }

    @Override
    public String getOutputChannel() {
        return outputChannel;
    }

    @Override
    public boolean isAlive() {
        if (!started) {
            return false;
        }
        try {
            checkAlive();
            return true;
        } catch (MachineException | NotFoundException e) {
            // when process is not found (may be finished or killed)
            // when ssh is not accessible or responds in an unexpected way
            return false;
        }
    }

    @Override
    public void start() throws ConflictException, MachineException {
        start(null);
    }

    @Override
    public void start(LineConsumer output) throws ConflictException, MachineException {
        if (started) {
            throw new ConflictException("Process already started.");
        }

        exec = sshClient.createExec(commandLine);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
             BufferedReader errReader = new BufferedReader(new InputStreamReader(exec.getErrStream()))) {

            exec.connect();

            started = true;

            if (output != null) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // todo format output as it done in docker impl
                    // todo read error
                    //todo use async streams?
                    // todo how to manage disconnections due to network failures?
                    output.writeLine(line);
                }
            }
        } catch (IOException | JSchException e) {
            throw new MachineException("Ssh machine command execution error:" + e.getLocalizedMessage());
        }
    }

    @Override
    public void checkAlive() throws MachineException, NotFoundException {
        if (!started) {
            throw new NotFoundException("Process is not started yet");
        }

        if (exec.getExitStatus() != -1) {
            throw new NotFoundException(format("Process with pid %s not found", pid));
        }
    }

    @Override
    public void kill() throws MachineException {
        exec.disconnect();
    }
}
