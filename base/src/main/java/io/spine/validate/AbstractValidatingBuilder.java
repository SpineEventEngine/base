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

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import io.spine.annotation.Internal;
import io.spine.base.ConversionException;
import io.spine.protobuf.Messages;
import io.spine.string.Stringifiers;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.getRootCause;
import static io.spine.validate.FieldValidatorFactory.create;

/**
 * Serves as an abstract base for all {@linkplain ValidatingBuilder validating builders}.
 */
public abstract class AbstractValidatingBuilder<T extends Message, B extends Message.Builder>
        implements ValidatingBuilder<T, B> {

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
        this.messageClass = TypeInfo.getMessageClass(getClass());
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
    protected <K, V> Map<K, V> convertToMap(String value, Class<K> keyClass, Class<V> valueClass) {
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
    protected <V> List<V> convertToList(String value, Class<V> valueClass) {
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
        FieldValidator<?> validator = create(valueToValidate);
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
    public T internalBuild() {
        @SuppressWarnings("unchecked")
        // OK, as real types of `B` are always generated to be compatible with `T`.
        T result = (T) getMessageBuilder().build();
        return result;
    }

    private B createBuilder() {
        @SuppressWarnings("unchecked")  // OK, since it is guaranteed by the class declaration.
        B result = (B) Messages.newInstance(messageClass)
                               .newBuilderForType();
        return result;
    }

    private void validateResult(T message) throws ValidationException {
        List<ConstraintViolation> violations = MessageValidator.newInstance(message)
                                                               .validate();
        checkViolations(violations);
    }

    private static void checkViolations(List<ConstraintViolation> violations)
            throws ValidationException {
        if (!violations.isEmpty()) {
            throw new ValidationException(violations);
        }
    }
}
