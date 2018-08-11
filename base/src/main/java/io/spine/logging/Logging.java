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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.event.EventRecodingLogger;
import org.slf4j.event.SubstituteLoggingEvent;
import org.slf4j.helpers.SubstituteLogger;

import java.util.Queue;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * Utility interface for objects that require logging output.
 *
 * <p>Such an object need to implement this interface and obtain a {@link Logger} instance
 * associated with the class of the object via {@link #log()} method.
 *
 * <p>In addition to this, this interface provides shortcut methods for the popular
 * logging interface methods. These shortcut methods are named after those provided by
 * {@link Logger}, but with the underscore as the prefix:
 * {@link #_trace(String) _trace()}, {@link #_debug(String) _debug()},
 * {@link #_warn(String) _warn()}, {@link #_error(String) _error()}.
 *
 * <p>The interface does not provide shortcut methods for than three arguments
 * because of the {@linkplain Logger#debug(String, Object...) associated performance cost}.
 * If you do need more than three arguments, please use a {@code Logger}
 * instance obtained via {@link #log()}.
 *
 * @apiNote The underscore-based convention is selected for making logging calls more visible and
 *          distinguishable from the real code.
 *
 * @author Alexander Yevsyukov
 */
@SuppressWarnings({
        "ClassWithTooManyMethods"
        /* We provide shortcut methods for calling Slf4J Logger API. */,

        "NewMethodNamingConvention"
        /* These methods are prefixed with underscore to highlight the fact that these methods
           are for logging, and to make them more visible in the real code. */
})
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
     * Logs {@linkplain Logger#warn(String, Throwable) warning} with the formatted string.
     *
     * @param log         the logger for placing the warning
     * @param th          the {@code Throwable} to log
     * @param errorFormat the format string for the error message
     * @param params      the arguments for the formatted string
     */
    @FormatMethod
    static void warn(Logger log,
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

    /*
     * TRACE Level
     ****************/

    /** Logs a message at the {@linkplain Logger#trace(String) TRACE} level. */
    default void _trace(String msg) {
        log().trace(msg);
    }

    /**
     * Logs a message at the {@linkplain Logger#trace(String, Object) TRACE} level according
     * to the specified format and argument.
     */
    default void _trace(String format, Object arg) {
        log().trace(format, arg);
    }

    /**
     * Logs a message at the {@linkplain Logger#trace(String, Object) TRACE} level according
     * to the specified format and arguments.
     */
    default void _trace(String format, Object arg1, Object arg2) {
        log().trace(format, arg1, arg2);
    }

    /**
     * Logs a message at the {@linkplain Logger#trace(String, Object) TRACE} level according
     * to the specified format and arguments.
     *
     * @apiNote for more than three arguments, please use:
     * <blockquote>{@code log().trace(format, arg1, arg2, arg3, ...); }</blockquote>
     */
    default void _trace(String format, Object arg1, Object arg2, Object arg3) {
        log().trace(format, arg1, arg2, arg3);
    }

    /*
     * DEBUG Level
     ****************/

    /** Logs a message at the {@linkplain Logger#debug(String) DEBUG} level. */
    default void _debug(String msg) {
        log().debug(msg);
    }

    /**
     * Logs a message at the {@linkplain Logger#debug(String, Object) DEBUG} level according
     * to the specified format and argument.
     */
    default void _debug(String format, Object arg) {
        log().debug(format, arg);
    }

    /**
     * Logs a message at the {@linkplain Logger#debug(String, Object) DEBUG} level according
     * to the specified format and arguments.
     */
    default void _debug(String format, Object arg1, Object arg2) {
        log().debug(format, arg1, arg2);
    }

    /**
     * Logs a message at the {@linkplain Logger#debug(String, Object) DEBUG} level according
     * to the specified format and arguments.
     *
     * @apiNote for more than three arguments, please use:
     * <blockquote>{@code log().debug(format, arg1, arg2, arg3, ...); }</blockquote>
     */
    default void _debug(String format, Object arg1, Object arg2, Object arg3) {
        log().debug(format, arg1, arg2, arg3);
    }


    /*
     * WARN Level
     ****************/

    /** Logs a message at the {@linkplain Logger#warn(String) WARN} level. */
    default void _warn(String msg) {
        log().warn(msg);
    }

    /**
     * Logs a message at the {@linkplain Logger#warn(String, Object) WARN} level according
     * to the specified format and argument.
     */
    default void _warn(String format, Object arg) {
        log().warn(format, arg);
    }

    /**
     * Logs a message at the {@linkplain Logger#warn(String, Object) WARN} level according
     * to the specified format and arguments.
     */
    default void _warn(String format, Object arg1, Object arg2) {
        log().warn(format, arg1, arg2);
    }

    /**
     * Logs a message at the {@linkplain Logger#warn(String, Object) WARN} level according
     * to the specified format and arguments.
     *
     * @apiNote for more than three arguments, please use:
     * <blockquote>{@code log().warn(format, arg1, arg2, arg3, ...); }</blockquote>
     */
    default void _warn(String format, Object arg1, Object arg2, Object arg3) {
        log().warn(format, arg1, arg2, arg3);
    }

    /*
     * ERROR Level
     ****************/

    /** Logs a message at the {@linkplain Logger#error(String) ERROR} level. */
    default void _error(String msg) {
        log().error(msg);
    }

    /**
     * Logs a message at the {@linkplain Logger#error(String, Object) ERROR} level according
     * to the specified format and argument.
     */
    default void _error(String format, Object arg) {
        log().error(format, arg);
    }

    /**
     * Logs a message at the {@linkplain Logger#error(String, Object) ERROR} level according
     * to the specified format and arguments.
     */
    default void _error(String format, Object arg1, Object arg2) {
        log().error(format, arg1, arg2);
    }

    /**
     * Logs a message at the {@linkplain Logger#error(String, Object) ERROR} level according
     * to the specified format and arguments.
     *
     * @apiNote for more than three arguments, please use:
     * <blockquote>{@code log().error(format, arg1, arg2, arg3, ...); }</blockquote>
     */
    default void _error(String format, Object arg1, Object arg2, Object arg3) {
        log().error(format, arg1, arg2, arg3);
    }
}
