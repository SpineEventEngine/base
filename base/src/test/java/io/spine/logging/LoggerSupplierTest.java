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

import io.spine.logging.given.LoggingTestEnv;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for a logger supplier provided by the {@link Logging#getLogger(Class)}.
 *
 * @author Alexander Yevsyukov
 */
@DisplayName("Logger Supplier should")
class LoggerSupplierTest {

    private Supplier<Logger> supplier;

    @BeforeEach
    void setUp() {
        supplier = Logging.supplyFor(getClass());
    }

    @Test
    @DisplayName("provide Logger")
    void createLogger() {
        Logger logger = supplier.get();

        assertNotNull(logger);
    }

    @Test
    @DisplayName("provide the same Logger instance each time")
    void sameLogger() {
        assertSame(supplier.get(), supplier.get());
    }

    @Test
    @DisplayName("create a logger for each class in hierarchy")
    void classHierarchy() {
        Logger baseLogger = new LoggingTestEnv.Base().log();
        Logger childOneLogger = new LoggingTestEnv.ChildOne().log();
        Logger childTwoLogger = new LoggingTestEnv.ChildTwo().log();

        assertNotSame(baseLogger, childOneLogger);
        assertNotSame(baseLogger, childTwoLogger);
        assertNotSame(childOneLogger, childTwoLogger);

        assertNotEquals(baseLogger, childOneLogger);
        assertNotEquals(baseLogger, childTwoLogger);
        assertNotEquals(childOneLogger, childTwoLogger);

        assertLogger(baseLogger, LoggingTestEnv.Base.class);
        assertLogger(childOneLogger, LoggingTestEnv.ChildOne.class);
        assertLogger(childTwoLogger, LoggingTestEnv.ChildTwo.class);
    }

    /**
     * Asserts that the logger name contains the name of the passed class.
     */
    private static void assertLogger(Logger logger, Class<?> cls) {
        assertTrue(logger.getName()
                         .contains(cls.getName()));
    }
}
