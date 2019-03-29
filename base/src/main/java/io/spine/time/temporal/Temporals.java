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

package io.spine.time.temporal;

import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import io.spine.annotation.Internal;
import io.spine.type.TypeName;

import static java.lang.String.format;

/**
 * A factory of {@link Temporal} instances.
 */
@Internal
public final class Temporals {

    /**
     * Prevents the utility class instantiation.
     */
    private Temporals() {
    }

    /**
     * Produces an instance of {@link Temporal} from the given message.
     *
     * <p>If the given message is a {@link Timestamp}, produces a {@link TimestampTemporal}.
     * If the given message is a {@link Temporal}, returns it without a change. Otherwise, throws
     * an {@code IllegalArgumentException}.
     *
     * @param value
     *         message to convert
     * @return instance of {@link Temporal}
     */
    public static Temporal<?> from(Message value) {
        if (value instanceof Temporal) {
            return (Temporal<?>) value;
        } else if (value instanceof Timestamp) {
            Timestamp timestampValue = (Timestamp) value;
            return TimestampTemporal.from(timestampValue);
        } else {
            throw new IllegalArgumentException(format("Type `%s` cannot represent a point in time.",
                                                      TypeName.of(value)));
        }
    }
}
