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

import com.google.errorprone.annotations.FormatMethod;
import com.google.errorprone.annotations.FormatString;
import io.spine.base.Environment;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.EventRecodingLogger;
import org.slf4j.event.SubstituteLoggingEvent;
import org.slf4j.helpers.SubstituteLogger;

import java.util.Queue;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Suppliers.memoize;
import static java.lang.String.format;

/**
 * Utility interface for objects that require logging output.
 *
 * <p>Such an object need to implement this interface and obtain a {@link Logger} instance
 * associated with the class of the object via {@link #log()} method.
 *
 * <p>In addition to this, this interface provides shortcut methods for the popular
 * logging interface methods. These shortcut methods are named after those provided by
 * {@link Logger}, but with the underscore as the prefix: {@link #_trace(String) _trace()},
 * {@link #_debug(String) _debuf()} and so on.
 *
 * @apiNote The underscore-based convention is selected for making logging calls more visible and
 *          distinguishable from the real code.
 *
 * @author Alexander Yevsyukov
 */
@SuppressWarnings("NewMethodNamingConvention")
    // We provide methods prefixed with underscore to highlight the fact that these methods
    // are for logging, and to make them visible.
public interface Logging {

    /**
     * Obtains logger associated with the class of this instance.
     */
    default Logger log() {
        return LoggerClassValue.getFor(getClass());
    }

    /**
     * Redirects logging to the passed logging event queue.
     */
    static void redirect(SubstituteLogger log, Queue<SubstituteLoggingEvent> queue) {
        checkNotNull(log);
        checkNotNull(queue);
        log.setDelegate(new EventRecodingLogger(log, queue));
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
     * @deprecated implement {@link Logging} and use {@link #log()} instead
     */
    @Deprecated
    static Supplier<Logger> supplyFor(Class<?> cls) {
        checkNotNull(cls);
        Supplier<Logger> supplier = memoize(() -> getLogger(cls));
        return supplier;
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
    static Logger getLogger(Class<?> cls) {
        return LoggerClassValue.getFor(cls);
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
            String msg = format(errorFormat, params);
            log.warn(msg, th);
        }
    }

    /** Logs a message at the {@linkplain Logger#trace(String) TRACE} level. */
    default void _trace(String msg) {
        log().trace(msg);
    }

    /**
     * Logs a message at the {@linkplain Logger#trace(String) TRACE} level according
     * to the specified format and argument.
     */
    default void _trace(String format, Object arg) {
        log().trace(format, arg);
    }

    /** Logs a message at the {@linkplain Logger#debug(String) DEBUG} level. */
    default void _debug(String msg) {
        log().debug(msg);
    }

    /** Logs a message at the {@linkplain Logger#warn(String) WARN} level. */
    default void _warn(String msg) {
        log().warn(msg);
    }

    /** Logs a message at the {@linkplain Logger#error(String) ERROR} level. */
    default void _error(String msg) {
        log().error(msg);
    }
}
