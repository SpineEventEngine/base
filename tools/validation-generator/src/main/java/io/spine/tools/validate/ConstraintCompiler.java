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

package io.spine.tools.validate;

import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.google.common.reflect.TypeToken;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import io.spine.code.java.SimpleClassName;
import io.spine.code.proto.FieldContext;
import io.spine.code.proto.FieldDeclaration;
import io.spine.option.GoesOption;
import io.spine.option.IfInvalidOption;
import io.spine.tools.validate.code.BooleanExpression;
import io.spine.tools.validate.code.Expression;
import io.spine.tools.validate.code.IsSet;
import io.spine.tools.validate.code.NewViolation;
import io.spine.tools.validate.field.Check;
import io.spine.tools.validate.field.ConstraintCode;
import io.spine.tools.validate.field.CreateViolation;
import io.spine.type.MessageType;
import io.spine.validate.Alternative;
import io.spine.validate.ComparableNumber;
import io.spine.validate.Constraint;
import io.spine.validate.ConstraintTranslator;
import io.spine.validate.ConstraintViolation;
import io.spine.validate.Duplicates;
import io.spine.validate.Validate;
import io.spine.validate.option.DistinctConstraint;
import io.spine.validate.option.GoesConstraint;
import io.spine.validate.option.IfInvalid;
import io.spine.validate.option.PatternConstraint;
import io.spine.validate.option.RangedConstraint;
import io.spine.validate.option.RequiredConstraint;
import io.spine.validate.option.RequiredFieldConstraint;
import io.spine.validate.option.ValidateConstraint;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.BoundType.OPEN;
import static com.squareup.javapoet.ClassName.bestGuess;
import static io.spine.tools.validate.MessageValidatorFactory.immutableListOfViolations;
import static io.spine.tools.validate.code.BooleanExpression.fromCode;
import static io.spine.tools.validate.code.BooleanExpression.trueLiteral;
import static io.spine.tools.validate.code.VoidExpression.formatted;
import static io.spine.tools.validate.field.Containers.isEmpty;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;
import static io.spine.validate.diags.ViolationText.errorMessage;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;

final class ConstraintCompiler implements ConstraintTranslator<Set<MethodSpec>> {

    private static final String VIOLATIONS = "violations";
    @SuppressWarnings("UnstableApiUsage")
    private static final Type listBuilderOfViolations =
            new TypeToken<ImmutableList.Builder<ConstraintViolation>>() {}.getType();
    @SuppressWarnings("UnstableApiUsage")
    private static final Type listOfViolations =
            new TypeToken<List<ConstraintViolation>>() {}.getType();
    private static final MessageAccess messageAccess = MessageAccess.of("msg");

    private final List<CodeBlock> compiledConstraints;
    private final AccumulateViolations violationAccumulator;
    private final FieldContext fieldContext;
    private final ImmutableSet.Builder<MethodSpec> generatedMethods;
    private final String methodName;
    private final MessageType type;

    ConstraintCompiler(String methodName, MessageType type) {
        this.methodName = checkNotEmptyOrBlank(methodName);
        this.type = checkNotNull(type);
        this.fieldContext = FieldContext.empty();
        this.compiledConstraints = new ArrayList<>();
        this.violationAccumulator =
                violation -> formatted("%s.add(%s);", VIOLATIONS, violation);
        this.generatedMethods = ImmutableSet.builder();
    }

    @Override
    public void visitRange(RangedConstraint<?> constraint) {
        FieldDeclaration field = constraint.field();
        checkRange(constraint, field, Limit.LOWER);
        checkRange(constraint, field, Limit.UPPER);
    }

    private void checkRange(RangedConstraint<?> constraint,
                                                    FieldDeclaration field,
                                                    Limit limit) {
        Range<ComparableNumber> range = constraint.range();
        if (limit.boundExists(range)) {
            Check check = fieldAccess -> fromCode("$L $L$L $L", fieldAccess,
                                                  limit.sign,
                                                  limit.type(range) == OPEN ? "=" : "",
                                                  limit.endpoint(range));
            CreateViolation violation = fieldAccess -> violation(constraint, field, fieldAccess);
            append(constraintCode(field)
                           .conditionCheck(check)
                           .createViolation(violation)
                           .build());
        }
    }

    @Override
    public void visitRequired(RequiredConstraint constraint) {
        FieldDeclaration field = constraint.field();
        IsSet fieldIsSet = new IsSet(field);
        generatedMethods.add(fieldIsSet.method());
        Check messageIsNotSet = fieldAccess -> fieldIsSet.invocation(messageAccess)
                                                         .negate();
        CreateViolation violation = fieldAccess -> violation(constraint, field);
        append(constraintCode(field)
                       .conditionCheck(messageIsNotSet)
                       .createViolation(violation)
                       .validateAsCollection()
                       .build());
    }

    @Override
    public void visitPattern(PatternConstraint constraint) {
        FieldDeclaration field = constraint.field();
        String pattern = constraint.optionValue()
                                   .getRegex();
        Check check = fieldAccess -> fromCode("$L.matches($S)", fieldAccess, pattern).negate();
        CreateViolation violation = fieldAccess -> newViolation(field, constraint)
                .setFieldValue(fieldAccess)
                .build();
        append(constraintCode(field)
                       .conditionCheck(check)
                       .createViolation(violation)
                       .build());
    }

    @Override
    public void visitDistinct(DistinctConstraint constraint) {
        FieldDeclaration field = constraint.field();
        String duplicatesName = "duplicates" + field.name().toCamelCase();
        Function<FieldAccess, CodeBlock> duplicates =
                fieldAccess -> CodeBlock.of("Set<?> $N = $T.findIn($L);",
                                duplicatesName,
                                Duplicates.class,
                                fieldAccess);
        Check check = fieldAccess -> isEmpty(Expression.of(duplicatesName)).negate();
        CreateViolation violation = fieldAccess -> violation(constraint, field);
        append(constraintCode(field)
                       .preparingDeclarations(duplicates)
                       .conditionCheck(check)
                       .createViolation(violation)
                       .validateAsCollection()
                       .build());
    }

    @Override
    public void visitGoesWith(GoesConstraint constraint) {
        FieldDeclaration field = constraint.field();
        GoesOption option = constraint.optionValue();
        String pairedFieldName = option.getWith();
        FieldDeclaration pairedField = type.field(pairedFieldName);
        IsSet fieldIsSet = new IsSet(field);
        IsSet pairedIsSet = new IsSet(pairedField);
        generatedMethods.add(fieldIsSet.method());
        generatedMethods.add(pairedIsSet.method());
        Check check = f -> fieldIsSet.invocation(messageAccess)
                                     .and(pairedIsSet.invocation(messageAccess).negate());
        CreateViolation createViolation = f -> violation(constraint, field);
        append(constraintCode(field)
                       .conditionCheck(check)
                       .createViolation(createViolation)
                       .build());
    }

    @Override
    public void visitValidate(ValidateConstraint constraint) {
        FieldDeclaration field = constraint.field();
        Expression<List<ConstraintViolation>> violationsVar =
                Expression.formatted("%sViolations", field.name()
                                                          .javaCase());
        Function<FieldAccess, CodeBlock> nestedViolations = fieldAccess ->
                CodeBlock.builder()
                         .addStatement("$T $N = $T.violationsOf($L)",
                                       listOfViolations,
                                       violationsVar.toString(),
                                       Validate.class,
                                       fieldAccess)
                         .build();
        Check check = fieldAccess -> isEmpty(violationsVar).negate();
        IfInvalidOption errorMessageOption = new IfInvalid().valueOrDefault(field.descriptor());
        String errorMessage = errorMessage(errorMessageOption, errorMessageOption.getMsgFormat());
        CreateViolation violation = fieldAccess -> newViolation(field, constraint)
                .setMessage(errorMessage)
                .setNestedViolations(violationsVar)
                .build();
        append(constraintCode(field)
                       .preparingDeclarations(nestedViolations)
                       .conditionCheck(check)
                       .createViolation(violation)
                       .build());
    }

    @Override
    public void visitRequiredField(RequiredFieldConstraint constraint) {
        ImmutableSet<Alternative> alternatives = constraint.alternatives();
        BooleanExpression fieldsAreSet = alternatives
                .stream()
                .map(this::matches)
                .reduce(BooleanExpression::or)
                .orElseThrow(
                        () -> new IllegalStateException("`(required_field)` must not be empty.")
                );
        BooleanExpression condition = fieldsAreSet.negate();
        Expression<ConstraintViolation> violation = NewViolation
                .forMessage(fieldContext, type)
                .setMessage("Required fields are not set. Must match pattern `%s`.")
                .setField(fieldContext.fieldPath())
                .addParam(constraint.optionValue())
                .build();
        CodeBlock check = condition.ifTrue(violationAccumulator
                                                   .apply(violation)
                                                   .toCode())
                                   .toCode();
        compiledConstraints.add(check);
    }

    private BooleanExpression matches(Alternative alternative) {
        BooleanExpression alternativeMatched =
                alternative
                        .fields()
                        .stream()
                        .map(IsSet::new)
                        .peek(isSet -> generatedMethods.add(isSet.method()))
                        .reduce(trueLiteral(),
                                (condition, isSet) ->
                                        condition.and(isSet.invocation(messageAccess)),
                                BooleanExpression::and);
        return alternativeMatched;
    }

    private void append(ConstraintCode constraintCode) {
        compiledConstraints.add(constraintCode.compile());
    }

    @Override
    public ImmutableSet<MethodSpec> translate() {
        ImmutableSet<MethodSpec> methods = generatedMethods
                .add(buildValidate())
                .build();
        return methods;
    }

    private MethodSpec buildValidate() {
        SimpleClassName messageSimpleName = type.javaClassName().toSimple();
        MethodSpec validateMethod = MethodSpec
                .methodBuilder(methodName)
                .addModifiers(PRIVATE, STATIC)
                .returns(immutableListOfViolations)
                .addParameter(bestGuess(messageSimpleName.value()), messageAccess.toString())
                .addCode(buildValidateBody())
                .build();
        return validateMethod;
    }

    private CodeBlock buildValidateBody() {
        CodeBlock.Builder validationCode = CodeBlock.builder();
        for (CodeBlock constraintCode : compiledConstraints) {
            validationCode.add(constraintCode);
        }
        return validationCode.isEmpty()
               ? CodeBlock.of("return $T.of();", ImmutableList.class)
               : CodeBlock
                       .builder()
                       .addStatement("$T $N = $T.builder()",
                                     listBuilderOfViolations,
                                     VIOLATIONS,
                                     ImmutableList.class)
                       .add(validationCode.build())
                       .addStatement("return $N.build()", VIOLATIONS)
                       .build();
    }

    private NewViolation.Builder newViolation(FieldDeclaration field, Constraint constraint) {
        FieldContext context = fieldContext.forChild(field);
        return NewViolation
                .forField(context)
                .setMessage(constraint.errorMessage(context));
    }

    private NewViolation violation(Constraint constraint, FieldDeclaration field) {
        return newViolation(field, constraint).build();
    }

    private NewViolation violation(Constraint constraint,
                                   FieldDeclaration field,
                                   FieldAccess getter) {
        return newViolation(field, constraint)
                .setFieldValue(getter)
                .build();
    }

    private ConstraintCode.Builder constraintCode(FieldDeclaration field) {
        return ConstraintCode
                .forField(field)
                .messageAccess(messageAccess)
                .onViolation(violationAccumulator);
    }

    private enum Limit {

        LOWER("<") {
            @Override
            boolean boundExists(Range<?> range) {
                return range.hasLowerBound();
            }

            @Override
            BoundType type(Range<?> range) {
                return range.lowerBoundType();
            }

            @Override
            Number endpoint(Range<? extends Number> range) {
                return range.lowerEndpoint();
            }
        },
        UPPER(">") {
            @Override
            boolean boundExists(Range<?> range) {
                return range.hasUpperBound();
            }

            @Override
            BoundType type(Range<?> range) {
                return range.upperBoundType();
            }

            @Override
            Number endpoint(Range<? extends Number> range) {
                return range.upperEndpoint();
            }
        };

        private final String sign;

        Limit(String sign) {
            this.sign = sign;
        }

        abstract boolean boundExists(Range<?> range);

        abstract BoundType type(Range<?> range);

        abstract Number endpoint(Range<? extends Number> range);
    }
}
