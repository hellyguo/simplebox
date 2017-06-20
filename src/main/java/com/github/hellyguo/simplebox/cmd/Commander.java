package com.github.hellyguo.simplebox.cmd;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Helly on 2017/06/20.
 */
public class Commander {
    private AtomicBoolean active = new AtomicBoolean(true);

    public void waitForCommand() {
        while (active.get()) {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                //
            }
            break;
        }
    }
}
