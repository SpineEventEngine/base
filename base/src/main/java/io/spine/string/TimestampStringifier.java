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

package io.spine.string;

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;

import java.text.ParseException;

import static io.spine.util.Exceptions.newIllegalArgumentException;

/**
 * The stringifier of timestamps into RFC 3339 date string format.
 */
final class TimestampStringifier extends SerializableStringifier<Timestamp> {

    private static final long serialVersionUID = 0L;
    private static final TimestampStringifier INSTANCE = new TimestampStringifier();

    private TimestampStringifier() {
        super("Stringifiers.forTimestamp()");
    }

    static TimestampStringifier getInstance() {
        return INSTANCE;
    }

    @Override
    protected String toString(Timestamp value) {
        return Timestamps.toString(value);
    }

    @Override
    protected Timestamp fromString(String str) {
        try {
            return Timestamps.parse(str);
        } catch (ParseException e) {
            throw newIllegalArgumentException(e.getMessage(), e);
        }
    }

    private Object readResolve() {
        return INSTANCE;
    }
}
