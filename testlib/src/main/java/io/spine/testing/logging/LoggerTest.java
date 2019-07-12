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

package io.spine.testing.logging;

import com.google.common.flogger.LoggerConfig;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Abstract base for tests of logging.
 */
public abstract class LoggerTest {

    /** The class which performs the log operations. */
    private final Class<?> loggingClass;
    /** The helper to set logger configuration. */
    private final LoggerConfig config;
    /** The value of the property used by the logger before the test. */
    private boolean useParentHandler;
    /** The value the logger had before the tests. */
    private final Level previousLevel;
    /** The level to be used during the tests. */
    private final Level level;
    /** The handler which remembers log records and performs assertions. */
    private @Nullable AssertingHandler handler;

    /**
     * Creates a new test suite.
     *
     * @param loggingClass
     *         the class which performs the logging operations
     * @param level
     *         the level of logging in which we are interested in the tests
     */
    protected LoggerTest(Class<?> loggingClass, Level level) {
        this.loggingClass = checkNotNull(loggingClass);
        this.config = LoggerConfig.getConfig(this.loggingClass);
        this.previousLevel = config.getLevel();
        this.level = checkNotNull(level);
    }

    /**
     * Obtains the instance of {@code AssertingHandler} of this test.
     *
     * @throws NullPointerException
     *          if the handler was not initialized or already removed
     * @see #addHandler()
     * @see #removeHandler()
     */
    protected final AssertingHandler handler() {
        return checkNotNull(handler, "The handler is not available. Please call `addHandler()`.");
    }

    /**
     * Obtains the class which logging operations of which we test.
     */
    protected final Class<?> loggingClass() {
        return loggingClass;
    }

    /**
     * Obtains the level of logging assigned for the tests.
     */
    protected final Level level() {
        return level;
    }

    /**
     * Obtains the level of the logging set for the logging class before the tests.
     */
    protected final Level previousLevel() {
        return previousLevel;
    }

    /**
     * Creates and assigns an {@link AssertingHandler} to the logger of the class.
     *
     * <p>The handler will have the {@linkplain #level() level} assigned for the test.
     * The method also turns off {@linkplain Logger#setUseParentHandlers(boolean) parent
     * handlers}.
     *
     * @apiNote This method is not annotated {@code @BeforeEach} to allow derived test
     *         suites hook up the logging where appropriate to the test suite. In some cases
     *         the logger should be tuned <em>after</em> some of the operations performed in
     *         a test setup.
     * @see #removeHandler()
     */
    protected final void addHandler() {
        handler = new AssertingHandler();
        handler.setLevel(level);

        useParentHandler = config.getUseParentHandlers();
        config.setLevel(level);
        config.addHandler(handler);
        config.setUseParentHandlers(false);
    }

    /**
     * Removes the handler assigned in {@link #addHandler()} and restores the value
     * of the flag for using {@linkplain Logger#getUseParentHandlers() parent handlers}.
     *
     * <p>The {@linkplain #handler handler} is not available after this method is called until
     * it is created and added back by {@link #addHandler()}.
     */
    protected final void removeHandler() {
        if (handler == null) {
            return;
        }
        config.removeHandler(handler);
        config.setUseParentHandlers(useParentHandler);
        config.setLevel(previousLevel);
        handler = null;
    }
}
