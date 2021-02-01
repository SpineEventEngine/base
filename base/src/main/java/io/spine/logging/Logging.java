/*
 * Copyright 2020, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import com.google.common.flogger.FluentLogger;

import java.util.logging.Level;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility interface for objects that require logging output.
 *
 * <p>Such an object needs to implement this interface and obtain a {@link FluentLogger} instance
 * associated with the class of the object via the {@link #logger()} method.
 *
 * <p>In addition to this, the interface provides shortcut methods for the popular
 * logging interface methods. These shortcut methods are named after the tracing level
 * of Java Logging (such as {@link #_fine()} or {@link #_severe()}, and aliases for the levels
 * popular from other logging frameworks (such as {@link #_debug()} or {@link #_error()}.
 *
 * @apiNote The underscore-based convention is selected for making logging calls more visible and
 *          distinguishable from the real code.
 */
@SuppressWarnings({
        "NewMethodNamingConvention", "PMD.MethodNamingConventions",
        /* These methods are prefixed with underscore to highlight the fact that these methods
           are for logging, and to make them more visible in the real code. */
        "FloggerSplitLogStatement" /* We use logging API directly for brevity.
          See: https://github.com/SpineEventEngine/base/issues/612 */
})
public interface Logging {

    /** Returns {@link Level#FINE} as the convention for debug logging. */
    static Level debugLevel() {
        return Level.FINE;
    }

    /** Returns {@link Level#SEVERE} as the convention for logging errors. */
    static Level errorLevel() {
        return Level.SEVERE;
    }

    /**
     * Obtains {@code FluentLogger} instance for the given class.
     */
    static FluentLogger loggerFor(Class<?> cls) {
        checkNotNull(cls);
        return FloggerClassValue.loggerOf(cls);
    }

    /**
     * Obtains {@code FluentLogger} instance for this class.
     */
    default FluentLogger logger() {
        return FloggerClassValue.loggerOf(getClass());
    }

    /** A convenience method for {@link FluentLogger#atSevere()}. */
    default FluentLogger.Api _severe() {
        return logger().atSevere();
    }

    /** Same as {@link #_severe()}. */
    default FluentLogger.Api _error() {
        return _severe();
    }

    /** A convenience method for {@link FluentLogger#atWarning()}. */
    default FluentLogger.Api _warn() {
        return logger().atWarning();
    }

    /** A convenience method for {@link FluentLogger#atInfo()}. */
    default FluentLogger.Api _info() {
        return logger().atInfo();
    }

    /** A convenience method for {@link FluentLogger#atConfig()}. */
    default FluentLogger.Api _config() {
        return logger().atConfig();
    }

    /** A convenience method for {@link FluentLogger#atFine()}. */
    default FluentLogger.Api _fine() {
        return logger().atFine();
    }

    /** Sames as {@link #_fine()}. */
    default FluentLogger.Api _debug() {
        return _fine();
    }

    /** A convenience method for {@link FluentLogger#atFiner()}. */
    default FluentLogger.Api _finer() {
        return logger().atFiner();
    }

    /** A convenience method for {@link FluentLogger#atFine()}. */
    default FluentLogger.Api _finest() {
        return logger().atFinest();
    }

    /** Same as {@link #_finest()}. */
    default FluentLogger.Api _trace() {
        return _finest();
    }
}
