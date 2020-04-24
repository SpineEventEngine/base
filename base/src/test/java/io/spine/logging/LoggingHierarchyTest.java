/*
 * Copyright 2020, TeamDev. All rights reserved.
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
import com.google.common.flogger.LogContext;
import io.spine.logging.given.Base;
import io.spine.logging.given.ChildOne;
import io.spine.logging.given.ChildTwo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

/**
 * Tests that classes in a hierarchy have own logs.
 */
@DisplayName("Logging interface should work in a class hierarchy")
class LoggingHierarchyTest {

    @Test
    @DisplayName("create a logger for each class in hierarchy")
    void classHierarchyFlogger() {
        FluentLogger baseLogger = new Base().logger();
        FluentLogger childOneLogger = new ChildOne().logger();
        FluentLogger childTwoLogger = new ChildTwo().logger();

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

    private static void assertLogger(FluentLogger logger, Class<?> cls) {
        LogContext<?, ?> context = (LogContext<?, ?>) logger.atSevere();
        assertThat(context.getLoggerName())
                .isEqualTo(cls.getName());
    }
}
