/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import com.google.common.flogger.backend.LogData;
import io.spine.logging.given.LoggingObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.spine.testing.logging.LogTruth.assertThat;

@DisplayName("`Logging` interface should")
class LoggingTest {

    @Test
    @DisplayName("assume `Level.FINE` for debug")
    void debugLevel() {
        assertThat(Logging.debugLevel())
             .isEqualTo(Level.FINE);
    }

    @Test
    @DisplayName("assume `Level.SEVER` for errors")
    void errorLevel() {
        assertThat(Logging.errorLevel())
                .isEqualTo(Level.SEVERE);
    }

    @Test
    @DisplayName("obtain same `FluentLogger` Instance")
    void fluentLogger() {
        Logging object = new LoggingObject();

        var logger = object.logger();

        assertThat(object.logger())
             .isSameInstanceAs(logger);
        assertThat(new LoggingObject().logger())
             .isSameInstanceAs(logger);
    }

    @Test
    @DisplayName("provide a static API to obtain a logger for a class")
    void staticApi() {
        var logger = Logging.loggerFor(LoggingObject.class);
        var assertLogger = assertThat(logger);
        assertLogger.isNotNull();
        assertLogger.isSameInstanceAs(new LoggingObject().logger());
    }

    @Nested
    @DisplayName("expose shortcut methods")
    class Shortcuts {

        private Logging object;
        private Logger julLogger;

        @BeforeEach
        void createLoggingObject() {
            object = new LoggingObject();
            julLogger = Logger.getLogger(object.getClass().getName());
        }

        @Test
        @DisplayName("for standard logging levels")
        void shortcutMethods() {
            assertApi(object::_config, Level.CONFIG);
            assertApi(object::_fine, Level.FINE);
            assertApi(object::_finer, Level.FINER);
            assertApi(object::_finest, Level.FINEST);
            assertApi(object::_info, Level.INFO);
            assertApi(object::_severe, Level.SEVERE);
            assertApi(object::_warn, Level.WARNING);
        }

        @Test
        @DisplayName("for popular aliases")
        void aliases() {
            assertApi(object::_debug, Level.FINE);
            assertApi(object::_trace, Level.FINEST);
            assertApi(object::_error, Level.SEVERE);
        }

        private void assertApi(Supplier<FluentLogger.Api> method, Level expectedLevel) {
            julLogger.setLevel(expectedLevel);
            @SuppressWarnings("FloggerSplitLogStatement")
            // See: https://github.com/SpineEventEngine/base/issues/612
            var api = method.get();
            assertThat(api)
                    .isInstanceOf(LogContext.class);
            assertThat(((LogData) api).getLevel())
                    .isEqualTo(expectedLevel);
        }
    }
}
