/*
 * Copyright 2020, TeamDev. All rights reserved.
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
import com.google.common.collect.ImmutableSet;
import com.google.common.flogger.FluentLogger;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.protobuf.Message;
import io.spine.annotation.Internal;
import io.spine.code.proto.FieldContext;
import io.spine.code.proto.FieldDeclaration;
import io.spine.code.proto.FieldName;
import io.spine.protobuf.Diff;
import io.spine.protobuf.MessageWithConstraints;
import io.spine.protobuf.Messages;
import io.spine.type.MessageType;
import io.spine.type.TypeName;
import io.spine.util.Preconditions2;
import io.spine.validate.option.SetOnce;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.protobuf.TextFormat.shortDebugString;
import static io.spine.util.Exceptions.newIllegalArgumentException;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * This class provides general validation routines.
 */
@SuppressWarnings("ClassWithTooManyMethods") // Many deprecated methods will be removed in the future.
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
     * @deprecated please use {@link Messages#isDefault(Message)}
     */
    @Deprecated
    public static boolean isDefault(Message object) {
        return Messages.isDefault(object);
    }

    /**
     * Verifies if the passed message object is not its default state and is not {@code null}.
     *
     * @param object the message to inspect
     * @return true if the message is not in the default state, false otherwise
     * @deprecated please use {@link Messages#isNotDefault(Message)}
     */
    @Deprecated
    public static boolean isNotDefault(Message object) {
        return Messages.isNotDefault(object);
    }

    /**
     * Ensures that the passed object is not in its default state and is not {@code null}.
     *
     * @param object
     *         the {@code Message} instance to check
     * @param errorMessage
     *         the message for the exception to be thrown;
     *         will be converted to a string using {@link String#valueOf(Object)}
     * @throws IllegalStateException
     *         if the object is in its default state
     * @deprecated please use {@link Preconditions2#checkNotDefaultArg(Message, Object)} or
     *         {@link Preconditions2#checkNotDefaultState(Message, Object)}
     */
    @Deprecated
    @CanIgnoreReturnValue
    public static <M extends Message> M checkNotDefault(M object, @Nullable Object errorMessage) {
        return Preconditions2.checkNotDefaultState(object, errorMessage);
    }

    /**
     * Ensures that the passed object is not in its default state and is not {@code null}.
     *
     * @param object               the {@code Message} instance to check
     * @param errorMessageTemplate a template for the exception message should the check fail
     * @param errorMessageArgs     the arguments to be substituted into the message template
     * @throws IllegalStateException if the object is in its default state
     * @deprecated please use {@link Preconditions2#checkNotDefaultState(Message, String, Object...)}
     */
    @Deprecated
    @CanIgnoreReturnValue
    @SuppressWarnings("OverloadedVarargsMethod")
    public static <M extends Message> M checkNotDefault(M object,
                                                        String errorMessageTemplate,
                                                        Object... errorMessageArgs) {
        checkNotNull(object);
        checkNotNull(errorMessageTemplate);
        checkNotNull(errorMessageArgs);
        return Preconditions2.checkNotDefaultState(object, errorMessageTemplate, errorMessageArgs);
    }

    /**
     * Ensures that the passed object is not in its default state and is not {@code null}.
     *
     * @param object
     *         the {@code Message} instance to check
     * @throws IllegalStateException
     *         if the object is in its default state
     * @deprecated please use {@link Preconditions2#checkNotDefaultArg(Message)} or
     *             {@link Preconditions2#checkNotDefaultState(Message)}
     */
    @Deprecated
    @CanIgnoreReturnValue
    public static <M extends Message> M checkNotDefault(M object) {
        return Preconditions2.checkNotDefaultState(object);
    }

    /**
     * Ensures that the passed object is in its default state and is not {@code null}.
     *
     * @param object       the {@code Message} instance to check
     * @param errorMessage the message for the exception to be thrown;
     *                     will be converted to a string using {@link String#valueOf(Object)}
     * @throws IllegalStateException if the object is not in its default state
     * @deprecated If you really need to check that a message is in the default state,
     *             please use {@code checkState(isDefault(object), errorMessage); }
     */
    @Deprecated
    @CanIgnoreReturnValue
    public static <M extends Message> M checkDefault(M object, @Nullable Object errorMessage) {
        checkNotNull(object);
        checkState(Messages.isDefault(object), errorMessage);
        return object;
    }

    /**
     * Ensures that the passed object is in its default state and is not {@code null}.
     *
     * @param object               the {@code Message} instance to check
     * @param errorMessageTemplate a template for the exception message should the check fail
     * @param errorMessageArgs     the arguments to be substituted into the message template
     * @throws IllegalStateException if the object is not in its default state
     * @deprecated If you really need to check that a message is in the default state,
     *             please use {@code checkState(isDefault(object), template, arg1, arg2, arg3); }
     */
    @Deprecated
    @CanIgnoreReturnValue
    @SuppressWarnings("OverloadedVarargsMethod")
    public static <M extends Message> M checkDefault(M object,
                                                     String errorMessageTemplate,
                                                     Object... errorMessageArgs) {
        checkNotNull(object);
        checkNotNull(errorMessageTemplate);
        checkNotNull(errorMessageArgs);
        checkState(Messages.isDefault(object), errorMessageTemplate, errorMessageArgs);
        return object;
    }

    /**
     * Ensures that the passed object is in its default state and is not {@code null}.
     *
     * @param object the {@code Message} instance to check
     * @throws IllegalStateException if the object is not in its default state
     * @deprecated If you really need to check that a message is its default state,
     *  please use {@code checkState(isDefault(msg));}
     */
    @Deprecated
    @CanIgnoreReturnValue
    public static <M extends Message> M checkDefault(M object) {
        checkNotNull(object);
        if (!Messages.isDefault(object)) {
            throw newIllegalStateException(
                    "The message is not in the default state: `%s`.", shortDebugString(object)
            );
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
     * @deprecated please use
     * {@link com.google.common.base.Preconditions#checkArgument(boolean, String, Object...)}
     */
    @Deprecated
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
     * @param stringToCheck
     *         the string to check
     * @param fieldName
     *         the name of the string field
     * @return the passed string
     * @throws IllegalArgumentException
     *         if the string is empty or blank
     * @throws NullPointerException
     *         if the passed string is {@code null}
     * @deprecated please use {@link Preconditions2#checkNotEmptyOrBlank(String, String, Object...)}
     */
    @Deprecated
    @CanIgnoreReturnValue
    public static String checkNotEmptyOrBlank(String stringToCheck, String fieldName) {
        checkNotNull(stringToCheck);
        checkNotNull(fieldName);
        return Preconditions2.checkNotEmptyOrBlank(stringToCheck, fieldName);
    }

    /**
     * Ensures that the passed value is positive.
     *
     * @param value the value to check
     * @throws IllegalArgumentException if requirement is not met
     * @deprecated please use {@link Preconditions2#checkPositive(long)}
     */
    @Deprecated
    public static void checkPositive(long value) {
        Preconditions2.checkPositive(value);
    }

    /**
     * Ensures that the passed value is positive.
     *
     * @param value        the value to check
     * @param argumentName the name of the checked value to be used in the error message
     * @throws IllegalArgumentException if requirement is not met
     * @deprecated please use {@link Preconditions2#checkPositive(long, String, Object...)}
     */
    @Deprecated
    public static void checkPositive(long value, String argumentName) {
        checkNotNull(argumentName);
        checkArgument(value > 0L, "`%s` must be a positive value.", argumentName);
    }

    /**
     * Ensures that target value is in between passed bounds.
     *
     * @param value     target value
     * @param paramName value name
     * @param lowBound  lower bound to check
     * @param highBound higher bound
     * @deprecated please use {@link Preconditions2#checkBounds(int, String, int, int)}
     */
    @Deprecated
    public static void checkBounds(int value, String paramName, int lowBound, int highBound) {
        Preconditions2.checkBounds(value, paramName, lowBound, highBound);
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
        List<ConstraintViolation> violations = violationsOf(message);
        if (!violations.isEmpty()) {
            throw new ValidationException(violations);
        }
    }

    /**
     * Validates the given message according to its definition and returns the constrain violations,
     * if any.
     *
     * @return violations of the validation rules or an empty list if the message is valid
     */
    public static List<ConstraintViolation> violationsOf(Message message) {
        return message instanceof MessageWithConstraints
               ? ((MessageWithConstraints) message).validate()
               : validateAtRuntime(message);
    }

    private static List<ConstraintViolation> validateAtRuntime(Message message) {
        return validateAtRuntime(message, FieldContext.empty());
    }

    /**
     * Validates the given message ignoring the generated validation code.
     *
     * <p>Use {@link #violationsOf(Message)} over this method. It is declared {@code public} only
     * to be accessible in the generated code.
     *
     * @param message
     *         the message to validate
     * @param context
     *         the validation field context
     * @return violations of the validation rules or an empty list if the message is valid
     */
    @Internal
    public static List<ConstraintViolation> validateAtRuntime(Message message,
                                                              FieldContext context) {
        Optional<ValidationError> error =
                Constraints.of(MessageType.of(message), context)
                           .runThrough(new MessageValidator(message, context));
        List<ConstraintViolation> violations =
                error.map(ValidationError::getConstraintViolationList)
                     .orElse(ImmutableList.of());
        return violations;
    }

    /**
     * Validates the given message according to custom validation constraints.
     *
     * <p>If there are user-defined {@link io.spine.validate.option.ValidatingOptionFactory} in
     * the classpath, they are used to create validating options and assemble constraints. If there
     * are no such factories, this method always returns an empty list.
     *
     * @param message
     *         the message to validate
     * @return a list of violations; an empty list if the message is valid
     */
    public static List<ConstraintViolation> violationsOfCustomConstraints(Message message) {
        checkNotNull(message);
        Optional<ValidationError> error =
                Constraints.onlyCustom(MessageType.of(message), FieldContext.empty())
                           .runThrough(new MessageValidator(message));
        List<ConstraintViolation> violations =
                error.map(ValidationError::getConstraintViolationList)
                     .orElse(ImmutableList.of());
        return violations;
    }

    /**
     * Checks that when transitioning a message state from {@code previous} to {@code current},
     * the {@code set_once} constrains are met and throws a {@link ValidationException} if
     * the value transition is not valid.
     *
     * @param previous
     *         the previous state of the message
     * @param current
     *         the new state of the message
     * @param <M>
     *         the type of the message
     * @throws ValidationException
     *          if the value transition is not valid
     */
    public static <M extends Message> void checkValidChange(M previous, M current) {
        checkNotNull(previous);
        checkNotNull(current);
        ImmutableSet<ConstraintViolation> setOnceViolations = validateChange(previous, current);
        if (!setOnceViolations.isEmpty()) {
            throw new ValidationException(setOnceViolations);
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
     * @return list of constraint violations, if the transaction is invalid, an empty list otherwise
     */
    public static <M extends Message> ImmutableSet<ConstraintViolation>
    validateChange(M previous, M current) {
        checkNotNull(previous);
        checkNotNull(current);

        Diff diff = Diff.between(previous, current);
        ImmutableSet<ConstraintViolation> violations = current
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
        return violations;
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
        return setOnceValue;
    }

    private static void onSetOnceMisuse(FieldDeclaration field) {
        FieldName fieldName = field.name();
        logger.atSevere()
              .log("Error found in `%s`. " +
                           "Repeated and map fields cannot be marked as `(set_once) = true`.",
                   fieldName);
    }

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