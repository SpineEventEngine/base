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

import com.google.common.flogger.FluentLogger;
import com.google.common.truth.DefaultSubject;
import com.google.common.truth.Subject;
import io.spine.logging.given.LoggingObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.helpers.SubstituteLogger;

import static io.spine.testing.logging.LogTruth.assertThat;

@DisplayName("Logging interface should")
class LoggingTest {

    @Test
    @DisplayName("obtain Logger instance")
    void loggerInstance() {
        Logging object = new LoggingObject();
        Logger logger = object.log();
        Subject<DefaultSubject, Object> assertLogger = assertThat(logger);

        assertLogger.isNotNull();
        assertLogger.isInstanceOf(SubstituteLogger.class);
    }

    @Test
    @DisplayName("obtain same `FluentLogger` Instance")
    void fluentLogger() {
        Logging object = new LoggingObject();

        FluentLogger logger = object.logger();

        assertThat(object.logger())
             .isSameInstanceAs(logger);
        assertThat(new LoggingObject().logger())
             .isSameInstanceAs(logger);
    }
}
