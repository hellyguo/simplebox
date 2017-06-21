package com.github.hellyguo.simplebox.cmd;

/**
 * Created by Helly on 2017/06/21.
 */
public enum SupportCommand {
    HELP("show this message again."),
    NOW("show current time."),
    DISCONN("just end the connection, simplebox will be alive."),
    STATUS("show the app's status."),
    START("start the app, simplebox will be alive."),
    RESTART("restart the app, simplebox will be alive."),
    STOP("stop the app, simplebox will be alive."),
    SHUTDOWN("shutdown the app and shutdown simplebox self.");

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
