package com.github.hellyguo.simplebox;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Helly on 2017/06/20.
 */
class Commander {
    private AtomicBoolean active = new AtomicBoolean(true);

    void waitForCommand() {
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
