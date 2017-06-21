package com.github.hellyguo.simplebox.app;

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

/**
 * Created by Helly on 2017/06/20.
 */
public class AppHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppHolder.class);

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

    private static final AppHolder HOLDER = new AppHolder();

    private String bootClassName;
    private String bootMethodName;
    private String stopClassName;
    private String stopMethodName;

    private URL[] classpath;
    private Thread targetThread;

    private AppStatus status = AppStatus.STOPPED;

    public static AppHolder getHolder() {
        return HOLDER;
    }

    private AppHolder() {
    }

    /**
     * init simplebox env
     */
    public void init() {
        String home = getSimpleBoxHome();
        File appDir = checkDir(home + APP_DIR);
        File appBootConf = checkFile(appDir.getAbsolutePath() + BOOT_PROP);
        File classesDir = checkDir(appDir.getAbsolutePath() + CLASSES_DIR);
        File libDir = checkDir(appDir.getAbsolutePath() + LIB_DIR);
        readClassInfo(appBootConf);
        classpath = genClasspath(classesDir, libDir);
    }

    /**
     * boot the app
     */
    public void boot() {
        if (AppStatus.STOPPED.equals(status)) {
            targetThread = bootApp();
        }
    }

    /**
     * shutdown the app
     */
    public void shutdown() {
        if (AppStatus.RUNNING.equals(status)) {
            stopApp();
            try {
                targetThread.join(10000L);
            } catch (InterruptedException e) {
                LOGGER.warn(e.getMessage(), e);
            }
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
        URLClassLoader targetClassLoader = new URLClassLoader(classpath, Thread.currentThread().getContextClassLoader());
        Thread thread = new Thread(() -> {
            try {
                runUnderSpecialClassLoader(Thread.currentThread().getContextClassLoader(), bootClassName, bootMethodName);
                status = AppStatus.RUNNING;
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
            runUnderSpecialClassLoader(targetThread.getContextClassLoader(), stopClassName, stopMethodName);
            status = AppStatus.STOPPED;
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

    public AppStatus getStatus() {
        return status;
    }
}
