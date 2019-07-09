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

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Abstract base for tests of logging.
 */
public abstract class LoggingTest {

    private final Class<?> loggingClass;
    private final Level level;
    private boolean useParentHandler;
    private @Nullable AssertingHandler handler;

    /**
     * Creates new test suite.
     *
     * @param loggingClass the class which performs the logging operations
     * @param level        the level of logging we are interested in the tests
     */
    protected LoggingTest(Class<?> loggingClass, Level level) {
        this.loggingClass = checkNotNull(loggingClass);
        this.level = checkNotNull(level);
    }

    protected final AssertingHandler handler() {
        return checkNotNull(handler, "The handler is not available. Please call `addHandler()`.");
    }

    /**
     * Obtains the level of logging assigned for the tests.
     */
    protected final Level level() {
        return level;
    }

    private Logger jdkLogger() {
        return Logger.getLogger(loggingClass.getName());
    }

    /**
     * Creates and assigns an {@link AssertingHandler} to the logger of the class.
     *
     * <p>The handler will have the {@linkplain #level() level} assigned for the test.
     * The method also turns off using {@linkplain Logger#setUseParentHandlers(boolean) parent
     * handlers}
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

        Logger jdkLogger = jdkLogger();
        this.useParentHandler = jdkLogger.getUseParentHandlers();
        jdkLogger.setUseParentHandlers(false);
        jdkLogger.addHandler(handler);
        jdkLogger.setLevel(level);
    }

    /**
     * Removes the handler assigned in {@link #addHandler()} and restores the value
     * of using {@linkplain Logger#getUseParentHandlers() parent handlers}.
     */
    protected final void removeHandler() {
        Logger jdkLogger = jdkLogger();
        jdkLogger.removeHandler(handler);
        jdkLogger.setUseParentHandlers(useParentHandler);
    }
}
