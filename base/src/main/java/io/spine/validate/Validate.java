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
import com.google.protobuf.Message;
import io.spine.annotation.Internal;
import io.spine.code.proto.FieldContext;
import io.spine.code.proto.FieldDeclaration;
import io.spine.code.proto.FieldName;
import io.spine.protobuf.Diff;
import io.spine.protobuf.MessageWithConstraints;
import io.spine.type.MessageType;
import io.spine.type.TypeName;
import io.spine.validate.option.SetOnce;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

/**
 * This class provides general validation routines.
 */
public final class Validate {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    /** Prevents instantiation of this utility class. */
    private Validate() {
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
     * @apiNote This method is used by the generated code, and as such needs to
     *         be {@code public}.
     */
    @Internal
    @SuppressWarnings("WeakerAccess") // see apiNote.
    public static List<ConstraintViolation> 
    validateAtRuntime(Message message, FieldContext context) {
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
