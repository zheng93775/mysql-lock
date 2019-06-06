package com.github.zheng93775.mlock;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InterruptedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;


/**
 * Load resources (or images) from various sources.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
class Loader {
    private static final Logger logger = LoggerFactory.getLogger(Loader.class);

    static final String TSTR = "Caught Exception while in Loader.getResource. This may be innocuous.";

    static private boolean ignoreTCL = false;

    static {
        ignoreTCL = SystemProperty.getBoolean("mysql-lock.ignoreTCL", false);
    }

    /**
     * Get a resource by delegating to getResource(String).
     *
     * @param resource resource name
     * @param clazz    class, ignored.
     * @return URL to resource or null.
     * @deprecated as of 1.2.
     */
    public static URL getResource(String resource, Class clazz) {
        return getResource(resource);
    }

    /**
     * This method will search for <code>resource</code> in different
     * places. The search order is as follows:
     * <p>
     * <ol>
     * <p>
     * <p><li>Search for <code>resource</code> using the thread context
     * class loader under Java2. If that fails, search for
     * <code>resource</code> using the class loader that loaded this
     * class (<code>Loader</code>). Under JDK 1.1, only the the class
     * loader that loaded this class (<code>Loader</code>) is used.
     * <p>
     * <p><li>Try one last time with
     * <code>ClassLoader.getSystemResource(resource)</code>, that is is
     * using the system class loader in JDK 1.2 and virtual machine's
     * built-in class loader in JDK 1.1.
     * <p>
     * </ol>
     */
    static public URL getResource(String resource) {
        ClassLoader classLoader = null;
        URL url = null;

        try {
            if (!ignoreTCL) {
                classLoader = getTCL();
                if (classLoader != null) {
                    logger.debug("Trying to find [" + resource + "] using context classloader "
                            + classLoader + ".");
                    url = classLoader.getResource(resource);
                    if (url != null) {
                        return url;
                    }
                }
            }

            // We could not find resource. Ler us now try with the
            // classloader that loaded this class.
            classLoader = Loader.class.getClassLoader();
            if (classLoader != null) {
                logger.debug("Trying to find [" + resource + "] using " + classLoader
                        + " class loader.");
                url = classLoader.getResource(resource);
                if (url != null) {
                    return url;
                }
            }
        } catch (IllegalAccessException t) {
            logger.warn(TSTR, t);
        } catch (InvocationTargetException t) {
            if (t.getTargetException() instanceof InterruptedException
                    || t.getTargetException() instanceof InterruptedIOException) {
                Thread.currentThread().interrupt();
            }
            logger.warn(TSTR, t);
        } catch (Throwable t) {
            //
            //  can't be InterruptedException or InterruptedIOException
            //    since not declared, must be error or RuntimeError.
            logger.warn(TSTR, t);
        }

        // Last ditch attempt: get the resource from the class path. It
        // may be the case that clazz was loaded by the Extentsion class
        // loader which the parent of the system class loader. Hence the
        // code below.
        logger.debug("Trying to find [" + resource +
                "] using ClassLoader.getSystemResource().");
        return ClassLoader.getSystemResource(resource);
    }

    /**
     * Get the Thread Context Loader which is a JDK 1.2 feature. If we
     * are running under JDK 1.1 or anything else goes wrong the method
     * returns <code>null<code>.
     */
    private static ClassLoader getTCL() throws IllegalAccessException,
            InvocationTargetException {

        // Are we running on a JDK 1.2 or later system?
        Method method = null;
        try {
            method = Thread.class.getMethod("getContextClassLoader", null);
        } catch (NoSuchMethodException e) {
            // We are running on JDK 1.1
            return null;
        }

        return (ClassLoader) method.invoke(Thread.currentThread(), null);
    }


    /**
     * If running under JDK 1.2 load the specified class using the
     * <code>Thread</code> <code>contextClassLoader</code> if that
     * fails try Class.forname. Under JDK 1.1 only Class.forName is
     * used.
     */
    static public Class loadClass(String clazz) throws ClassNotFoundException {
        // Just call Class.forName(clazz) if we are running under JDK 1.1
        // or if we are instructed to ignore the TCL.
        if (ignoreTCL) {
            return Class.forName(clazz);
        } else {
            try {
                return getTCL().loadClass(clazz);
            }
            // we reached here because tcl was null or because of a
            // security exception, or because clazz could not be loaded...
            // In any case we now try one more time
            catch (InvocationTargetException e) {
                if (e.getTargetException() instanceof InterruptedException
                        || e.getTargetException() instanceof InterruptedIOException) {
                    Thread.currentThread().interrupt();
                }
            } catch (Throwable t) {
            }
        }
        return Class.forName(clazz);
    }
}
