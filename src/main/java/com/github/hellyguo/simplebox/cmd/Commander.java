package com.github.hellyguo.simplebox.cmd;

import com.github.hellyguo.simplebox.cmd.netty.CommandServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Helly on 2017/06/20.
 */
public class Commander {
    private static final Logger LOGGER = LoggerFactory.getLogger(Commander.class);

    public void waitForCommand() {
        try {
            CommandServer server = new CommandServer();
            server.execute();
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }
}
