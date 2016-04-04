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

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * author Alexander Garagatyi
 */
public class JschTest {
    private static final String PASSWORD = "password";
    private JSch jsch = new JSch();

    @Test(enabled = false)
    public void testExec() throws Exception {
        Session session = null;
        try {
            session = jsch.getSession("gaal", "localhost", 22);
            UserInfo ui = new MyUserInfo();
            session.setUserInfo(ui);
            session.connect(200);

            ChannelExec exec = (ChannelExec)session.openChannel("exec");
            exec.setCommand("ping -c 5 google.com");
            // todo error stream
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()))) {
                exec.connect(1000);

                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                throw new IOException("Ssh machine command execution error:" + e.getLocalizedMessage());
            }
        } finally {
            if (session != null) {
                session.disconnect();
            }
        }
    }

    @Test(enabled = false)
    public void testExec2() throws Exception {
        Session session = null;
        try {
            session = jsch.getSession("gaal", "localhost", 22);
            UserInfo ui = new MyUserInfo();
            session.setUserInfo(ui);
            session.connect(200);

            ChannelExec exec = (ChannelExec)session.openChannel("exec");
            exec.setCommand("ping -c 100 google.com");

            exec.connect(1000);
            Thread.sleep(5000);
            exec.disconnect();
        } finally {
            if (session != null) {
                session.disconnect();
            }
        }
    }

    @Test(enabled = false)
    public void testExec3() throws Exception {
        Session session = null;
        try {
            session = jsch.getSession("gaal", "localhost", 22);
            UserInfo ui = new MyUserInfo();
            session.setUserInfo(ui);
            session.connect(200);

            ChannelExec exec = (ChannelExec)session.openChannel("exec");
            exec.setCommand("ping -c 100 google.com");

            exec.connect(1000);
            Thread.sleep(5000);
            exec.disconnect();
        } finally {
            if (session != null) {
                session.disconnect();
            }
        }
    }

    @Test(enabled = false)
    public void test1() throws JSchException, IOException {
        JSch jsch = new JSch();

        Session session = jsch.getSession("gaal", "localhost", 22);

        UserInfo ui = new MyUserInfo();
        session.setUserInfo(ui);
        session.connect(2000);

        Channel exec = exec(session);
        Channel exec2 = exec(session);

        session.disconnect();
        session.connect(2000);

        exec(session);
    }

    private Channel exec(Session session) throws JSchException, IOException {
        Channel channel = session.openChannel("exec");
        ((ChannelExec)channel).setCommand("ping -c 10000 google.com");

        channel.setInputStream(null);

        ((ChannelExec)channel).setErrStream(System.err);

//        InputStream in = channel.getInputStream();

        channel.connect();

        return channel;

//        byte[] tmp=new byte[1024];
//        while(true){
//            while(in.available()>0){
//                int i=in.read(tmp, 0, 1024);
//                if(i<0)break;
//                System.out.print(new String(tmp, 0, i));
//            }
//            if(channel.isClosed()){
//                if(in.available()>0) continue;
//                System.out.println("exit-status: "+channel.getExitStatus());
//                break;
//            }
//            try {
//                Thread.sleep(1000);
//            } catch(Exception ee) {
//
//            }
//        }
//        channel.disconnect();
    }

    @Test(enabled = false)
    public void testConnection() throws JSchException {
        JSch jsch = new JSch();

        Session session = jsch.getSession("gaal", "127.0.0.1", 22);

        UserInfo ui = new MyUserInfo();
        session.setUserInfo(ui);
        session.connect();

        // shell
        // exec
        // session
//        Channel channel=session.openChannel("exec");
//
//        channel.connect();
    }

    public static class MyUserInfo implements UserInfo {
        public String getPassword() {
            return PASSWORD;
        }

        public boolean promptYesNo(String str) {
            return true;
        }

        public String getPassphrase() {
            return null;
        }

        public boolean promptPassphrase(String message) {
            return true;
        }

        public boolean promptPassword(String message) {
            return true;
        }

        public void showMessage(String message) {
            System.out.println(message);
        }
    }
}
