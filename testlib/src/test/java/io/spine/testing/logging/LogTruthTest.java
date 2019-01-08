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

import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import org.slf4j.event.SubstituteLoggingEvent;

import static io.spine.testing.logging.LogTruth.assertThat;

@DisplayName("LogTruth should")
class LogTruthTest extends UtilityClassTest<LogTruth> {

    LogTruthTest() {
        super(LogTruth.class);
    }

    @Test
    void eventSubject() {
        SubstituteLoggingEvent event = new SubstituteLoggingEvent();
        LogEventSubject assertThat = assertThat(event);
        assertThat.isNotNull();
    }

    @Nested
    @DisplayName("provide Subject for log event with")
    class SubjectForLogEvent {

        private SubstituteLoggingEvent event;
        private LogEventSubject subject;

        @BeforeEach
        void setUp() {
            event = new SubstituteLoggingEvent();
            event.setLevel(Level.DEBUG);
            event.setMessage("Testing subject of logging event");
            event.setArgumentArray(new String[] { "arg1", "arg2", "arg3" });

            subject = assertThat(event);
        }

        @Test
        void isNotNull() {
            subject.isNotNull();
        }

        @Test
        void hasMessageThat() {
            subject.hasMessageThat()
                   .isEqualTo(event.getMessage());
        }

        @Test
        void hasArgumentsThat() {
            subject.hasArgumentsThat()
                   .asList()
                   .containsExactly(event.getArgumentArray());
        }
    }
}
