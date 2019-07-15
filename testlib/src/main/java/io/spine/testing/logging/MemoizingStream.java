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

import com.google.common.annotations.VisibleForTesting;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An {@link OutputStream} which stores its input.
 */
final class MemoizingStream extends OutputStream {

    private static final int ONE_MEBI_BYTE = 1024 * 1024;
    private final ByteArrayOutputStream memory;

    MemoizingStream() {
        super();
        memory = new ByteArrayOutputStream(ONE_MEBI_BYTE);
    }

    @Override
    public void write(int b) {
        memory.write(b);
    }

    @VisibleForTesting
    long size() {
        return memory.size();
    }

    /**
     * Clears the memoized input.
     */
    void reset() {
        memory.reset();
    }

    /**
     * Copies the memoized input into the given stream and {@linkplain #reset() clears} memory.
     *
     * @param stream the target stream
     * @throws IOException if the target stream throws an {@link IOException} on a write operation
     */
    synchronized void flushTo(OutputStream stream) throws IOException {
        byte[] bytes = memory.toByteArray();
        stream.write(bytes);
        reset();
    }
}
