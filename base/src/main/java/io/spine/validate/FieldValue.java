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
import com.google.protobuf.DescriptorProtos.FieldOptions;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import com.google.protobuf.ProtocolMessageEnum;
import io.spine.code.proto.FieldDeclaration;
import io.spine.code.proto.Option;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.validate.rule.ValidationRuleOptions.getOptionValue;
import static java.lang.String.format;

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
final class FieldValue {

    private final Object value;
    private final FieldContext context;
    private final FieldDeclaration declaration;

    private FieldValue(Object value, FieldContext context, FieldDeclaration declaration) {
        this.value = value;
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
        FieldDescriptor fieldDescriptor = context.getTarget();
        FieldDeclaration declaration = new FieldDeclaration(fieldDescriptor);
        return new FieldValue(value, context, declaration);
    }

    FieldValidator<?> createValidator() {
        return createValidator(false);
    }

    FieldValidator<?> createValidatorAssumingRequired() {
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

    /**
     * Obtains the {@link JavaType} of the value.
     *
     * <p>For a map, returns the type of the values.
     *
     * @return {@link JavaType} of {@linkplain #asList() list} elements
     */
    JavaType javaType() {
        if (!declaration.isMap()) {
            return declaration.javaType();
        }
        JavaType result = declaration.valueDeclaration()
                                     .javaType();
        return result;
    }

    /**
     * Obtains the desired option for the field.
     *
     * @param option
     *         an extension key used to obtain an option
     * @param <T>
     *         the type of the option value
     */
    <T> Option<T> option(GeneratedExtension<FieldOptions, T> option) {
        Optional<Option<T>> validationRuleOption = getOptionValue(context, option);
        if (validationRuleOption.isPresent()) {
            return validationRuleOption.get();
        }

        Option<T> result = Option.from(context.getTarget(), option);
        return result;
    }

    /**
     * Obtains the value of the option.
     *
     * @param option
     *         an extension key used to obtain an option
     * @param <T>
     *         the type of the option value
     * @return the value of the option
     */
    <T> T valueOf(GeneratedExtension<FieldOptions, T> option) {
        return option(option).value();
    }

    /**
     * Converts the value to a list.
     *
     * @param <T>
     *         the type of the list elements
     * @return the value as a list
     */
    @SuppressWarnings({
            "unchecked", // Specific validator must call with its type.
            "ChainOfInstanceofChecks" // No other possible way to check the value type.
    })
    <T> ImmutableList<T> asList() {
        if (value instanceof Collection) {
            Collection<T> result = (Collection<T>) value;
            return ImmutableList.copyOf(result);
        } else if (value instanceof Map) {
            Map<?, T> map = (Map<?, T>) value;
            return ImmutableList.copyOf(map.values());
        } else {
            T result = (T) value;
            return ImmutableList.of(result);
        }
    }

    /** Returns the declaration of the value. */
    FieldDeclaration declaration() {
        return declaration;
    }

    /** Returns the context of the value. */
    FieldContext context() {
        return context;
    }
}
