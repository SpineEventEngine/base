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
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.Message;
import com.google.protobuf.ProtocolMessageEnum;
import io.spine.code.proto.FieldContext;
import io.spine.code.proto.FieldDeclaration;
import io.spine.protobuf.TypeConverter;

import java.util.List;
import java.util.Map;

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
public final class FieldValue<T> {

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
    private final List<T> values;
    private final FieldContext context;
    private final FieldDeclaration declaration;

    private FieldValue(List<T> values, FieldContext context, FieldDeclaration declaration) {
        this.values = values;
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
    // Object to T is always safe since validating builders only receive `T`s.
    @SuppressWarnings("unchecked")
    static <T> FieldValue<T> of(Object rawValue, FieldContext context) {
        checkNotNull(rawValue);
        checkNotNull(context);
        T value = rawValue instanceof ProtocolMessageEnum
                  ? (T) ((ProtocolMessageEnum) rawValue).getValueDescriptor()
                  : (T) rawValue;
        FieldDescriptor fieldDescriptor = context.target();
        FieldDeclaration declaration = new FieldDeclaration(fieldDescriptor);

        FieldValue<T> result = resolveType(declaration, context, value);
        return result;
    }

    /**
     * Returns a properly typed {@code FieldValue}.
     *
     * <p>To do so, performs a series of {@code instanceof} calls and casts, since there are no
     * common ancestors between all the possible value types ({@code Map} for Protobuf {@code map}
     * fields, {@code List} for {@code repeated} fields, and {@code T} for plain values).
     *
     * Casting to {@code T} is safe because the {@code FieldValue} is always created by the
     * {@linkplain io.spine.validate.ValidatingBuilder validating builder} implementors, and the
     * raw value always corresponds to one of the Protobuf field types.
     *
     * @return a properly typed {@code FieldValue} instance.
     */
    @SuppressWarnings({
            "unchecked", // Raw value is always of a correct type, see Javadoc for details.
            "ChainOfInstanceofChecks" // No common ancestors.
    })
    private static <T> FieldValue<T> resolveType(FieldDeclaration field,
                                                 FieldContext context,
                                                 T value) {
        if (value instanceof List) {
            List<T> values = (List<T>) value;
            return new FieldValue<>(values, context, field);
        } else if (value instanceof Map) {
            Map<?, T> map = (Map<?, T>) value;
            ImmutableList<T> values = ImmutableList.copyOf(map.values());
            return new FieldValue<>(values, context, field);
        } else {
            return new FieldValue<>(ImmutableList.of(value), context, field);
        }
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
                return new MessageFieldValidator(castThis(), assumeRequired);
            case INT:
                return new IntegerFieldValidator(castThis());
            case LONG:
                return new LongFieldValidator(castThis());
            case FLOAT:
                return new FloatFieldValidator(castThis());
            case DOUBLE:
                return new DoubleFieldValidator(castThis());
            case STRING:
                return new StringFieldValidator(castThis(), assumeRequired);
            case BYTE_STRING:
                return new ByteStringFieldValidator(castThis());
            case BOOLEAN:
                return new BooleanFieldValidator(castThis());
            case ENUM:
                return new EnumFieldValidator(castThis());
            default:
                throw fieldTypeIsNotSupported(fieldType);
        }
    }

    /**
     * Casts this value to a more accurately typed {@code FieldValue}.
     */
    @SuppressWarnings("unchecked"
            /* Casting is safe since {@link JavaType}, that is being checked by
            * `#createValidator()` maps 1 to 1 to all `FieldValidator` subclasses, i.e. there
            * is always going to be fitting validator.
            */
    )
    private <S> FieldValue<S> castThis() {
        return (FieldValue<S>) this;
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
    public ImmutableList<T> asList() {
        return ImmutableList.copyOf(values);
    }

    public T singleValue() {
        return asList().get(0);
    }

    /** Returns {@code true} if this field is default, {@code false} otherwise. */
    public boolean isDefault() {
        return asList().isEmpty() || (declaration.isNotCollection() &&
                isSingleValueDefault());
    }

    @SuppressWarnings("OverlyStrongTypeCast") // Casting to a sensible public class.
    private boolean isSingleValueDefault() {
        if (this.singleValue() instanceof EnumValueDescriptor) {
            return ((EnumValueDescriptor) this.singleValue()).getNumber() == 0;
        }
        Message thisAsMessage = TypeConverter.toMessage(singleValue());
        return Validate.isDefault(thisAsMessage);
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
