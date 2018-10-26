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

package io.spine.validate;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.ProtocolMessageEnum;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A field value to validate.
 *
 * <p>The exact type of the value is unknown since it is set
 * by a user using a generated validating builder.
 */
class FieldValue {

    private final Object value;

    private FieldValue(Object value) {
        this.value = value;
    }

    /**
     * Creates a new instance from the value.
     *
     * @param rawValue
     *         the value obtained via a validating builder
     * @return a new instance
     */
    static FieldValue of(Object rawValue) {
        checkNotNull(rawValue);
        Object value = rawValue instanceof ProtocolMessageEnum
                       ? ((ProtocolMessageEnum) rawValue).getValueDescriptor()
                       : rawValue;
        return new FieldValue(value);
    }

    Object value() {
        return value;
    }

    @SuppressWarnings({
            "unchecked"               /* specific validator must call with its type */,
            "ChainOfInstanceofChecks" /* because fields do not have common parent class */
    })
    <T> ImmutableList<T> asList() {
        if (value instanceof List) {
            List<T> result = (List<T>) value;
            return ImmutableList.copyOf(result);
        } else if (value instanceof Map) {
            Map<?, T> map = (Map<?, T>) value;
            return ImmutableList.copyOf(map.values());
        } else {
            T result = (T) value;
            return ImmutableList.of(result);
        }
    }
}
