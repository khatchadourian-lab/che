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

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

import org.eclipse.che.api.machine.server.spi.InstanceProvider;
import org.eclipse.che.api.machine.server.terminal.MachineSpecificTerminalLauncher;

/**
 * Provides bindings needed for persistent machine implementation usage.
 *
 * author Alexander Garagatyi
 */
public class PersistentMachineModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder<InstanceProvider> machineProviderMultibinder =
                Multibinder.newSetBinder(binder(),
                                         org.eclipse.che.api.machine.server.spi.InstanceProvider.class);
        machineProviderMultibinder.addBinding()
                                  .to(org.eclipse.che.plugin.machine.persistent.PersistentMachineInstanceProvider.class);

        install(new FactoryModuleBuilder()
                        .implement(org.eclipse.che.api.machine.server.spi.Instance.class,
                                   org.eclipse.che.plugin.machine.persistent.PersistentMachineInstance.class)
                        .implement(org.eclipse.che.api.machine.server.spi.InstanceProcess.class,
                                   org.eclipse.che.plugin.machine.persistent.PersistentMachineProcess.class)
                        .build(org.eclipse.che.plugin.machine.persistent.PersistentMachineFactory.class));

        Multibinder<MachineSpecificTerminalLauncher> terminalLaunchers = Multibinder.newSetBinder(binder(),
                                                                                                  MachineSpecificTerminalLauncher.class);
        terminalLaunchers.addBinding().to(PersistentMachineTerminalLauncher.class);

        bindConstant().annotatedWith(Names.named(PersistentMachineTerminalLauncher.TERMINAL_LAUNCH_COMMAND_PROPERTY))
                      .to("~/che/terminal/che-websocket-terminal -addr :4411 -cmd /bin/bash -static ~/che/che-websocket-terminal/");

        bindConstant().annotatedWith(Names.named(PersistentMachineTerminalLauncher.TERMINAL_LOCATION_PROPERTY))
                      .to("~/che/terminal/");
    }
}
