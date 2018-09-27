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

package io.spine.testing.logging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;

import static com.google.common.primitives.Bytes.asList;
import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.TestValues.random;
import static java.lang.Byte.MAX_VALUE;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("MemoizingStream should")
class MemoizingStreamTest {

    private static final byte[] EMPTY_BYTES = {};

    @Test
    @DisplayName("flush all the input")
    void flushEverything() throws IOException {
        MemoizingStream stream = new MemoizingStream();
        byte[] input = randomBytes(42);

        stream.write(input);

        checkMemoized(stream, input);
    }

    @Test
    @DisplayName("not store flushed bytes")
    void clearAfterFlush() throws IOException {
        MemoizingStream stream = new MemoizingStream();
        byte[] input = randomBytes(12);

        stream.write(input);

        checkMemoized(stream, input);
        checkMemoized(stream, EMPTY_BYTES);
    }

    @Test
    @DisplayName("clear memoized bytes on demand")
    void clearOnDemand() throws IOException {
        MemoizingStream stream = new MemoizingStream();
        byte[] input = randomBytes(4);

        stream.write(input);
        stream.clear();

        checkMemoized(stream, EMPTY_BYTES);
    }

    @Test
    @DisplayName("clear memoized bytes idempotently")
    void clearIdempotently() throws IOException {
        MemoizingStream stream = new MemoizingStream();
        byte[] input = randomBytes(4);

        stream.write(input);

        checkMemoized(stream, input);
        checkMemoized(stream, EMPTY_BYTES);

        stream.clear();
        stream.clear();

        checkMemoized(stream, EMPTY_BYTES);
    }

    @Test
    @DisplayName("store 1 MiB of data")
    void capacity() throws IOException {
        int mebiByte = 1024 * 1024;
        byte[] input = randomBytes(mebiByte);

        MemoizingStream stream = new MemoizingStream();
        stream.write(input);

        assertThrows(BufferOverflowException.class, () -> stream.write(42));
        checkMemoized(stream, input);
    }

    private static void checkMemoized(MemoizingStream stream, byte[] expected)
            throws IOException {
        ByteArrayOutputStream outputCollector = new ByteArrayOutputStream();
        stream.flushTo(outputCollector);
        byte[] actualBytes = outputCollector.toByteArray();

        assertThat(actualBytes).asList().containsAllIn(asList(expected));
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
