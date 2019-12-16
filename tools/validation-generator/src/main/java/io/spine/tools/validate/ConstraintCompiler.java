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
import io.spine.option.IfInvalidOption;
import io.spine.tools.validate.code.BooleanExpression;
import io.spine.tools.validate.code.Expression;
import io.spine.tools.validate.code.IsSet;
import io.spine.tools.validate.code.NewViolation;
import io.spine.tools.validate.field.ConstraintCode;
import io.spine.tools.validate.field.FieldConstraintCode;
import io.spine.type.MessageType;
import io.spine.validate.ComparableNumber;
import io.spine.validate.Constraint;
import io.spine.validate.ConstraintTranslator;
import io.spine.validate.ConstraintViolation;
import io.spine.validate.Duplicates;
import io.spine.validate.Validate;
import io.spine.validate.option.DistinctConstraint;
import io.spine.validate.option.FieldConstraint;
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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.BoundType.OPEN;
import static com.squareup.javapoet.ClassName.bestGuess;
import static io.spine.tools.validate.MessageValidatorFactory.immutableListOfViolations;
import static io.spine.tools.validate.code.BooleanExpression.fromCode;
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

    private final List<ConstraintCode> compiledConstraints;
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
        FieldAccess fieldAccess = messageAccess.get(field);
        Range<ComparableNumber> range = constraint.range();
        if (limit.boundExists(range)) {
            BooleanExpression condition = fromCode("$N $L$L $L", fieldAccess.value(),
                                                   limit.sign,
                                                   limit.type(range) == OPEN ? "=" : "",
                                                   limit.endpoint(range));
            NewViolation violation = violation(constraint, field, fieldAccess);
            append(new FieldConstraintCode(condition, violation));
        }
    }

    @Override
    public void visitRequired(RequiredConstraint constraint) {
        FieldDeclaration field = constraint.field();
        IsSet fieldIsSet = new IsSet(field);
        BooleanExpression messageIsNotSet = fieldIsSet.invocation(messageAccess)
                                                      .negate();
        if (!messageIsNotSet.isConstant()) {
            generatedMethods.add(fieldIsSet.method());
        }
        NewViolation violation = violation(constraint, field);
        FieldConstraintCode check = new FieldConstraintCode(messageIsNotSet, violation);
        append(check);
    }

    @Override
    public void visitPattern(PatternConstraint constraint) {
        FieldDeclaration field = constraint.field();
        FieldAccess fieldAccess = messageAccess.get(field);
        String pattern = constraint.optionValue()
                                   .getRegex();
        append(new FieldConstraintCode(
                fromCode("$L.matches($S)", fieldAccess.toCode(), pattern),
                newViolation(field, constraint)
                        .setFieldValue(fieldAccess)
                        .build()));
    }

    @Override
    public void visitDistinct(DistinctConstraint constraint) {
        FieldDeclaration field = constraint.field();
        FieldAccess fieldAccess = accessField(constraint);
        String duplicatesName = "duplicates" + field.name().toCamelCase();
        CodeBlock duplicates = CodeBlock.of("Set<?> $N = $T.findIn($L);",
                                            duplicatesName,
                                            Duplicates.class,
                                            fieldAccess);
        BooleanExpression condition = isEmpty(Expression.of(duplicatesName)).negate();
        NewViolation violation = violation(constraint, field);
        append(new FieldConstraintCode(duplicates, condition, violation));
    }

    @Override
    public void visitGoesWith(GoesConstraint constraint) {
        // TBD.
    }

    @Override
    public void visitValidate(ValidateConstraint constraint) {
        FieldDeclaration field = constraint.field();
        FieldAccess fieldAccess = accessField(constraint);
        Expression<List<ConstraintViolation>> violationsVar =
                Expression.formatted("%sViolations", field.name().javaCase());
        CodeBlock nestedViolationDecl = CodeBlock
                .builder()
                .addStatement("$T $N = $T.violationsOf($L)", listOfViolations, violationsVar.toString(), Validate.class, fieldAccess.toCode())
                .build();
        BooleanExpression condition = isEmpty(violationsVar).negate();
        IfInvalidOption errorMessageOption = new IfInvalid().valueOrDefault(field.descriptor());
        String errorMessage = errorMessage(errorMessageOption, errorMessageOption.getMsgFormat());
        NewViolation violation = newViolation(field, constraint)
                .setMessage(errorMessage)
                .setNestedViolations(violationsVar)
                .build();
        FieldConstraintCode code =
                new FieldConstraintCode(nestedViolationDecl, condition, violation);
        append(code);
    }

    @Override
    public void visitRequiredField(RequiredFieldConstraint constraint) {
        // TBD.
    }

    private void append(ConstraintCode constraintCode) {
        compiledConstraints.add(constraintCode);
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
        for (ConstraintCode constraintCode : compiledConstraints) {
            validationCode.add(constraintCode.compile(violationAccumulator));
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

    private static FieldAccess accessField(FieldConstraint<?> constraint) {
        return messageAccess.get(constraint.field());
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
