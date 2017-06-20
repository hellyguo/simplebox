package com.github.hellyguo.simplebox.cmd;

import com.github.hellyguo.simplebox.cmd.netty.TelnetServer;
import com.github.hellyguo.simplebox.cmd.netty.TelnetServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Helly on 2017/06/20.
 */
public class Commander {
    private static final Logger LOGGER = LoggerFactory.getLogger(Commander.class);

    public void waitForCommand() {
        try {
            TelnetServer server = new TelnetServer();
            server.execute();
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }
}
