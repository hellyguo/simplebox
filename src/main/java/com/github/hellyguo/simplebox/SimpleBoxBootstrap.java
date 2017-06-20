package com.github.hellyguo.simplebox;

/**
 * Created by Helly on 2017/06/19.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * scan dir:{app}<br>
 * loop and boot each app<br>
 * wait for command<br>
 * TODO:1.boot order 2.classloader isolation 3.manager console<br>
 */
public class SimpleBoxBootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleBoxBootstrap.class);

    //home
    private static final String ENV_SBOX_HOME_NAME = "SBOX_HOME";
    //dir
    private static final String APP_DIR = "/app";
    private static final String CLASSES_DIR = "/classes";
    private static final String LIB_DIR = "/lib";
    //prop conf
    private static final String BOOT_PROP = "/boot.properties";
    //main class name & args
    private static final String BOOT_CLASS_NAME = "BootClass";
    private static final String BOOT_METHOD_NAME = "BootMethod";
    private static final String STOP_CLASS_NAME = "StopClass";
    private static final String STOP_METHOD_NAME = "StopMethod";

    private AtomicBoolean active = new AtomicBoolean(true);

    private String bootClassName;
    private String bootMethodName;
    private String stopClassName;
    private String stopMethodName;

    private URLClassLoader targetClassLoader;

    private Thread targetThread;

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
        String home = getSimpleBoxHome();
        File appDir = checkDir(home + APP_DIR);
        File appBootConf = checkFile(appDir.getAbsolutePath() + BOOT_PROP);
        File classesDir = checkDir(appDir.getAbsolutePath() + CLASSES_DIR);
        File libDir = checkDir(appDir.getAbsolutePath() + LIB_DIR);
        readClassInfo(appBootConf);
        URL[] classpath = genClasspath(classesDir, libDir);
        targetClassLoader = new URLClassLoader(classpath, Thread.currentThread().getContextClassLoader());
    }

    private void boot() {
        targetThread = bootApp();
    }

    private void waitForCommand() {
        int i = 200000;
        while (active.get()) {
            i--;
            if (i == 0) {
                break;
            }
        }
    }

    private void shutdown() {
        stopApp();
        try {
            targetThread.join(10000L);
        } catch (InterruptedException e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    private void readClassInfo(File appBootConf) {
        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream(appBootConf)) {
            prop.load(fis);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            System.exit(-1);
        }
        bootClassName = prop.getProperty(BOOT_CLASS_NAME);
        bootMethodName = prop.getProperty(BOOT_METHOD_NAME);
        stopClassName = prop.getProperty(STOP_CLASS_NAME);
        stopMethodName = prop.getProperty(STOP_METHOD_NAME);
    }

    private Thread bootApp() {
        Thread thread = new Thread(() -> {
            try {
                runUnderSpecialClassLoader(Thread.currentThread().getContextClassLoader(), bootClassName, bootMethodName);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                System.exit(-1);
            }
        }, "target thread");
        thread.setContextClassLoader(targetClassLoader);
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    private URL[] genClasspath(File classesDir, File libDir) {
        List<URL> classpathList = new ArrayList<>();
        try {
            classpathList.add(classesDir.toURI().toURL());
            File[] jars = libDir.listFiles((dir, name) -> name.endsWith(".jar"));
            if (jars != null) {
                Arrays.stream(jars).forEach(file -> {
                    try {
                        classpathList.add(file.toURI().toURL());
                    } catch (MalformedURLException e) {
                        LOGGER.warn(e.getMessage(), e);
                    }
                });
            }
        } catch (MalformedURLException e) {
            LOGGER.warn(e.getMessage(), e);
        }
        return classpathList.toArray(new URL[0]);
    }


    private void stopApp() {
        try {
            runUnderSpecialClassLoader(targetClassLoader, stopClassName, stopMethodName);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            System.exit(-1);
        }
    }

    private void runUnderSpecialClassLoader(ClassLoader loader, String className, String methodName) throws Exception {
        Class<?> clazz = loader.loadClass(className);
        Object object = clazz.newInstance();
        Method method = clazz.getMethod(methodName);
        method.invoke(object);
    }

    private File checkFile(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            if (!file.isFile()) {
                LOGGER.error("{} must be a file", file);
                System.exit(-1);
            }
        } else {
            LOGGER.error("{} must exist", file);
            System.exit(-1);
        }
        return file;
    }

    private File checkDir(String dirName) {
        File dir = new File(dirName);
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                LOGGER.error("{} must be a directory", dir);
                System.exit(-1);
            }
        } else {
            LOGGER.error("{} must exist", dir);
            System.exit(-1);
        }
        return dir;
    }

    private String getSimpleBoxHome() {
        String home = System.getenv(ENV_SBOX_HOME_NAME);
        if (home == null) {
            LOGGER.error("env SBOX_HOME must exist");
            System.exit(-1);
        }
        return home;
    }

}
