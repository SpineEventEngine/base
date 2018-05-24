/*
 * Copyright 2018, TeamDev. All rights reserved.
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.logging;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.errorprone.annotations.FormatMethod;
import com.google.errorprone.annotations.FormatString;
import io.spine.base.Environment;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.SubstituteLogger;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * Utilities for working with logging.
 *
 * @author Alexander Yevsyukov
 */
public class Logging {

    /** Prevents instantiation of this utility class. */
    private Logging() {
    }

    /**
     * Creates a supplier for logger of the passed class.
     *
     * <p>A logger instance will be lazily {@linkplain #getLogger(Class) created}
     * when {@linkplain Supplier#get() requested} for the first time.
     *
     * <p>Such an arrangement may be convenient for having separate loggers in a class
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
        final Supplier<Logger> defaultSupplier = () -> getLogger(cls);
        return Suppliers.memoize(defaultSupplier);
    }

    /**
     * Obtains a logger for the passed class depending on the state of the {@link Environment}.
     *
     * <p>In {@linkplain Environment#isTests() tests mode}, the returned logger is a <em>new</em>
     * instance of {@link org.slf4j.helpers.SubstituteLogger SubstituteLogger} delegating to
     * a logger obtained from the {@link LoggerFactory#getLogger(Class) LoggerFactory}.
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

    /**
     * Logs {@linkplain Logger#warn(String, Throwable) warning} with the formatted string.
     *
     * @param log         the logger for placing the warning
     * @param th          the {@code Throwable} to log
     * @param errorFormat the format string for the error message
     * @param params      the arguments for the formatted string
     */
    @FormatMethod
    public static void warn(Logger log,
                            Throwable th,
                            @FormatString String errorFormat,
                            Object @Nullable ... params) {
        checkNotNull(log);
        checkNotNull(th);
        checkNotNull(errorFormat);
        checkNotNull(params);
        if (log.isWarnEnabled()) {
            final String msg = format(errorFormat, params);
            log.warn(msg, th);
        }
    }
}
