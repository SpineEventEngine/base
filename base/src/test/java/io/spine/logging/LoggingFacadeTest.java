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

import com.google.common.collect.Queues;
import com.google.common.truth.DefaultSubject;
import com.google.common.truth.Subject;
import io.spine.logging.given.LoggingObject;
import io.spine.testing.logging.LogEventSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.event.Level;
import org.slf4j.event.SubstituteLoggingEvent;
import org.slf4j.helpers.SubstituteLogger;

import java.util.Queue;
import java.util.function.Consumer;

import static io.spine.testing.TestValues.randomString;
import static io.spine.testing.logging.LogTruth.assertThat;
import static io.spine.logging.Logging.redirect;

/**
 * This test suite tests default implementations of the {@link Logging} interface.
 */
@DisplayName("Logging object should")
class LoggingFacadeTest {

    private Logging object;
    private Subject<DefaultSubject, Object> assertLogger;

    @BeforeEach
    void setUp() {
        object = new LoggingObject();
        Logger logger = object.log();
        assertLogger = assertThat(logger);
    }

    @Test
    @DisplayName("obtain Logger instance")
    void methodLog() {
        assertLogger.isNotNull();
        assertLogger.isInstanceOf(SubstituteLogger.class);
    }

    @Nested
    @DisplayName("Support level")
    class LogLevel {

        private SubstituteLogger logger;
        private Queue<SubstituteLoggingEvent> queue;

        @BeforeEach
        void redirectLogging() {
            queue = Queues.newArrayDeque();
            logger = (SubstituteLogger) object.log();
            redirect(logger, queue);
        }

        @Test
        @DisplayName("trace")
        void trace() {
            assertMethod(object::_trace, Level.TRACE);
        }

        @Test
        @DisplayName("debug")
        void debug() {
            // We pass here `TRACE` (instead of `DEBUG`) because of this bug in Slf4J:
            //   https://jira.qos.ch/browse/SLF4J-376
            assertMethod(object::_debug, Level.TRACE);
        }

        /**
         * Asserts that a method generates a logging event of the correct level,
         * and passes the message to the event.
         */
        private void assertMethod(Consumer<String> method, Level level) {
            String message = randomString();
            method.accept(message);

            LogEventSubject assertThat = assertThat(queue.poll());
            assertThat.isNotNull();
            assertThat.hasMessageThat()
                      .isEqualTo(message);
            assertThat.hasLevelThat()
                      .isEqualTo(level);
        }
    }
}
