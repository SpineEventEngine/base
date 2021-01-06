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

package io.spine.testing.logging;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

/**
 * Abstract base for tests that need to substitute {@link System#out} and {@link System#err}
 * for analyzing logging output.
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr") // Test std I/O substitution.
public abstract class SystemOutputTest {

    private static final PrintStream originalOut = System.out;
    private static final PrintStream originalErr = System.err;
    private static final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private static final ByteArrayOutputStream err = new ByteArrayOutputStream();

    @BeforeAll
    static void substituteStreams() {
        System.setOut(newPrintStream(out));
        System.setErr(newPrintStream(err));
    }

    private static PrintStream newPrintStream(ByteArrayOutputStream stream) {
        return new PrintStream(stream);
    }

    private static Charset charset() {
        return Charset.defaultCharset();
    }

    @AfterAll
    static void restoreOriginalStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
        out.reset();
        err.reset();
    }

    /**
     * Obtains the stream which accumulates system output.
     */
    protected static ByteArrayOutputStream out() {
        return out;
    }

    /**
     * Obtains the content of the system output accumulated so far.
     */
    protected static String output() {
        return toString(out);
    }

    /**
     * Obtains the stream which accumulates system error output.
     */
    protected static ByteArrayOutputStream err() {
        return err;
    }

    /**
     * Obtains the content of the system error output accumulated so far.
     */
    protected static String errorOutput() {
        return toString(err);
    }

    /**
     * Obtains the logging output accumulated so far.
     *
     * @apiNote By default Java Logging writes logging to {@code System.err}.
     *         This method is an alias to {@link #errorOutput()} so that the code of tests
     *         does not bring a confusion related to the "error" word in the context of logging.
     */
    protected static String loggingOutput() {
        return errorOutput();
    }

    private static String toString(ByteArrayOutputStream stream) {
        String result = new String(stream.toByteArray(), charset());
        return result;
    }
}
