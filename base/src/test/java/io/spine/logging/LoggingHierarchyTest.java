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

import io.spine.logging.given.Base;
import io.spine.logging.given.ChildOne;
import io.spine.logging.given.ChildTwo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests that classes in a hierarchy have own logs.
 *
 * @author Alexander Yevsyukov
 */
@DisplayName("Logging interface should work in a class hierarchy")
class LoggingHierarchyTest {

    @Test
    @DisplayName("create a logger for each class in hierarchy")
    void classHierarchy() {
        Logger baseLogger = new Base().log();
        Logger childOneLogger = new ChildOne().log();
        Logger childTwoLogger = new ChildTwo().log();

        assertNotSame(baseLogger, childOneLogger);
        assertNotSame(baseLogger, childTwoLogger);
        assertNotSame(childOneLogger, childTwoLogger);

        assertNotEquals(baseLogger, childOneLogger);
        assertNotEquals(baseLogger, childTwoLogger);
        assertNotEquals(childOneLogger, childTwoLogger);

        assertLogger(baseLogger, Base.class);
        assertLogger(childOneLogger, ChildOne.class);
        assertLogger(childTwoLogger, ChildTwo.class);
    }

    /**
     * Asserts that the logger name contains the name of the passed class.
     */
    private static void assertLogger(Logger logger, Class<?> cls) {
        assertTrue(logger.getName()
                         .contains(cls.getName()));
    }
}
