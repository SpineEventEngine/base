/*
 * Copyright 2019, TeamDev. All rights reserved.
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
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.SubstituteLogger;
import org.slf4j.helpers.SubstituteLoggerFactory;

import static org.slf4j.helpers.NOPLogger.NOP_LOGGER;

/**
 * Obtains {@link Logger} instance for a passed class and associates the value with the class.
 */
class LoggerClassValue extends ClassValue<Logger> {

    private static final LoggerClassValue INSTANCE = new LoggerClassValue();

    /**
     * The factory for logger instances when running under tests.
     *
     * <p>If this field is not-null, we're running under tests, and should produce
     * {@link org.slf4j.helpers.SubstituteLogger} instances, so that logging can be tested too.
     *
     * <p>If this field is {@code null}, we're under production mode, and return
     * {@link org.slf4j.Logger} obtained from {@link org.slf4j.LoggerFactory} without substitution.
     */
    private final @MonotonicNonNull SubstituteLoggerFactory substFactory;

    /**
     * {@code true} if this class value should be resolved to a
     * {@linkplain org.slf4j.helpers.NOPLogger NO-OP logger}.
     */
    private boolean muted;

    private LoggerClassValue() {
        super();
        this.substFactory = Environment.getInstance()
                                       .isTests()
                            ? new SubstituteLoggerFactory()
                            : null;
        this.muted = false;
    }

    static Logger getFor(Class<?> cls) {
        return INSTANCE.get(cls);
    }

    /**
     * Mutes all computed logger instances.
     *
     * <p>This will cause all class values to be resolved to a
     * {@linkplain org.slf4j.helpers.NOPLogger NO-OP logger}.
     */
    static void muteAll() {
        INSTANCE.setMuted(true);
    }

    /**
     * Unmutes all computed logger instances.
     */
    static void unmuteAll() {
        INSTANCE.setMuted(false);
    }

    /**
     * Obtains or creates a logger for the passed class.
     *
     * @implNote If the code is executed under the {@linkplain Environment#isTests() tests},
     *           returned instance is new instance of a {@link SubstituteLogger},
     *           which redirects to a {@code Logger} obtained from
     *           {@link LoggerFactory#getLogger(Class) LoggerFactory}.
     */
    @Override
    protected Logger computeValue(Class<?> type) {
        Logger result = muted
                        ? noopLogger(type)
                        : computeLogger(type);
        return result;
    }

    /**
     * Obtains a logger implementation for the class.
     */
    private Logger computeLogger(Class<?> cls) {
        Logger logger = LoggerFactory.getLogger(cls);
        Logger result = substituteIfAvailable(logger, cls);
        return result;
    }

    /**
     * Returns a NO-OP {@linkplain org.slf4j.helpers.NOPLogger implementation} of the logger.
     *
     * <p>In case of test environment, this logger's output can still be redirected via
     * {@link SubstituteLogger#setDelegate(Logger)}.
     */
    private Logger noopLogger(Class<?> cls) {
        Logger result = substituteIfAvailable(NOP_LOGGER, cls);
        return result;
    }

    private Logger substituteIfAvailable(Logger logger, Class<?> cls) {
        if (substFactory != null) {
            SubstituteLogger substLogger = (SubstituteLogger) substFactory.getLogger(cls.getName());
            substLogger.setDelegate(logger);
            return substLogger;
        }
        return logger;
    }

    private void setMuted(boolean muted) {
        this.muted = muted;
    }
}
