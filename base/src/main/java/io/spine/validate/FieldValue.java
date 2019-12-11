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
import static java.lang.String.format;

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

    FieldValidator<?> createValidator() {
        return createValidator(false);
    }

    public FieldValidator<?> createValidatorAssumingRequired() {
        return createValidator(true);
    }

    /**
     * Creates a new validator instance according to the type of the value.
     *
     * @param assumeRequired
     *         if {@code true} validators would always assume that the field is required even
     *         if the constraint is not set explicitly
     */
    @SuppressWarnings("OverlyComplexMethod")
    private FieldValidator<?> createValidator(boolean assumeRequired) {
        JavaType fieldType = javaType();
        switch (fieldType) {
            case MESSAGE:
                return new MessageFieldValidator(this, assumeRequired);
            case INT:
                return new IntegerFieldValidator(this);
            case LONG:
                return new LongFieldValidator(this);
            case FLOAT:
                return new FloatFieldValidator(this);
            case DOUBLE:
                return new DoubleFieldValidator(this);
            case STRING:
                return new StringFieldValidator(this, assumeRequired);
            case BYTE_STRING:
                return new ByteStringFieldValidator(this);
            case BOOLEAN:
                return new BooleanFieldValidator(this);
            case ENUM:
                return new EnumFieldValidator(this);
            default:
                throw fieldTypeIsNotSupported(fieldType);
        }
    }

    private static IllegalArgumentException fieldTypeIsNotSupported(JavaType type) {
        String msg = format("The field type is not supported for validation: %s", type);
        throw new IllegalArgumentException(msg);
    }

    public FieldDescriptor descriptor() {
        return context.target();
    }

    /**
     * Obtains the {@link JavaType} of the value.
     *
     * <p>For a map, returns the type of the values.
     *
     * @return {@link JavaType} of {@linkplain #asList() list} elements
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
     */
    public final ImmutableList<?> asList() {
        return values;
    }

    public final Stream<?> nonDefault() {
        return values
                .stream()
                .filter(val -> !isDefault(val));
    }

    public Object singleValue() {
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
