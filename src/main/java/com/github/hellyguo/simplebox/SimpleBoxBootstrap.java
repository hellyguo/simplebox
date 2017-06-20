package com.github.hellyguo.simplebox;

/**
 * Created by Helly on 2017/06/19.
 */

/**
 * scan dir:{app}<br>
 * loop and boot each app<br>
 * wait for command<br>
 * TODO:1.boot order 2.classloader isolation 3.manager console<br>
 */
public class SimpleBoxBootstrap {

    private AppHolder holder = new AppHolder();
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
