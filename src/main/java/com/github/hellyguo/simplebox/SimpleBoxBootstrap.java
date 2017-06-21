package com.github.hellyguo.simplebox;

/**
 * Created by Helly on 2017/06/19.
 */

import com.github.hellyguo.simplebox.app.AppHolder;
import com.github.hellyguo.simplebox.cmd.Commander;

/**
 * scan dir:{app}<br>
 * boot the app<br>
 * wait for command<br>
 * destroy the app<br>
 * TODO:1.support multi app 2.classloader isolation 3.boot order 4.manager console<br>
 */
public class SimpleBoxBootstrap {

    // app holder
    private AppHolder holder = new AppHolder();
    // commander
    private Commander cmder = new Commander();

    public static void main(String[] args) throws Exception {
        SimpleBoxBootstrap bootstrap = new SimpleBoxBootstrap();
        bootstrap.init();
        bootstrap.boot();
        bootstrap.waitForCommand();
        bootstrap.shutdown();
    }

    private SimpleBoxBootstrap() {
    }

    private void init() {
        holder.init();
    }

    private void boot() {
        holder.boot();
    }

    private void waitForCommand() {
        cmder.waitForCommand();
    }

    private void shutdown() {
        holder.shutdown();
    }

}
