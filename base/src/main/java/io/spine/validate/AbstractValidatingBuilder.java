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
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import io.spine.annotation.Internal;
import io.spine.base.ConversionException;
import io.spine.code.proto.FieldContext;
import io.spine.code.proto.FieldDeclaration;
import io.spine.code.proto.FieldName;
import io.spine.logging.Logging;
import io.spine.protobuf.Messages;
import io.spine.reflect.GenericTypeIndex;
import io.spine.string.Stringifiers;
import io.spine.type.TypeName;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.getRootCause;
import static io.spine.protobuf.Messages.defaultInstance;
import static io.spine.util.Exceptions.illegalArgumentWithCauseOf;

/**
 * Serves as an abstract base for all {@linkplain ValidatingBuilder validating builders}.
 */
@Deprecated
@SuppressWarnings("DeprecatedIsStillUsed") // To be removed gradually.
public abstract class AbstractValidatingBuilder<T extends Message, B extends Message.Builder>
        implements ValidatingBuilder<T, B>, Logging {

    /**
     * The builder for the original {@code Message}.
     */
    private final B messageBuilder;

    /**
     * The class of the {@code Message} being built.
     */
    private final Class<T> messageClass;

    /**
     * The state of the message, serving as a base value for this {@code ValidatingBuilder}.
     *
     * <p>Used to verify if any modifications were made by a user via {@code ValidatingBuilder}
     * public API calls.
     *
     * <p>Has {@code null} value if not set via {@linkplain #setOriginalState(Message)
     * setOriginalState(..)}.
     */
    private @Nullable T originalState;

    protected AbstractValidatingBuilder() {
        this.messageClass = getMessageClass(getClass());
        this.messageBuilder = createBuilder();
    }

    @Override
    public T build() throws ValidationException {
        T message = internalBuild();
        validateResult(message);
        return message;
    }

    @Override
    public void clear() {
        messageBuilder.clear();
        originalState = null;
    }

    /**
     * Converts the passed `raw` value and returns it.
     *
     * @param value
     *         the value to convert
     * @param valueClass
     *         the {@code Class} of the value
     * @param <V>
     *         the type of the converted value
     * @return the converted value
     * @throws ConversionException
     *         if passed value cannot be converted
     */
    protected <V> V convert(String value, Class<V> valueClass) throws ConversionException {
        try {
            V convertedValue = Stringifiers.fromString(value, valueClass);
            return convertedValue;
        } catch (RuntimeException ex) {
            Throwable rootCause = getRootCause(ex);
            throw new ConversionException(ex.getMessage(), rootCause);
        }
    }

    /**
     * Converts the passed `raw` value to {@code Map}.
     *
     * <p>Acts as a shortcut to {@linkplain #convert(String, java.lang.Class) convert(String, Map)}.
     *
     * @param <K>
     *         the type of the {@code Map} keys
     * @param <V>
     *         the type of the {@code Map} values
     * @param value
     *         the value to convert
     * @param keyClass
     *         the {@code Class} of the key
     * @param valueClass
     *         the {@code Class} of the value
     * @return the converted value
     */
    protected static <K, V> Map<K, V> convertToMap(String value,
                                                   Class<K> keyClass,
                                                   Class<V> valueClass) {
        Map<K, V> result = Stringifiers.newForMapOf(keyClass, valueClass)
                                       .reverse()
                                       .convert(value);
        return result;
    }

    /**
     * Converts the passed `raw` value to {@code List}.
     *
     * <p>Acts as a shortcut to {@linkplain #convert(String, Class) convert(String, List)}.
     *
     * @param <V>
     *         the type of the {@code List} values
     * @param value
     *         the value to convert
     * @param valueClass
     *         the {@code Class} of the list values
     * @return the converted value
     */
    protected static <V> List<V> convertToList(String value, Class<V> valueClass) {
        List<V> result = Stringifiers.newForListOf(valueClass)
                                     .reverse()
                                     .convert(value);
        return result;
    }

    @Override
    public <V> void validate(FieldDescriptor descriptor, V fieldValue, String fieldName)
            throws ValidationException {
        FieldContext fieldContext = FieldContext.create(descriptor);
        FieldValue valueToValidate = FieldValue.of(fieldValue, fieldContext);
        FieldValidator<?> validator = valueToValidate.createValidator();
        List<ConstraintViolation> violations = validator.validate();
        checkViolations(violations);
    }

    /**
     * Checks whether any modifications have been made to the fields of message being built.
     *
     * @return {@code true} if any modifications have been made, {@code false} otherwise.
     */
    @Override
    public boolean isDirty() {
        T message = internalBuild();
        boolean result = originalState != null
                         ? !originalState.equals(message)
                         : Validate.isNotDefault(message);
        return result;
    }

    @Override
    public void setOriginalState(T state) {
        checkNotNull(state);
        this.originalState = state;

        messageBuilder.clear();
        messageBuilder.mergeFrom(state);
    }

    protected B getMessageBuilder() {
        return messageBuilder;
    }

    @Override
    @CanIgnoreReturnValue
    public ValidatingBuilder<T, B> mergeFrom(T message) {
        messageBuilder.mergeFrom(message);
        return this;
    }

    /**
     * Builds a message without triggering its validation.
     *
     * <p>Exposed to those who wish to obtain the state anyway, e.g. for logging.
     *
     * @return the message built from the values set by the user
     */
    @Internal
    public final T internalBuild() {
        @SuppressWarnings("unchecked")
        // OK, as real types of `B` are always generated to be compatible with `T`.
        T result = (T) getMessageBuilder().build();
        return result;
    }

    private B createBuilder() {
        @SuppressWarnings("unchecked")  // OK, since it is guaranteed by the class declaration.
        B result = (B) defaultInstance(messageClass).newBuilderForType();
        return result;
    }

    private void validateResult(T message) throws ValidationException {
        List<ConstraintViolation> violations = MessageValidator.newInstance(message)
                                                               .validate();
        checkViolations(violations);
    }

    /**
     * Validates the {@code set_once} field option.
     *
     * <p>A field can only be set once if it either declares an explicit option value
     * (e.g {@code string name = 2 [(set_once) = true];}), or if it's the first value of a an
     * entity message.
     *
     * <p>Example:
     * in the message
     * <pre>
     * {@code
     * message User {
     *     option (entity).kind = ENTITY;
     *
     *     string id = 2;
     *     string name = 1;
     * }
     * }
     * </pre>
     * the {@code id} field is implicitly {@code (set_once) = true}, since it's the first one
     * in the entity message, regardless of the declared number.
     * To avoid this, the declaration of the field should be
     * {@code string id = 1[(set_once) = false];}.
     *
     * @param newValue
     *         the new value of the field
     * @throws ValidationException
     *         if the value of the  field that is {@code (set_once) = true} is being changed
     */
    @SuppressWarnings("unused") // Called by all actual validating builder subclasses.
    protected final void validateSetOnce(FieldDescriptor field, Object newValue)
            throws ValidationException {
        checkNotNull(field);
        checkNotNull(newValue);

        FieldDeclaration declaration = new FieldDeclaration(field);
        boolean shouldValidate = setOnce(declaration);
        if (shouldValidate) {
            boolean setOnceInapplicable = declaration.isCollection();
            if (setOnceInapplicable) {
                onSetOnceMisuse(declaration);
            } else {
                checkNotOverriding(declaration, newValue);
            }
        }
    }

    private void checkNotOverriding(FieldDeclaration field, Object newValue)
            throws ValidationException {
        FieldDescriptor descriptor = field.descriptor();
        B builder = getMessageBuilder();
        boolean valueIsSet = builder.hasField(descriptor);
        if (valueIsSet) {
            Object currentValue = builder.getField(descriptor);
            boolean anotherValueSet = !currentValue.equals(newValue);
            if (anotherValueSet) {
                throw violatedSetOnce(field);
            }
        }
    }

    private static boolean setOnce(FieldDeclaration declaration) {
        Optional<Boolean> setOnceDeclaration = SetOnce.from(declaration.descriptor());
        boolean setOnceValue = setOnceDeclaration.orElse(false);
        boolean requiredByDefault = declaration.isEntityId()
                                 && !setOnceDeclaration.isPresent();
        return setOnceValue || requiredByDefault;
    }

    protected void checkNotSetOnce(FieldDescriptor descriptor) {
        boolean setOnce = SetOnce.from(descriptor)
                                 .orElse(false);
        if (setOnce) {
            FieldDeclaration declaration = new FieldDeclaration(descriptor);
            onSetOnceMisuse(declaration);
        }
    }

    private void onSetOnceMisuse(FieldDeclaration field) {
        FieldName fieldName = field.name();
        _error("Error found in `%s`. " +
                       "Repeated and map fields cannot be marked as `(set_once) = true`.",
               fieldName);
    }

    private static ValidationException violatedSetOnce(FieldDeclaration declaration) {
        TypeName declaringTypeName = declaration.declaringType().name();
        FieldName fieldName = declaration.name();
        ConstraintViolation setOnceViolation = ConstraintViolation
                .newBuilder()
                .setMsgFormat("Attempted to change the value of the field `%s.%s` which has " +
                                      "`(set_once) = true` and is already set.")
                .addParam(declaringTypeName.value())
                .addParam(fieldName.value())
                .setFieldPath(declaration.name()
                                         .asPath())
                .setTypeName(declaration.declaringType()
                                        .name()
                                        .value())
                .build();
        return new ValidationException(ImmutableList.of(setOnceViolation));
    }

    private static void checkViolations(List<ConstraintViolation> violations)
            throws ValidationException {
        if (!violations.isEmpty()) {
            throw new ValidationException(violations);
        }
    }

    /**
     * Obtains the class of the message produced by the builder.
     */
    private static <T extends Message> Class<T>
    getMessageClass(Class<? extends ValidatingBuilder> builderClass) {
        @SuppressWarnings("unchecked") // The type is ensured by the class declaration.
        Class<T> result = (Class<T>) GenericParameter.MESSAGE.argumentIn(builderClass);
        return result;
    }

    // as the method names are the same, but methods are different.

    /**
     * Obtains the raw method for creating new validating builder.
     *
     * <p>To simplify migration to Validating Builders, we use the same name which is used in
     * Protobuf for obtaining a {@code Message.Builder}.
     */
    static Method getNewBuilderMethod(Class<? extends ValidatingBuilder<?, ?>> cls) {
        try {
            return cls.getMethod(Messages.METHOD_NEW_BUILDER);
        } catch (NoSuchMethodException e) {
            throw illegalArgumentWithCauseOf(e);
        }
    }

    /**
     * Enumeration of generic type parameters of {@link ValidatingBuilder}.
     */
    private enum GenericParameter implements GenericTypeIndex<ValidatingBuilder> {

        /**
         * The index of the declaration of the generic parameter type {@code <T>}.
         */
        MESSAGE(0),

        /**
         * The index of the declaration of the generic parameter type {@code <B>}.
         */
        MESSAGE_BUILDER(1);

        private final int index;

        GenericParameter(int index) {
            this.index = index;
        }

        @Override
        public int index() {
            return this.index;
        }
    }
}
