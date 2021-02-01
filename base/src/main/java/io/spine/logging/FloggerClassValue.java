/*
 * Copyright 2021, TeamDev. All rights reserved.
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
import com.google.common.flogger.backend.LoggerBackend;
import com.google.common.flogger.backend.Platform;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;

/**
 * Obtains {@link FluentLogger} instance for a passed class and associates the value with the class.
 */
final class FloggerClassValue extends ClassValue<FluentLogger> {
    
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private static final FloggerClassValue INSTANCE = new FloggerClassValue();

    private final Constructor<FluentLogger> constructor;

    /**
     * Obtains the logger instance for the passed class.
     */
    static FluentLogger loggerOf(Class<?> cls) {
        return INSTANCE.get(cls);
    }

    /** Prevent instantiation from outside. */
    private FloggerClassValue() {
        super();
        this.constructor = ctor();
    }

    private static Constructor<FluentLogger> ctor() {
        Class<LoggerBackend> loggerBackendClass = LoggerBackend.class;
        try {
            Constructor<FluentLogger> constructor =
                    FluentLogger.class.getDeclaredConstructor(loggerBackendClass);
            constructor.setAccessible(true);
            return constructor;
        } catch (NoSuchMethodException e) {
            logger.atSevere()
                  .withCause(e)
                  .log("Unable to find constructor `%s(%s)`.",
                       FluentLogger.class.getName(), loggerBackendClass.getName());
            throw illegalStateWithCauseOf(e);
        }
    }

    @Override
    protected FluentLogger computeValue(Class<?> type) {
        checkNotNull(type);
        LoggerBackend backend = Platform.getBackend(type.getName());
        try {
            FluentLogger logger = constructor.newInstance(backend);
            return logger;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            logger.atSevere()
                  .withCause(e)
                  .log("Unable to create logger.");
            throw illegalStateWithCauseOf(e);
        }
    }
}
