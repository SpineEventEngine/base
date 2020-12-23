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

package io.spine.validate;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.Message;
import com.google.protobuf.ProtocolMessageEnum;
import io.spine.annotation.Internal;
import io.spine.code.proto.FieldContext;
import io.spine.code.proto.FieldDeclaration;
import io.spine.protobuf.Messages;
import io.spine.protobuf.TypeConverter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * A field value to validate.
 *
 * <p>The exact type of the value is unknown since it is set
 * by a user who applies a generated validating builder.
 *
 * <p>Map fields are considered in a special way, and only values are validated.
 * Keys don't require validation since they are of primitive types.
 *
 * @see <a href="https://developers.google.com/protocol-buffers/docs/proto3#maps">
 *         Protobuf Maps</a>
 */
@Immutable
@Internal
public final class FieldValue {

    /**
     * Actual field values.
     *
     * <p>Since a field can be, among other things, a repeated field or a map, the values are stored
     * in a list.
     *
     * <p>For singular fields, a list contains a single value.
     * For repeated fields, a list contains all values.
     * For a map fields, a list contains a list of values, since the map values are being validated,
     * not the keys.
     */
    @SuppressWarnings("Immutable")
    private final ImmutableList<?> values;
    private final FieldContext context;
    private final FieldDeclaration declaration;

    private FieldValue(Collection<?> values, FieldContext context, FieldDeclaration declaration) {
        this.values = ImmutableList.copyOf(values);
        this.context = context;
        this.declaration = declaration;
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
        FieldDescriptor fieldDescriptor = context.target();
        FieldDeclaration declaration = new FieldDeclaration(fieldDescriptor);

        FieldValue result = resolveType(declaration, context, value);
        return result;
    }

    /**
     * Returns a properly typed {@code FieldValue}.
     *
     * <p>To do so, performs a series of {@code instanceof} calls and casts, since there are no
     * common ancestors between all the possible value types ({@code Map} for Protobuf {@code map}
     * fields, {@code List} for {@code repeated} fields, and {@code T} for plain values).
     *
     * @return a properly typed {@code FieldValue} instance.
     */
    @SuppressWarnings("ChainOfInstanceofChecks")
    private static
    FieldValue resolveType(FieldDeclaration field, FieldContext context, Object value) {
        if (value instanceof List) {
            List<?> values = (List<?>) value;
            return new FieldValue(values, context, field);
        } else if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            return new FieldValue(map.values(), context, field);
        } else {
            return new FieldValue(ImmutableList.of(value), context, field);
        }
    }

    public FieldDescriptor descriptor() {
        return context.target();
    }

    /**
     * Obtains the {@link JavaType} of the value.
     *
     * <p>For a map, returns the type of the values.
     *
     * @return {@link JavaType} of the field elements
     */
    public JavaType javaType() {
        if (!declaration.isMap()) {
            return declaration.javaType();
        }
        JavaType result = declaration.valueDeclaration()
                                     .javaType();
        return result;
    }

    /**
     * Converts the value to a list.
     *
     * @return the value as a list
     * @deprecated Use {@link #values()} instead.
     */
    @Deprecated
    public final ImmutableList<?> asList() {
        return values;
    }

    /**
     * Creates a stream of all the values of this field.
     *
     * <p>If the field is singular, obtains the only value. If the field is {@code repeated},
     * obtains all the values of the list. If the field is a {@code map}, obtains all the map values
     * (but not the keys).
     */
    public final Stream<?> values() {
        return values.stream();
    }

    /**
     * Creates a stream of non-default values of this field.
     *
     * <p>Behaves similarly to {@link #values()} but also filters out default values.
     *
     * <p>{@code 0} number values, {@code false} boolean values, {@code 0}-number enum instances,
     * empty char and byte strings, and empty messages are considered default values.
     */
    public final Stream<?> nonDefault() {
        return values().filter(val -> !isDefault(val));
    }

    /**
     * Obtains the single value of this field.
     *
     * <p>If the field is a {@linkplain FieldDeclaration#isCollection() collection}, obtains
     * the first element. If the first element is not available, throws
     * an {@code IllegalStateException}.
     *
     * @return a single value of this field
     */
    public Object singleValue() {
        checkState(!values.isEmpty(),
                   "Unable to get the first element of an empty collection for the field `%s`.",
                   declaration());
        return values.get(0);
    }

    /** Returns {@code true} if this field is default, {@code false} otherwise. */
    public boolean isDefault() {
        return values.isEmpty() || allDefault();
    }

    private boolean allDefault() {
        return values.stream()
                     .allMatch(FieldValue::isDefault);
    }

    private static boolean isDefault(Object singleValue) {
        if (singleValue instanceof EnumValueDescriptor) {
            return ((EnumValueDescriptor) singleValue).getNumber() == 0;
        }
        Message thisAsMessage = TypeConverter.toMessage(singleValue);
        return Messages.isDefault(thisAsMessage);
    }

    /** Returns the declaration of the value. */
    public FieldDeclaration declaration() {
        return declaration;
    }

    /** Returns the context of the value. */
    public FieldContext context() {
        return context;
    }
}
