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
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.ProtocolMessageEnum;
import io.spine.code.proto.FieldTypes2;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A field value to validate.
 *
 * <p>The exact type of the value is unknown since it is set
 * by a user using a generated validating builder.
 *
 * <p>Map fields are considered in a special way and only values are validated.
 * Keys don't require validation since they are of primitive types.
 *
 * @see <a href="https://developers.google.com/protocol-buffers/docs/proto3#maps">
 *         Protobuf Maps</a>
 */
class FieldValue {

    private final Object value;
    private final FieldDescriptor descriptor;
    private final FieldContext context;
    private final FieldDeclaration declaration;

    private FieldValue(Object value, FieldContext context) {
        this.value = value;
        this.descriptor = context.getTarget();
        this.context = context;
        this.declaration = new FieldDeclaration(context);
    }

    /**
     * Creates a new instance from the value.
     *
     * @param rawValue
     *         the value obtained via a validating builder
     * @param context
     *         the context of the field
     * @return a new instance
     */
    static FieldValue of(Object rawValue, FieldContext context) {
        checkNotNull(rawValue);
        checkNotNull(context);
        Object value = rawValue instanceof ProtocolMessageEnum
                       ? ((ProtocolMessageEnum) rawValue).getValueDescriptor()
                       : rawValue;
        return new FieldValue(value, context);
    }

    /**
     * Obtains the {@link JavaType} of the value.
     *
     * <p>For a map, returns the type of the values.
     *
     * @return {@link JavaType} of {@linkplain #asList() list} elements
     */
    JavaType javaType() {
        if (!isMap()) {
            return descriptor().getJavaType();
        }
        JavaType valuesType = FieldTypes2.valueDescriptor(descriptor())
                                         .getJavaType();
        return valuesType;
    }

    FieldDescriptor descriptor() {
        return descriptor;
    }

    FieldDeclaration declaration() {
        return declaration;
    }

    FieldContext context() {
        return context;
    }

    /**
     * Converts the value to a list.
     *
     * @param <T>
     *         the type of the list elements
     * @return the value as a list
     */
    @SuppressWarnings("unchecked" /* specific validator must call with its type */)
    <T> ImmutableList<T> asList() {
        if (declaration.isRepeated()) {
            List<T> result = (List<T>) value;
            return ImmutableList.copyOf(result);
        } else if (declaration.isMap()) {
            Map<?, T> map = (Map<?, T>) value;
            return ImmutableList.copyOf(map.values());
        } else {
            T result = (T) value;
            return ImmutableList.of(result);
        }
    }

    /**
     * Determines whether the field is a {@code map<k, v>}.
     *
     * @return {@code true} if the value is a map, {@code false} otherwise
     */
    private boolean isMap() {
        return FieldTypes2.isMap(descriptor);
    }
}
