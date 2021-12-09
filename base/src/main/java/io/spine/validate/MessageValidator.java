/*
 * Copyright 2021, TeamDev. All rights reserved.
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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.google.protobuf.Any;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import io.spine.base.Field;
import io.spine.code.proto.FieldContext;
import io.spine.code.proto.FieldDeclaration;
import io.spine.code.proto.FieldName;
import io.spine.type.MessageType;
import io.spine.validate.option.DistinctConstraint;
import io.spine.validate.option.GoesConstraint;
import io.spine.validate.option.IsRequiredConstraint;
import io.spine.validate.option.PatternConstraint;
import io.spine.validate.option.RangedConstraint;
import io.spine.validate.option.RequiredConstraint;
import io.spine.validate.option.RequiredFieldConstraint;
import io.spine.validate.option.ValidateConstraint;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static io.spine.protobuf.Messages.ensureMessage;
import static io.spine.protobuf.TypeConverter.toAny;
import static io.spine.util.Exceptions.newIllegalStateException;
import static io.spine.validate.MessageValue.atTopLevel;
import static io.spine.validate.MessageValue.nestedIn;
import static java.util.stream.Collectors.toList;

/**
 * Validates a given message according to the constraints.
 *
 * <p>The output result of this {@link ConstraintTranslator} is a {@link ValidationError}.
 */
@SuppressWarnings("OverlyCoupledClass")
final class MessageValidator implements ConstraintTranslator<Optional<ValidationError>> {

    private final MessageValue message;
    private final List<ConstraintViolation> violations;

    private MessageValidator(MessageValue message) {
        this.message = checkNotNull(message);
        this.violations = new ArrayList<>();
    }

    /**
     * Creates a new validator for the {@linkplain MessageValue#atTopLevel(Message)
     * top-level} {@code message}.
     */
    MessageValidator(Message message) {
        this(atTopLevel(message));
    }

    /**
     * Creates a new validator for the {@code message} with the specific field {@code context}.
     */
    MessageValidator(Message message, FieldContext context) {
        this(nestedIn(context, message));
    }

    @Override
    public void visitRange(RangedConstraint<?> constraint) {
        checkNotNull(constraint);
        var value = message.valueOf(constraint.field());
        var range = constraint.range();
        checkTypeConsistency(range, value);
        value.values()
             .map(Number.class::cast)
             .map(ComparableNumber::new)
             .filter(range.negate())
             .map(number -> violation(constraint, value, number.value()))
             .forEach(violations::add);
    }

    @Override
    public void visitRequired(RequiredConstraint constraint) {
        checkNotNull(constraint);
        if (constraint.optionValue()) {
            var fieldValue = message.valueOf(constraint.field());
            if (fieldValue.isDefault()) {
                violations.add(violation(constraint, fieldValue));
            }
        }
    }

    @Override
    public void visitPattern(PatternConstraint constraint) {
        checkNotNull(constraint);
        var fieldValue = message.valueOf(constraint.field());
        var regex = constraint.regex();
        var flags = constraint.flagsMask();
        @SuppressWarnings("MagicConstant")
        var compiledPattern = Pattern.compile(regex, flags);
        var partialMatch = constraint.allowsPartialMatch();
        fieldValue.nonDefault()
                  .filter(value -> partialMatch
                                   ? noPartialMatch(compiledPattern, (String) value)
                                   : noCompleteMatch(compiledPattern, (String) value))
                  .map(value -> violation(constraint, fieldValue, value)
                          .toBuilder()
                          .addParam(regex)
                          .build())
                  .forEach(violations::add);
    }

    private static boolean noCompleteMatch(Pattern pattern, String value) {
        var matcher = pattern.matcher(value);
        return !matcher.matches();
    }

    private static boolean noPartialMatch(Pattern pattern, String value) {
        var matcher = pattern.matcher(value);
        return !matcher.find();
    }

    @Override
    public void visitDistinct(DistinctConstraint constraint) {
        checkNotNull(constraint);
        var fieldValue = message.valueOf(constraint.field());
        var duplicates = findDuplicates(fieldValue);
        violations.addAll(
                duplicates.stream()
                          .map(duplicate -> violation(constraint, fieldValue, duplicate))
                          .collect(toImmutableList())
        );
    }

    @Override
    public void visitGoesWith(GoesConstraint constraint) {
        checkNotNull(constraint);
        var field = constraint.field();
        var value = message.valueOf(field);
        var declaration = withField(message, constraint);
        var withFieldName = constraint.optionValue().getWith();
        checkState(
                declaration.isPresent(),
                "The field `%s` specified in the `(goes).with` option is not found.",
                withFieldName
        );
        var withField = declaration.get();
        if (!value.isDefault() && fieldValueNotSet(withField)) {
            var withFieldNotSet = violation(constraint, value).toBuilder()
                    .addParam(field.name().value())
                    .addParam(withFieldName)
                    .build();
            violations.add(withFieldNotSet);
        }
    }

    @Override
    public void visitValidate(ValidateConstraint constraint) {
        checkNotNull(constraint);
        var fieldValue = message.valueOf(constraint.field());
        if (!fieldValue.isDefault()) {
            var childViolations = fieldValue
                    .values()
                    .map(val -> ensureMessage((Message) val))
                    .map(msg -> childViolations(fieldValue.context(), msg))
                    .flatMap(List::stream)
                    .collect(toList());
            if (!childViolations.isEmpty()) {
                var parentViolation = violation(constraint, fieldValue)
                        .toBuilder()
                        .addAllViolation(childViolations)
                        .build();
                violations.add(parentViolation);
            }
        }
    }

    @Override
    public void visitRequiredField(RequiredFieldConstraint constraint) {
        checkNotNull(constraint);
        var check = new RequiredFieldCheck(constraint.optionValue(),
                                           constraint.alternatives(),
                                           message);
        var violation = check.perform();
        violation.ifPresent(violations::add);
    }

    @Override
    public void visitRequiredOneof(IsRequiredConstraint constraint) {
        checkNotNull(constraint);
        var fieldValue = message.valueOf(constraint.declaration());
        var noneSet = fieldValue.isEmpty();
        if (noneSet) {
            var oneofName = constraint.oneofName();
            var oneofField = Field.named(oneofName.value());
            var targetType = constraint.targetType();
            var violation = ConstraintViolation.newBuilder()
                    .setMsgFormat(constraint.errorMessage(message.context()))
                    .setFieldPath(oneofField.path())
                    .setTypeName(targetType.name().value())
                    .build();
            violations.add(violation);
        }
    }

    @Override
    public void visitCustom(CustomConstraint constraint) {
        checkNotNull(constraint);
        var violations = constraint.validate(message);
        this.violations.addAll(violations);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Obtains the resulting {@link ValidationError} or an {@code Optional.empty()} if
     * the message value is valid.
     */
    @Override
    public Optional<ValidationError> translate() {
        if (violations.isEmpty()) {
            return Optional.empty();
        }
        var error = ValidationError
                .newBuilder()
                .addAllConstraintViolation(violations)
                .build();
        return Optional.of(error);
    }

    private static List<ConstraintViolation> childViolations(FieldContext field, Message message) {
        var messageValue = nestedIn(field, ensureMessage(message));
        var childInterpreter = new MessageValidator(messageValue);
        return Constraints
                .of(MessageType.of(message), field)
                .runThrough(childInterpreter)
                .map(ValidationError::getConstraintViolationList)
                .orElse(ImmutableList.of());
    }

    private static void checkTypeConsistency(Range<ComparableNumber> range, FieldValue value) {
        if (range.hasLowerBound() && range.hasUpperBound()) {
            var upper = range.upperEndpoint().toText();
            var lower = range.lowerEndpoint().toText();
            if (!upper.isOfSameType(lower)) {
                throw newIllegalStateException(
                        "Boundaries have inconsistent types: lower is `%s`, upper is `%s`.",
                        upper, lower
                );
            }
            checkBoundaryAndValue(upper, value);
        } else {
            checkSingleBoundary(range, value);
        }
    }

    private static void checkSingleBoundary(Range<ComparableNumber> range, FieldValue value) {
        var singleBoundary = range.hasLowerBound()
                             ? range.lowerEndpoint().toText()
                             : range.upperEndpoint().toText();
        checkBoundaryAndValue(singleBoundary, value);
    }

    private static void checkBoundaryAndValue(NumberText boundary, FieldValue value) {
        var boundaryNumber = boundary.toNumber();
        var valueNumber = (Number) value.singleValue();
        if (!NumberConversion.check(boundaryNumber, valueNumber)) {
            throw newIllegalStateException(
                    "Boundary values must have types consistent with the values they bind: " +
                            "boundary is `%s`, value is `%s`.",
                    boundary, valueNumber
            );
        }
    }

    private static ImmutableSet<?> findDuplicates(FieldValue fieldValue) {
        Set<? super Object> uniques = new HashSet<>();
        ImmutableSet.Builder<? super Object> duplicates = ImmutableSet.builder();
        fieldValue.values().forEach(potentialDuplicate -> {
            if (uniques.contains(potentialDuplicate)) {
                duplicates.add(potentialDuplicate);
            } else {
                uniques.add(potentialDuplicate);
            }
        });
        return duplicates.build();
    }

    private boolean fieldValueNotSet(FieldDeclaration field) {
        return message
                .valueOf(field.descriptor())
                .map(FieldValue::isDefault)
                .orElse(false);
    }

    private static Optional<FieldDeclaration>
    withField(MessageValue messageValue, GoesConstraint constraint) {
        var withField = FieldName.of(constraint.optionValue().getWith());
        for (var field : messageValue.declaration().fields()) {
            if (withField.equals(field.name())) {
                return Optional.of(field);
            }
        }
        return Optional.empty();
    }

    private static ConstraintViolation violation(Constraint constraint, FieldValue value) {
        return violation(constraint, value, null);
    }

    private static ConstraintViolation violation(Constraint constraint,
                                                 FieldValue value,
                                                 @Nullable Object violatingValue) {
        var context = value.context();
        var fieldPath = context.fieldPath();
        var typeName = constraint.targetType().name();
        var violation = ConstraintViolation.newBuilder()
                .setMsgFormat(constraint.errorMessage(context))
                .setFieldPath(fieldPath)
                .setTypeName(typeName.value());
        if (violatingValue != null) {
            violation.setFieldValue(toFieldValue(violatingValue));
        }
        return violation.build();
    }

    /**
     * Converts the {@code violatingValue} to a wrapped {@link Any}.
     *
     * <p>If the violation is caused by an enum, unwraps the enum value from the descriptor before
     * doing the conversion.
     */
    private static Any toFieldValue(Object violatingValue) {
        if (violatingValue instanceof Descriptors.EnumValueDescriptor) {
            return toAny(((Descriptors.EnumValueDescriptor) violatingValue).toProto());
        }
        return toAny(violatingValue);
    }
}
