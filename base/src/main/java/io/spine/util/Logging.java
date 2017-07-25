package io.spine.util;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import io.spine.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.SubstituteLogger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utilities for working with logging.
 *
 * @author Alexander Yevsyukov
 */
public class Logging {

    /** Prevents instantiation of this utility class. */
    private Logging() {}

    /**
     * Creates a supplier for logger of the passed class.
     *
     * <p>A logger instance will be lazily {@linkplain #getLogger(Class) created}
     * when {@linkplain Supplier#get() requested} for the first time.
     *
     * <p>Such an arrangement may be convenient for arranging separate loggers in a class
     * hierarchy.
     *
     * <h3>Typical usage pattern:</h3>
     * <pre>
     * {@code
     *   class MyClass {
     *     private final Supplier<Logger> loggerSupplier = Logging.supplyFor(getClass());
     *     ...
     *     protected Logger log() {
     *       return loggerSupplier.get();
     *     }
     *
     *     void doSomething() {
     *       log().debug("do something");
     *     }
     *   }
     * }
     * </pre>
     *
     * @param cls the class for which to supply a logger
     * @return new supplier
     */
    public static Supplier<Logger> supplyFor(final Class<?> cls) {
        checkNotNull(cls);
        final Supplier<Logger> defaultSupplier = new Supplier<Logger>() {
            @Override
            public Logger get() {
                return getLogger(cls);
            }
        };
        return Suppliers.memoize(defaultSupplier);
    }

    /**
     * Obtains a logger for the passed class depending on the state of the {@link Environment}.
     *
     * <p>In {@linkplain Environment#isTests() tests mode}, the returned logger is an instance of
     * {@link org.slf4j.helpers.SubstituteLogger SubstituteLogger} delegating to a logger obtained
     * from the {@link LoggerFactory#getLogger(Class) LoggerFactory}.
     *
     * <p>In {@linkplain Environment#isProduction() production mode}, returns the instance obtained
     * from the {@link LoggerFactory#getLogger(Class) LoggerFactory}.
     *
     * @param cls the class for which to create the logger
     * @return the logger instance
     */
    public static Logger getLogger(Class<?> cls) {
        final Logger logger = LoggerFactory.getLogger(cls);
        if (Environment.getInstance()
                       .isTests()) {
            final SubstituteLogger substLogger = new SubstituteLogger(cls.getName(), null, true);
            substLogger.setDelegate(logger);
            return substLogger;
        } else {
            return logger;
        }
    }
}
