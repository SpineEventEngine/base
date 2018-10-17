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

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static com.google.common.collect.Lists.newArrayListWithExpectedSize;

/**
 * An {@link OutputStream} which stores its input.
 */
final class MemoizingStream extends OutputStream {

    private static final int ONE_MEBI_BYTE = 10 * 1024 * 1024;

    private final List<Byte> memory = newArrayListWithExpectedSize(ONE_MEBI_BYTE);

    @Override
    public void write(int b) {
        /*
           According to the `OutputStream` contract, a negative value may represent the end of
           the stream. The actual data is the lowest 8 bits of the int.
         */
        if (b >= 0) {
            @SuppressWarnings("NumericCastThatLosesPrecision")
                // Adheres to the OutputStream contract.
            byte byteValue = (byte) b;
            memory.add(byteValue);
        }
    }

    /**
     * Clears the memoized input.
     */
    void clear() {
        memory.clear();
    }

    /**
     * Copies the memoized input into the given stream and {@linkplain #clear() clears} memory.
     *
     * @param stream the target stream
     * @throws IOException if the target stream throws an {@link IOException} on a write operation
     */
    void flushTo(OutputStream stream) throws IOException {
        byte[] buffer = new byte[memory.size()];
        for (int i = 0; i < memory.size(); i++) {
            buffer[i] = memory.get(i);
        }
        stream.write(buffer);
        clear();
    }
}
