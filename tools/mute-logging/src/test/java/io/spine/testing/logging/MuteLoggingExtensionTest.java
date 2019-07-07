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

import io.spine.logging.Logging;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.util.Optional;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.truth.Truth.assertThat;
import static java.lang.reflect.Modifier.isPublic;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("UseOfSystemOutOrSystemErr") // Test std I/O overloading.
@DisplayName("MuteLogging JUnit Extension should")
class MuteLoggingExtensionTest {

    private static final PrintStream originalOut = System.out;
    private static final PrintStream originalErr = System.err;
    private static final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private static final ByteArrayOutputStream err = new ByteArrayOutputStream();

    @BeforeAll
    static void init() {
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));
    }

    @AfterAll
    static void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @BeforeEach
    void setUp() {
        out.reset();
        err.reset();
    }

    @Test
    @DisplayName("have public parameter-less constructor")
    void ctor() throws NoSuchMethodException {
        Constructor<MuteLoggingExtension> constructor = MuteLoggingExtension.class.getConstructor();
        int modifiers = constructor.getModifiers();
        assertTrue(isPublic(modifiers));
    }

    @Test
    @DisplayName("hide the standard output")
    void hideStandardOutput() throws IOException {
        MuteLoggingExtension extension = new MuteLoggingExtension();
        extension.beforeEach(successfulContext());
        System.out.println("Output Message");
        System.out.println("Error Message");
        extension.afterEach(successfulContext());

        assertEquals(0, out.size());
        assertEquals(0, err.size());
    }

    @Test
    @DisplayName("print the standard output into std err stream if the test fails")
    void printOutputOnException() throws IOException {
        MuteLoggingExtension extension = new MuteLoggingExtension();
        extension.beforeEach(successfulContext());
        String outputMessage = "out";
        String errorMessage = "err";
        System.out.println(outputMessage);
        System.out.println(errorMessage);
        extension.afterEach(failedContext());

        assertEquals(0, out.size());
        String actualErrorOutput = new String(err.toByteArray(), UTF_8);
        assertThat(actualErrorOutput).contains(
                outputMessage
              + System.lineSeparator()
              + errorMessage
        );
    }

    @Test
    @DisplayName("mute Spine Logging tool")
    @SuppressWarnings("deprecation") // until new muting is implemented
    void muteSpineLogging() throws IOException {
        MuteLoggingExtension extension = new MuteLoggingExtension();
        extension.beforeEach(successfulContext());
        Logger muted = Logging.get(MuteLoggingExtensionTest.class);
        muted.warn("Muted warning");
        extension.afterEach(successfulContext());

        assertEquals(0, out.size());
        assertEquals(0, err.size());
    }

    private static ExtensionContext successfulContext() {
        ExtensionContext context = mock(ExtensionContext.class);
        when(context.getExecutionException()).thenReturn(Optional.empty());
        return context;
    }

    private static ExtensionContext failedContext() {
        ExtensionContext context = mock(ExtensionContext.class);
        when(context.getExecutionException()).thenReturn(Optional.of(new TestThrowable()));
        return context;
    }

    private static class TestThrowable extends Throwable {
        private static final long serialVersionUID = 0L;
    }
}
