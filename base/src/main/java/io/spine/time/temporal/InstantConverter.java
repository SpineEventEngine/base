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

import com.google.common.base.Converter;
import com.google.protobuf.Timestamp;
import io.spine.annotation.Internal;

import java.io.Serializable;
import java.time.Instant;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Converts {@code Timestamp} to {@code Instant}.
 */
@Internal
public final class InstantConverter extends Converter<Instant, Timestamp>
        implements Serializable {

    private static final long serialVersionUID = 0L;
    private static final InstantConverter INSTANCE = new InstantConverter();

    public static Converter<Instant, Timestamp> instance() {
        return INSTANCE;
    }

    public static Converter<Timestamp, Instant> reversed() {
        return instance().reverse();
    }

    @Override
    protected Timestamp doForward(Instant value) {
        checkNotNull(value);
        Timestamp result = Timestamp
                .newBuilder()
                .setSeconds(value.getEpochSecond())
                .setNanos(value.getNano())
                .build();
        return result;
    }

    @Override
    protected Instant doBackward(Timestamp value) {
        checkNotNull(value);
        Instant result = Instant.ofEpochSecond(value.getSeconds(), value.getNanos());
        return result;
    }

    private Object readResolve() {
        return INSTANCE;
    }
}
