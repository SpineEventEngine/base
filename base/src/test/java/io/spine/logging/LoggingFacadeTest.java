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
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static io.spine.logging.Logging.redirect;
import static io.spine.testing.TestValues.random;
import static io.spine.testing.TestValues.randomString;
import static io.spine.testing.logging.LogTruth.assertThat;

/**
 * This test suite tests default implementations of the {@link Logging} interface.
 */
@DisplayName("Logging should")
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
    @DisplayName("have shortcut methods")
    class ShortcutMethod {

        private SubstituteLogger logger;
        private Queue<SubstituteLoggingEvent> queue;

        @BeforeEach
        void redirectLogging() {
            queue = Queues.newArrayDeque();
            logger = (SubstituteLogger) object.log();
            redirect(logger, queue);
        }

        /**
         * Asserts that the logging event queue has only one non-null event and
         * returns the subject for it.
         */
        private LogEventSubject assertEvent() {
            assertThat(queue).hasSize(1);
            LogEventSubject subject = assertThat(queue.poll());
            subject.isNotNull();
            return subject;
        }

        @Nested
        @DisplayName("with one message parameter")
        class WithMessage {

            @Test
            void _trace() {
                assertMethod(object::_trace, Level.TRACE);
            }

            /**
             * @implNote This method passes {@code TRACE} instead of {@code DEBUG}
             * because of the <a href="https://jira.qos.ch/browse/SLF4J-376">bug</a>
             * in {@link org.slf4j.event.EventRecodingLogger#debug(String)
             * EventRecodingLogger.debug(String)}.
             *
             * <p>This issue is not yet closed, but the
             * <a href="https://github.com/qos-ch/slf4j/blob/master/slf4j-api/src/main/java/org/slf4j/event/EventRecodingLogger.java">
             * EventRecodingLogger</a> class in the master branch of the coming v1.8 of Slf4J
             * already fixes this issue.
             */
            @Test
            void _debug() {
                assertMethod(object::_debug, Level.TRACE);
            }

            @Test
            void _warn() {
                assertMethod(object::_warn, Level.WARN);
            }

            @Test
            void _error() {
                assertMethod(object::_error, Level.ERROR);
            }

            /**
             * Asserts that a method generates a logging event of the passed level,
             * and that the method passes a generated message to the event.
             */
            private void assertMethod(Consumer<String> method, Level level) {
                String msg = randomString();
                method.accept(msg);

                LogEventSubject assertThat = assertEvent();
                assertThat.hasMessageThat()
                          .isEqualTo(msg);
                assertThat.hasLevelThat()
                          .isEqualTo(level);
            }

        }

        @Nested
        @DisplayName("with format and one argument")
        class SingleArgument {

            @Test
            void _trace() {
                assertMethod(object::_trace, Level.TRACE);
            }

            @Test
            void _debug() {
                assertMethod(object::_debug, Level.DEBUG);
            }

            @Test
            void _warn() {
                assertMethod(object::_warn, Level.WARN);
            }

            @Test
            void _error() {
                assertMethod(object::_error, Level.ERROR);
            }

            private void assertMethod(BiConsumer<String, Object> method, Level level) {
                String textInFormat = "Text" + random(100);
                String fmt = textInFormat + " {}";
                Object arg = "Arg" + random(10_000);

                method.accept(fmt, arg);

                LogEventSubject assertThat = assertEvent();
                assertThat.hasLevelThat()
                          .isEqualTo(level);
                assertThat.hasMessageThat()
                          .contains(textInFormat);
                assertThat.hasArgumentsThat()
                          .asList()
                          .containsExactly(arg);
            }
        }
    }
}
