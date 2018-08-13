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

import com.google.common.truth.StringSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.logging.MethodGroupTest.randomArgument;
import static io.spine.logging.MethodGroupTest.randomText;

/**
 * @author Alexander Yevsyukov
 */
@DisplayName("Logging Util class should")
class UtilTest {

    private String formatText;
    private String fmt;
    private Object arg1;
    private Object arg2;

    @BeforeEach
    void setUp() {
        formatText = randomText();
        fmt = formatText + " {} {}";
        arg1 = randomArgument();
        arg2 = randomArgument();
    }

    @Test
    @DisplayName("call logging method expanding formatted string")
    void logThrowable() {
        Throwable t = new RuntimeException(getClass().getSimpleName());
        LogMessages.logThrowable(this::inspectCall, t, fmt, arg1, arg2);
    }

    private void inspectCall(String message, @SuppressWarnings("unused") Throwable ignored) {
        StringSubject assertThat = assertThat(message);
        assertThat.contains(formatText);
        assertThat.contains(arg1.toString());
        assertThat.contains(arg2.toString());
    }
}
