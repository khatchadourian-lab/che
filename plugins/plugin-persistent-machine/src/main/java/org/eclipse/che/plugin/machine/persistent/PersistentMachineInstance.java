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

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.impl.AbstractInstance;
import org.eclipse.che.api.machine.server.model.impl.MachineRuntimeInfoImpl;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceKey;
import org.eclipse.che.api.machine.server.spi.InstanceNode;
import org.eclipse.che.api.machine.server.spi.InstanceProcess;
import org.eclipse.che.plugin.machine.persistent.ssh.jsch.SshClient;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;

/**
 * Implementation of {@link Instance} that uses represents persistent machine.
 *
 * @author Alexander Garagatyi
 *
 * @see org.eclipse.che.plugin.machine.persistent.PersistentMachineInstanceProvider
 */
public class PersistentMachineInstance extends AbstractInstance {
    private static final AtomicInteger pidSequence = new AtomicInteger(1);

    private final SshClient                                   sshClient;
    private final LineConsumer                                outputConsumer;
    private final PersistentMachineFactory                    machineFactory;
    private final ConcurrentHashMap<Integer, InstanceProcess> machineProcesses;

    private MachineRuntimeInfoImpl machineRuntime;

    @Inject
    public PersistentMachineInstance(@Assisted Machine machine,
                                     @Assisted SshClient sshClient,
                                     @Assisted LineConsumer outputConsumer,
                                     PersistentMachineFactory machineFactory) {
        super(machine);
        this.sshClient = sshClient;
        this.outputConsumer = outputConsumer;
        this.machineFactory = machineFactory;
        this.machineProcesses = new ConcurrentHashMap<>();
    }

    @Override
    public LineConsumer getLogger() {
        return outputConsumer;
    }

    @Override
    public MachineRuntimeInfoImpl getRuntime() {
        // if runtime info is not evaluated yet
        if (machineRuntime == null) {
            machineRuntime = new MachineRuntimeInfoImpl(emptyMap(), emptyMap(), emptyMap());
            // todo use servers from config
            // todo get env from client
        }
        return machineRuntime;
    }
// todo try to avoid map of processes
    @Override
    public InstanceProcess getProcess(final int pid) throws NotFoundException, MachineException {
        final InstanceProcess machineProcess = machineProcesses.get(pid);
        if (machineProcess != null) {
            try {
                machineProcess.checkAlive();
                return machineProcess;
            } catch (NotFoundException e) {
                machineProcesses.remove(pid);
                throw e;
            }
        } else {
            throw new NotFoundException(format("Process with pid %s not found", pid));
        }
    }

    @Override
    public List<InstanceProcess> getProcesses() throws MachineException {
        // todo get children of session process
        Map<Integer, InstanceProcess> aliveProcesses = machineProcesses.values()
                                                                       .stream()
                                                                       .filter(InstanceProcess::isAlive)
                                                                       .collect(Collectors.toMap(InstanceProcess::getPid,
                                                                                                 Function.identity()));
        machineProcesses.putAll(aliveProcesses);

        return new ArrayList<>(aliveProcesses.values());
    }

    @Override
    public InstanceProcess createProcess(Command command, String outputChannel) throws MachineException {
        final Integer pid = pidSequence.getAndIncrement();

        PersistentMachineProcess instanceProcess = machineFactory.createInstanceProcess(command, outputChannel, pid, sshClient);

        machineProcesses.put(pid, instanceProcess);

        return instanceProcess;
    }

    @Override
    public InstanceKey saveToSnapshot(String owner) throws MachineException {
        throw new MachineException("Snapshot feature is unsupported for ssh machine implementation");
    }

    @Override
    public void destroy() throws MachineException {
        // session destroying stops all processes
        // todo kill all processes started by code, we should get parent pid of session and kill all children
        sshClient.closeSession();
    }

    @Override
    public InstanceNode getNode() {
        return null;// todo
    }

    @Override
    public String readFileContent(String filePath, int startFrom, int limit) throws MachineException {
        // todo
        throw new MachineException("Copying is not implemented in ssh machine");
    }

    @Override
    public void copy(Instance sourceMachine, String sourcePath, String targetPath, boolean overwrite) throws MachineException {
        //todo
        throw new MachineException("Copying is not implemented in ssh machine");
    }
}
