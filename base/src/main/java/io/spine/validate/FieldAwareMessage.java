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

package io.spine.validate;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import io.spine.annotation.Experimental;
import io.spine.annotation.GeneratedMixin;

import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * The mixin for {@link Message Message}s which are aware of their fields and are able to provide
 * their values with no reflection {@linkplain Message#getField(FieldDescriptor) used by Protobuf}.
 *
 * <p>This mixin is an experimental part of the framework and may be changed or removed in future.
 */
@GeneratedMixin
@Experimental
public interface FieldAwareMessage extends Message {

    /**
     * Reads the value of the field.
     *
     * <p>By default, falls back to the {@linkplain Message#getField(FieldDescriptor) original
     * Protobuf method}.
     *
     * @param field
     *         descriptor of the field
     * @return field value
     */
    default Object readValue(FieldDescriptor field) {
        return getField(field);
    }

    /**
     * A test-only method that checks that the implementation of {@link #readValue(FieldDescriptor)}
     * gives the same results as the {@link Message#getField(FieldDescriptor)}.
     */
    @VisibleForTesting
    default boolean checkFieldsReachable() {
        Descriptors.Descriptor msgDescriptor = getDescriptorForType();
        List<FieldDescriptor> fields = msgDescriptor.getFields();

        for (FieldDescriptor field : fields) {
            Object value = getField(field);
            Object actual = readValue(field);
            boolean equals = Objects.equals(actual, value);
            checkArgument(equals, "" +
                    "`readValue(field)` is implemented incorrectly for the `%s` field in `%s`",
                          field, getClass());
        }
        return true;
    }
}
