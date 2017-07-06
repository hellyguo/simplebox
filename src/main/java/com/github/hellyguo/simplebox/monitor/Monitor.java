package com.github.hellyguo.simplebox.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.*;
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
                printStackElement(builder, element);
            });
            builder.append("\r\n");
        });
        return builder;
    }

    private void printStackElement(StringBuilder builder, StackTraceElement element) {
        if (element.isNativeMethod()) {
            builder.append('\t').append(element.getClassName()).append('.').append(element.getMethodName())
                    .append("()[native]\r\n");
        } else {
            builder.append('\t').append(element.getClassName()).append('.').append(element.getMethodName())
                    .append("()[").append(element.getFileName()).append(':').append(element.getLineNumber()).append("]\r\n");
        }
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

    public StringBuilder getMXBeanThreadInfo() {
        StringBuilder builder = new StringBuilder();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        builder.append("MXBean thread info:\r\n");
        Arrays.stream(threadMXBean.dumpAllThreads(true, true)).forEach(threadInfo -> {
            builder.append("---------------------------\r\n");
            builder.append("id:\t").append(threadInfo.getThreadId()).append("\r\n");
            builder.append("name:\t").append(threadInfo.getThreadName()).append("\r\n");
            builder.append("state:\t").append(threadInfo.getThreadState().name()).append("\r\n");
            builder.append("\r\n");
            builder.append("blocked times:\t").append(threadInfo.getBlockedCount()).append("\r\n");
            builder.append("waited times:\t").append(threadInfo.getWaitedCount()).append("\r\n");
            LockInfo lock = threadInfo.getLockInfo();
            if (lock != null) {
                builder.append("\r\nthread lock/monitor detail:\r\n");
                printLock(builder, threadInfo, lock);
            }
            builder.append("\r\nthread stack detail:\r\n");
            Arrays.stream(threadInfo.getStackTrace()).forEach(element -> {
                printStackElement(builder, element);
            });
            builder.append("\r\n");
        });
        return builder;
    }

    private void printLock(StringBuilder builder, ThreadInfo threadInfo, LockInfo lock) {
        builder.append("lock info:[name:").append(threadInfo.getLockName())
                .append(" owner id:").append(threadInfo.getLockOwnerId())
                .append(" owner name:").append(threadInfo.getLockOwnerName())
                .append("]").append("\r\n");
        builder.append(lock.getClassName()).append('#').append(Long.toHexString(lock.getIdentityHashCode())).append("\r\n");
        Arrays.stream(threadInfo.getLockedMonitors()).forEach(monitorInfo -> {
            builder.append("locked stack depth:").append(monitorInfo.getLockedStackDepth()).append("\r\n");
            printStackElement(builder, monitorInfo.getLockedStackFrame());
        });
        Arrays.stream(threadInfo.getLockedSynchronizers())
                .forEach(lockInfo -> builder.append("locked:").append(lockInfo.toString()).append("\r\n"));
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
