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

import com.google.common.collect.ImmutableSet;
import com.google.common.flogger.FluentLogger;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.protobuf.Message;
import io.spine.code.proto.FieldDeclaration;
import io.spine.code.proto.FieldName;
import io.spine.protobuf.Diff;
import io.spine.type.TypeName;
import io.spine.validate.option.SetOnce;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static io.spine.util.Exceptions.newIllegalArgumentException;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * This class provides general validation routines.
 */
@SuppressWarnings("ClassWithTooManyMethods") // OK for this utility class.
public final class Validate {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    /** Prevents instantiation of this utility class. */
    private Validate() {
    }

    /**
     * Verifies if the passed message object is its default state and is not {@code null}.
     *
     * @param object the message to inspect
     * @return true if the message is in the default state, false otherwise
     */
    public static boolean isDefault(Message object) {
        checkNotNull(object);
        boolean result = object.getDefaultInstanceForType()
                               .equals(object);
        return result;
    }

    /**
     * Verifies if the passed message object is not its default state and is not {@code null}.
     *
     * @param object the message to inspect
     * @return true if the message is not in the default state, false otherwise
     */
    public static boolean isNotDefault(Message object) {
        checkNotNull(object);
        boolean result = !isDefault(object);
        return result;
    }

    /**
     * Ensures that the passed message is not in the default state.
     *
     * @param message
     *         the message to check
     * @param <T>
     *         the type of the message
     * @return the passed message
     * @throws IllegalArgumentException
     *          if the passed message has the default state
     * @throws NullPointerException
     *          if the passed message is {@code null}
     */
    public static <T extends @NonNull Message> T checkNotDefaultArg(T message) {
        checkArgument(!isDefault(message));
        return message;
    }

    /**
     * Ensures that the passed message is not in the default state.
     *
     * @param message
     *         the message to check
     * @param <T>
     *         the type of the message
     * @return the passed message
     * @throws IllegalStateException
     *          if the passed message has the default state
     * @throws NullPointerException
     *          if the passed message is {@code null}
     */
    public static <T extends @NonNull Message> T checkNotDefaultState(T message) {
        checkState(!isDefault(message));
        return message;
    }

    /**
     * Ensures that the passed object is not in its default state and is not {@code null}.
     *
     * @param object       the {@code Message} instance to check
     * @param errorMessage the message for the exception to be thrown;
     *                     will be converted to a string using {@link String#valueOf(Object)}
     * @throws IllegalStateException if the object is in its default state
     */
    @CanIgnoreReturnValue
    public static <M extends Message> M checkNotDefault(M object, @Nullable Object errorMessage) {
        checkNotNull(object);
        checkState(isNotDefault(object), errorMessage);
        return object;
    }

    /**
     * Ensures that the passed object is not in its default state and is not {@code null}.
     *
     * @param object               the {@code Message} instance to check
     * @param errorMessageTemplate a template for the exception message should the check fail
     * @param errorMessageArgs     the arguments to be substituted into the message template
     * @throws IllegalStateException if the object is in its default state
     */
    @CanIgnoreReturnValue
    @SuppressWarnings("OverloadedVarargsMethod")
    public static <M extends Message> M checkNotDefault(M object,
                                                        String errorMessageTemplate,
                                                        Object... errorMessageArgs) {
        checkNotNull(object);
        checkNotNull(errorMessageTemplate);
        checkNotNull(errorMessageArgs);
        checkState(isNotDefault(object), errorMessageTemplate, errorMessageArgs);
        return object;
    }

    /**
     * Ensures that the passed object is not in its default state and is not {@code null}.
     *
     * @param object the {@code Message} instance to check
     * @throws IllegalStateException if the object is in its default state
     * @deprecated please use {@link #checkNotDefaultState(Message)} when intending to throw
     *  {@code IllegalStateException} or {@link #checkNotDefaultArg(Message)} when intending to
     *  throw {@code IllegalArgumentException}
     */
    @Deprecated
    @CanIgnoreReturnValue
    public static <M extends Message> M checkNotDefault(M object) {
        return checkNotDefaultState(object);
    }

    /**
     * Ensures that the passed object is in its default state and is not {@code null}.
     *
     * @param object       the {@code Message} instance to check
     * @param errorMessage the message for the exception to be thrown;
     *                     will be converted to a string using {@link String#valueOf(Object)}
     * @throws IllegalStateException if the object is not in its default state
     */
    @CanIgnoreReturnValue
    public static <M extends Message> M checkDefault(M object, @Nullable Object errorMessage) {
        checkNotNull(object);
        checkState(isDefault(object), errorMessage);
        return object;
    }

    /**
     * Ensures that the passed object is in its default state and is not {@code null}.
     *
     * @param object               the {@code Message} instance to check
     * @param errorMessageTemplate a template for the exception message should the check fail
     * @param errorMessageArgs     the arguments to be substituted into the message template
     * @throws IllegalStateException if the object is not in its default state
     */
    @CanIgnoreReturnValue
    @SuppressWarnings("OverloadedVarargsMethod")
    public static <M extends Message> M checkDefault(M object,
                                                     String errorMessageTemplate,
                                                     Object... errorMessageArgs) {
        checkNotNull(object);
        checkNotNull(errorMessageTemplate);
        checkNotNull(errorMessageArgs);
        checkState(isDefault(object), errorMessageTemplate, errorMessageArgs);
        return object;
    }

    /**
     * Ensures that the passed object is in its default state and is not {@code null}.
     *
     * @param object the {@code Message} instance to check
     * @throws IllegalStateException if the object is not in its default state
     */
    @CanIgnoreReturnValue
    public static <M extends Message> M checkDefault(M object) {
        checkNotNull(object);
        if (!isDefault(object)) {
            String typeName = TypeName.of(object)
                                      .value();
            throw newIllegalStateException("The message is not in the default state: %s", typeName);
        }
        return object;
    }

    /**
     * Ensures the truth of an expression involving one parameter to the calling method.
     *
     * @param expression         a boolean expression with the parameter we check
     * @param errorMessageFormat the format of the error message, which has {@code %s} placeholder
     *                           for the parameter name
     * @param parameterName      the name of the parameter
     * @throws IllegalArgumentException if {@code expression} is false
     */
    public static void checkParameter(boolean expression,
                                      String errorMessageFormat,
                                      String parameterName) {
        checkNotNull(errorMessageFormat);
        checkNotNull(parameterName);
        if (!expression) {
            throw newIllegalArgumentException(errorMessageFormat, parameterName);
        }
    }

    /**
     * Ensures that the passed string is not {@code null}, empty or blank string.
     *
     * @param stringToCheck the string to check
     * @param fieldName     the name of the string field
     * @return the passed string
     * @throws IllegalArgumentException if the string is empty or blank
     */
    @CanIgnoreReturnValue
    public static String checkNotEmptyOrBlank(String stringToCheck, String fieldName) {
        checkNotNull(stringToCheck);
        checkNotNull(fieldName);
        checkArgument(!stringToCheck.isEmpty(), "Field %s must not be an empty string.", fieldName);
        String trimmed = stringToCheck.trim();
        checkArgument(trimmed.length() > 0, "Field %s must not be a blank string.", fieldName);
        return stringToCheck;
    }

    /**
     * Ensures that the passed value is positive.
     *
     * @param value the value to check
     * @throws IllegalArgumentException if requirement is not met
     */
    public static void checkPositive(long value) {
        if (value <= 0) {
            throw newIllegalArgumentException("value (%d) must be positive", value);
        }
    }

    /**
     * Ensures that the passed value is positive.
     *
     * @param value        the value to check
     * @param argumentName the name of the checked value to be used in the error message
     * @throws IllegalArgumentException if requirement is not met
     */
    public static void checkPositive(long value, String argumentName) {
        checkNotNull(argumentName);
        checkArgument(value > 0L, "%s must be a positive value", argumentName);
    }

    /**
     * Ensures that target value is in between passed bounds.
     *
     * @param value     target value
     * @param paramName value name
     * @param lowBound  lower bound to check
     * @param highBound higher bound
     */
    public static void checkBounds(int value, String paramName, int lowBound, int highBound) {
        checkNotNull(paramName);
        if (!isBetween(value, lowBound, highBound)) {
            throw newIllegalArgumentException("%s (%d) should be in bounds [%d, %d] inclusive",
                                              paramName, value, lowBound, highBound);
        }
    }

    private static boolean isBetween(int value, int lowBound, int highBound) {
        return lowBound <= value && value <= highBound;
    }

    /**
     * Validates the given message according to its definition and throws
     * {@code ValidationException} if any constraints are violated.
     *
     * @throws ValidationException if the passed message does not satisfy the constraints
     *                             set for it in its Protobuf definition
     */
    public static void checkValid(Message message) throws ValidationException {
        checkNotNull(message);

        List<ConstraintViolation> violations = MessageValidator.newInstance(message)
                                                               .validate();
        if (!violations.isEmpty()) {
            throw new ValidationException(violations);
        }
    }

    /**
     * Checks that when transitioning a message state from {@code previous} to {@code current},
     * the {@code set_once} constrains are met.
     *
     * @param previous
     *         the previous state of the message
     * @param current
     *         the new state of the message
     * @param <M>
     *         the type of the message
     */
    public static <M extends Message> void checkValidChange(M previous, M current) {
        checkNotNull(previous);
        checkNotNull(current);

        Diff diff = Diff.between(previous, current);
        ImmutableSet<ConstraintViolation> setOnceViolations = current
                .getDescriptorForType()
                .getFields()
                .stream()
                .map(FieldDeclaration::new)
                .filter(Validate::isNonOverridable)
                .filter(diff::contains)
                .filter(field -> {
                    Object fieldValue = previous.getField(field.descriptor());
                    return !field.isDefault(fieldValue);
                })
                .map(Validate::violatedSetOnce)
                .collect(toImmutableSet());
        if (!setOnceViolations.isEmpty()) {
            throw new ValidationException(setOnceViolations);
        }
    }

    /**
     * Checks if the given field, once set, may not be changed.
     *
     * <p>This property is defined by the {@code (set_once)} option. If the option is set to
     * {@code true} on a non-{@code repeated} and non-{@code map} field, this field is
     * <strong>non-overridable</strong>.
     *
     * <p>Logs if the option is set but the field is {@code repeated} or a {@code map}.
     *
     * @param field
     *         the field to check
     * @return {@code true} if the field is neither {@code repeated} nor {@code map} and is
     *         {@code (set_once)}
     */
    private static boolean isNonOverridable(FieldDeclaration field) {
        checkNotNull(field);

        boolean marked = markedSetOnce(field);
        if (marked) {
            boolean setOnceInapplicable = field.isCollection();
            if (setOnceInapplicable) {
                onSetOnceMisuse(field);
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    private static boolean markedSetOnce(FieldDeclaration declaration) {
        Optional<Boolean> setOnceDeclaration = SetOnce.from(declaration.descriptor());
        boolean setOnceValue = setOnceDeclaration.orElse(false);
        boolean requiredByDefault = declaration.isEntityId()
                && !setOnceDeclaration.isPresent();
        return setOnceValue || requiredByDefault;
    }

    @SuppressWarnings("DuplicateStringLiteralInspection")
        // Usage in AbstractValidatingBuilder will be removed.
    private static void onSetOnceMisuse(FieldDeclaration field) {
        FieldName fieldName = field.name();
        logger.atSevere()
              .log("Error found in `%s`. " +
                           "Repeated and map fields cannot be marked as `(set_once) = true`.",
                   fieldName);
    }

    @SuppressWarnings("DuplicateStringLiteralInspection")
        // Usage in AbstractValidatingBuilder will be removed.
    private static ConstraintViolation violatedSetOnce(FieldDeclaration declaration) {
        TypeName declaringTypeName = declaration.declaringType().name();
        FieldName fieldName = declaration.name();
        ConstraintViolation violation = ConstraintViolation
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
        return violation;
    }
}
