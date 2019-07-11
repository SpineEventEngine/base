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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.TestValues.randomString;

@DisplayName("MutingLoggerTap should")
class MutingLoggerTapTest extends SystemOutputTest {

    private MutingLoggerTap tap;

    @BeforeEach
    void createTap() {
        tap = new MutingLoggerTap(name());
    }

    @Nested
    @DisplayName("when not installed, NOT intercept")
    class NonInterception {

        @Test
        @DisplayName("regular logging")
        void regularLog() {
            String expected = "Test non interception.";
            logger().info(expected);

            assertThat(standardLoggingOutput()).contains(expected);
        }

        @Test
        @DisplayName("error logging")
        void errorLog() {
            String expectedError = "Testing error non interception.";
            logger().severe(expectedError);

            assertThat(standardLoggingOutput()).contains(expectedError);
        }
    }

    @Nested
    @DisplayName("intercept")
    class Interception {

        @BeforeEach
        void install() {
            tap.install();
        }

        @AfterEach
        void remove() {
            tap.remove();
        }

        @Test
        @DisplayName("regular logging")
        void regularLog() {
            String expected = "Test interception.";
            logger().info(expected);

            assertThat(standardLoggingOutput()).doesNotContain(expected);
        }

        @Test
        @DisplayName("error logging")
        void errorLog() {
            String expectedError = "Testing error interception.";
            logger().severe(expectedError);

            assertThat(standardLoggingOutput()).doesNotContain(expectedError);
        }

        @Test
        @DisplayName("redirecting to `MemoizingStream`")
        void redirection() {
            assertThat(tap.streamSize()).isEqualTo(0);
            String msg = randomString();
            Logger logger = logger();

            logger.info(msg);

            assertThat(tap.streamSize() > 0).isTrue();
        }

        @Nested
        @DisplayName("flush to `OutputStream`")
        class Flushing {

            private ByteArrayOutputStream stream;
            private String logMessage;
            private String errorMessage;

            @BeforeEach
            void flush() throws IOException {
                stream = new ByteArrayOutputStream();
                logMessage = "Testing log flushing. Random suffix: " + randomString();
                logger().info(logMessage);
                errorMessage = "Testing error flushing. Random suffix: " + randomString();
                logger().severe(errorMessage);
                tap.flushTo(stream);
            }

            @Test
            @DisplayName("accumulated output")
            void ofRegularLogs() {
                String fo = flushedOutput();
                assertThat(fo).contains(logMessage);
            }

            @Test
            @DisplayName("accumulated error output")
            void ofErrorLogs() {
                String fo = flushedOutput();
                assertThat(fo).contains(errorMessage);
            }
            String flushedOutput() {
                return new String(stream.toByteArray());
            }
        }
    }

    private String name() {
        return getClass().getName();
    }

    private Logger logger() {
        return Logger.getLogger(name());
    }
}
