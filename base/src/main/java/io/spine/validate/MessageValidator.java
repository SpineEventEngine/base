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
import com.google.common.collect.Range;
import com.google.protobuf.Message;
import io.spine.base.FieldPath;
import io.spine.code.proto.FieldContext;
import io.spine.code.proto.FieldDeclaration;
import io.spine.code.proto.FieldName;
import io.spine.type.MessageType;
import io.spine.type.TypeName;
import io.spine.validate.option.DistinctConstraint;
import io.spine.validate.option.GoesConstraint;
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
import java.util.regex.Matcher;
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

    MessageValidator(Message message) {
        this(atTopLevel(checkNotNull(message)));
    }

    MessageValidator(Message message, FieldContext context) {
        this(nestedIn(checkNotNull(context), checkNotNull(message)));
    }

    private MessageValidator(MessageValue message) {
        this.message = message;
        this.violations = new ArrayList<>();
    }

    @Override
    public void visitRange(RangedConstraint<?> constraint) {
        FieldValue value = message.valueOf(constraint.field());
        Range<ComparableNumber> range = constraint.range();
        checkTypeConsistency(range, value);
        value.values()
             .map(num -> new ComparableNumber((Number) num))
             .filter(range.negate())
             .map(number -> violation(constraint, value, number.value()))
             .forEach(violations::add);
    }

    @Override
    public void visitRequired(RequiredConstraint constraint) {
        if (constraint.optionValue()) {
            FieldValue fieldValue = message.valueOf(constraint.field());
            if (fieldValue.isDefault()) {
                violations.add(violation(constraint, fieldValue));
            }
        }
    }

    @Override
    public void visitPattern(PatternConstraint constraint) {
        FieldValue fieldValue = message.valueOf(constraint.field());
        String regex = constraint.regex();
        int flags = constraint.flagsMask();
        @SuppressWarnings("MagicConstant")
        Pattern compiledPattern = Pattern.compile(regex, flags);
        boolean partialMatch = constraint.allowsPartialMatch();
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
        Matcher matcher = pattern.matcher(value);
        return !matcher.matches();
    }

    private static boolean noPartialMatch(Pattern pattern, String value) {
        Matcher matcher = pattern.matcher(value);
        return !matcher.find();
    }

    @Override
    public void visitDistinct(DistinctConstraint constraint) {
        FieldValue fieldValue = message.valueOf(constraint.field());
        Set<?> duplicates = findDuplicates(fieldValue);
        violations.addAll(
                duplicates.stream()
                          .map(duplicate -> violation(constraint, fieldValue, duplicate))
                          .collect(toImmutableList())
        );
    }

    @Override
    public void visitGoesWith(GoesConstraint constraint) {
        FieldValue value = message.valueOf(constraint.field());
        Optional<FieldDeclaration> declaration = withField(message, constraint);
        checkState(declaration.isPresent(),
                   "Field `%s` noted in `(goes).with` option is not found.",
                   constraint.optionValue().getWith());
        FieldDeclaration withField = declaration.get();
        if (!value.isDefault() && fieldValueNotSet(withField)) {
            ConstraintViolation withFieldNotSet =
                    violation(constraint, value)
                            .toBuilder()
                            .addParam(constraint.field().name().value())
                            .addParam(constraint.optionValue().getWith())
                            .build();
            violations.add(withFieldNotSet);
        }
    }

    @Override
    public void visitValidate(ValidateConstraint constraint) {
        FieldValue fieldValue = message.valueOf(constraint.field());
        if (!fieldValue.isDefault()) {
            List<ConstraintViolation> childViolations = fieldValue
                    .values()
                    .map(val -> ensureMessage((Message) val))
                    .flatMap(msg -> childViolations(fieldValue.context(), msg).stream())
                    .collect(toList());
            if (!childViolations.isEmpty()) {
                ConstraintViolation parentViolation = violation(constraint, fieldValue)
                        .toBuilder()
                        .addAllViolation(childViolations)
                        .build();
                violations.add(parentViolation);
            }
        }
    }

    @Override
    public void visitRequiredField(RequiredFieldConstraint constraint) {
        RequiredFieldCheck check = new RequiredFieldCheck(constraint.optionValue(),
                                                          constraint.alternatives(),
                                                          message);
        Optional<ConstraintViolation> violation = check.perform();
        violation.ifPresent(violations::add);
    }

    @Override
    public void visitCustom(CustomConstraint constraint) {
        ImmutableList<ConstraintViolation> violations = constraint.validate(message);
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
        } else {
            ValidationError error = ValidationError
                    .newBuilder()
                    .addAllConstraintViolation(violations)
                    .build();
            return Optional.of(error);
        }
    }

    private static List<ConstraintViolation> childViolations(FieldContext field, Message message) {
        MessageValue messageValue = nestedIn(field, ensureMessage(message));
        MessageValidator childInterpreter = new MessageValidator(messageValue);
        return Constraints
                .of(MessageType.of(message), field)
                .runThrough(childInterpreter)
                .map(ValidationError::getConstraintViolationList)
                .orElse(ImmutableList.of());
    }

    private static void checkTypeConsistency(Range<ComparableNumber> range, FieldValue value) {
        if (range.hasLowerBound() && range.hasUpperBound()) {
            NumberText upper = range.upperEndpoint()
                                    .toText();
            NumberText lower = range.lowerEndpoint()
                                    .toText();
            if (!upper.isOfSameType(lower)) {
                String errorMessage = "Boundaries have inconsistent types: lower %s, upper %s";
                throw newIllegalStateException(errorMessage, upper, lower);
            }
            checkBoundaryAndValue(upper, value);
        } else {
            checkSingleBoundary(range, value);
        }
    }

    private static void checkSingleBoundary(Range<ComparableNumber> range, FieldValue value) {
        NumberText singleBoundary = range.hasLowerBound()
                                    ? range.lowerEndpoint()
                                           .toText()
                                    : range.upperEndpoint()
                                           .toText();
        checkBoundaryAndValue(singleBoundary, value);
    }

    private static void checkBoundaryAndValue(NumberText boundary, FieldValue value) {
        ComparableNumber boundaryNumber = boundary.toNumber();
        Number valueNumber = (Number) value.singleValue();
        if (!NumberConversion.check(boundaryNumber, valueNumber)) {
            String errorMessage =
                    "Boundary values must have types consistent with values they bind: " +
                            "boundary %s, value %s";
            throw newIllegalStateException(errorMessage, boundary, valueNumber);
        }
    }

    private static Set<?> findDuplicates(FieldValue fieldValue) {
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
        FieldName withField = FieldName.of(constraint.optionValue()
                                                     .getWith());
        for (FieldDeclaration field : messageValue.declaration()
                                                  .fields()) {
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
        FieldContext context = value.context();
        FieldPath fieldPath = context.fieldPath();
        TypeName typeName = constraint.targetType()
                                      .name();
        ConstraintViolation.Builder violation = ConstraintViolation
                .newBuilder()
                .setMsgFormat(constraint.errorMessage(context))
                .setFieldPath(fieldPath)
                .setTypeName(typeName.value());
        if (violatingValue != null) {
            violation.setFieldValue(toAny(violatingValue));
        }
        return violation.build();
    }
}
