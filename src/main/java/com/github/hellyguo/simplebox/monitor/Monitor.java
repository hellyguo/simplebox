package com.github.hellyguo.simplebox.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.Arrays;

/**
 * Created by Helly on 2017/06/22.
 */
public class Monitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(Monitor.class);
    private static final Monitor MONITOR = new Monitor();

    public static Monitor getMonitor() {
        return MONITOR;
    }

    private Monitor() {
    }

    public StringBuilder getThreadsInfo() {
        StringBuilder builder = new StringBuilder();
        Thread.getAllStackTraces().entrySet().forEach(entry -> {
            Thread thread = entry.getKey();
            StackTraceElement[] elements = entry.getValue();
            builder.append(thread.getName()).append(":\r\n");
            Arrays.stream(elements).forEach(element -> {
                if (element.isNativeMethod()) {
                    builder.append('\t').append(element.getClassName()).append('.').append(element.getMethodName())
                            .append("()[native]\r\n");
                } else {
                    builder.append('\t').append(element.getClassName()).append('.').append(element.getMethodName())
                            .append("()[").append(element.getFileName()).append(':').append(element.getLineNumber()).append("]\r\n");
                }
            });
            builder.append("\r\n");
        });
        return builder;
    }

    public StringBuilder getMemInfo() {
        StringBuilder builder = new StringBuilder();
        Runtime runtime = Runtime.getRuntime();
        double totalMemory = runtime.totalMemory() / 1024D / 1024D;
        double freeMemory = runtime.freeMemory() / 1024D / 1024D;
        double maxMemory = runtime.maxMemory() / 1024D / 1024D;
        builder.append("jvm mem info:\r\n")
                .append("\ttotalMemory:\t").append(totalMemory).append("MB\r\n")
                .append("\tfreeMemory:\t").append(freeMemory).append("MB\r\n")
                .append("\tmaxMemory:\t").append(maxMemory).append("MB\r\n");
        return builder;
    }

    public StringBuilder getMXBeanMemInfo() {
        StringBuilder builder = new StringBuilder();
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        builder.append("MXBean mem info:\r\n")
                .append("Heap:\t\t").append(memoryMXBean.getHeapMemoryUsage()).append("\r\n")
                .append("NonHeap:\t").append(memoryMXBean.getNonHeapMemoryUsage()).append("\r\n");
        return builder;
    }
}
