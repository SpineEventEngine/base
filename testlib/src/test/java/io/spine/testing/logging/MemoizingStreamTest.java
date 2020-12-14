/*
 * Copyright 2020, TeamDev. All rights reserved.
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.google.common.primitives.Bytes.asList;
import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.TestValues.random;
import static java.lang.Byte.MAX_VALUE;
import static java.lang.Byte.MIN_VALUE;

@DisplayName("MemoizingStream should")
class MemoizingStreamTest {

    private static final byte[] EMPTY_BYTES = {};

    private MemoizingStream stream;

    @BeforeEach
    void createStream() {
        stream = new MemoizingStream();
    }

    @AfterEach
    void closeStream() throws IOException {
        stream.close();
    }

    @Nested
    @DisplayName("provide size of the stream")
    class Size {

        private int size;
        private byte[] input;

        @BeforeEach
        void generateInput() {
            size = random(1000);
            input = randomBytes(size);
        }

        @Test
        @DisplayName("equal to zero")
        void nothingWritten() {
            assertThat(stream.size())
                    .isEqualTo(0);
        }

        @Test
        @DisplayName("equal to size of written bytes")
        void inputWritten() throws IOException {
            stream.write(input);

            assertThat(stream.size())
                    .isEqualTo(size);
        }

        @Test
        @DisplayName("equal to zero after `reset()`")
        void clearOnReset() throws IOException {
            stream.write(input);
            stream.reset();

            assertThat(stream.size())
                    .isEqualTo(0);
        }
    }

    @Test
    @DisplayName("flush all the input")
    void flushEverything() throws IOException {
        byte[] input = randomBytes(42);

        stream.write(input);

        checkMemoized(input);
    }

    @Test
    @DisplayName("not store flushed bytes")
    void clearAfterFlush() throws IOException {
        byte[] input = randomBytes(12);

        stream.write(input);

        checkMemoized(input);
        checkMemoized(EMPTY_BYTES);
    }

    @Test
    @DisplayName("clear memoized bytes on demand")
    void clearOnDemand() throws IOException {
        byte[] input = randomBytes(4);

        stream.write(input);
        stream.reset();

        checkMemoized(EMPTY_BYTES);
    }

    @Test
    @DisplayName("allow to clear memoized bytes any number of times")
    void clearAnyNumberOfTimes() throws IOException {
        byte[] input = randomBytes(4);

        stream.write(input);

        checkMemoized(input);
        checkMemoized(EMPTY_BYTES);

        stream.reset();
        stream.reset();

        checkMemoized(EMPTY_BYTES);
    }

    @Test
    @DisplayName("store only lower bits if `int`")
    void negatives() throws IOException {
        stream.write(-1);
        stream.write(-42);
        stream.write(10);
        stream.write(0);
        stream.write(MIN_VALUE);

        checkMemoized(new byte[]{(byte)-1, (byte)-42, (byte) 10, (byte) 0, MIN_VALUE});
    }

    private void checkMemoized(byte[] expected) throws IOException {
        ByteArrayOutputStream outputCollector = new ByteArrayOutputStream();
        stream.flushTo(outputCollector);
        byte[] actualBytes = outputCollector.toByteArray();

        assertThat(actualBytes)
                .asList()
                .containsExactlyElementsIn(asList(expected));
    }

    private static byte[] randomBytes(int count) {
        byte[] result = new byte[count];
        for (int i = 0; i < count; i++) {
            @SuppressWarnings("NumericCastThatLosesPrecision") // OK because of the bounds.
            byte randomByte = (byte) random(0, MAX_VALUE);
            result[i] = randomByte;
        }
        return result;
    }
}
