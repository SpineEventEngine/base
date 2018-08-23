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

import io.spine.base.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.SubstituteLogger;

/**
 * Obtains {@link Logger} instance for a passed class and associates the value with the class.
 *
 * @author Alexander Yevsyukov
 */
class LoggerClassValue extends ClassValue<Logger> {

    private static final LoggerClassValue INSTANCE = new LoggerClassValue();

    static Logger getFor(Class<?> cls) {
        return INSTANCE.get(cls);
    }

    @Override
    protected Logger computeValue(Class<?> type) {
        Logger result = computeLogger(type);
        return result;
    }

    /**
     * Obtains or creates a logger for the passed class.
     *
     * @implNote If the code is executed under the {@linkplain Environment#isTests() tests},
     *           returned instance is new instance of a {@link SubstituteLogger},
     *           which redirects to a {@code Logger} obtained from
     *           {@link LoggerFactory#getLogger(Class) LoggerFactory}.
     */
    private static Logger computeLogger(Class<?> cls) {
        Logger logger = LoggerFactory.getLogger(cls);
        if (Environment.getInstance()
                       .isTests()) {
            SubstituteLogger substLogger = new SubstituteLogger(cls.getName(), null, true);
            substLogger.setDelegate(logger);
            return substLogger;
        }
        return logger;
    }
}
