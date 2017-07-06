package com.github.hellyguo.simplebox.cmd;

/**
 * Created by Helly on 2017/06/21.
 */
public enum SupportCommand {
    //help
    HELP("show this message again."),
    //time
    NOW("show current time."),
    //disconnect
    DISCONN("just end the connection, simplebox will be alive."),
    //box
    SHUTDOWN("shutdown the app and shutdown simplebox self."),
    //appholder
    STATUS("show the app's status."),
    START("start the app, simplebox will be alive."),
    RESTART("restart the app, simplebox will be alive."),
    STOP("stop the app, simplebox will be alive."),
    //monitor
    THREAD("print threads info"),
    MXBEAN_THREAD("print MXBean threads info"),
    MEM("print general mem info"),
    MXBEAN_MEM("print MXBean mem info");

    private String desc;

    SupportCommand(String desc) {
        this.desc = desc;
    }

    public String desc() {
        return desc;
    }

    public String cmd() {
        return name().toLowerCase();
    }
}
