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

package io.spine.tools.validate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.google.common.reflect.TypeToken;
import com.google.protobuf.ProtocolMessageEnum;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import io.spine.tools.code.proto.FieldContext;
import io.spine.tools.code.proto.FieldDeclaration;
import io.spine.option.GoesOption;
import io.spine.option.IfInvalidOption;
import io.spine.protobuf.AnyPacker;
import io.spine.tools.validate.code.BooleanExpression;
import io.spine.tools.validate.code.Check;
import io.spine.tools.validate.code.ConstraintCode;
import io.spine.tools.validate.code.CreateViolation;
import io.spine.tools.validate.code.Expression;
import io.spine.tools.validate.code.IsSet;
import io.spine.tools.validate.code.NewViolation;
import io.spine.type.MessageType;
import io.spine.util.Duplicates;
import io.spine.validate.Alternative;
import io.spine.validate.ComparableNumber;
import io.spine.validate.Constraint;
import io.spine.validate.ConstraintTranslator;
import io.spine.validate.ConstraintViolation;
import io.spine.validate.CustomConstraint;
import io.spine.validate.Validate;
import io.spine.validate.option.DistinctConstraint;
import io.spine.validate.option.GoesConstraint;
import io.spine.validate.option.IfInvalid;
import io.spine.validate.option.IsRequiredConstraint;
import io.spine.validate.option.PatternConstraint;
import io.spine.validate.option.RangedConstraint;
import io.spine.validate.option.RequiredConstraint;
import io.spine.validate.option.RequiredFieldConstraint;
import io.spine.validate.option.ValidateConstraint;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.validate.ValidateMethod.VIOLATIONS;
import static io.spine.tools.validate.code.BooleanExpression.fromCode;
import static io.spine.tools.validate.code.Containers.isEmpty;
import static io.spine.tools.validate.code.IsSet.alternativeIsSet;
import static io.spine.tools.validate.code.VoidExpression.formatted;
import static io.spine.util.Exceptions.unsupported;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;
import static io.spine.validate.diags.ViolationText.errorMessage;
import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.toList;

/**
 * A {@link ConstraintTranslator} which generates Java code for message validation.
 *
 * <p>The generator operates under the assumption that the generated code is embedded into
 * the message class. The result of the code generation is a set of methods to be added to a single
 * nesting class. The nesting class need not be the message class. It might be a class nested inside
 * the message class. Note that some methods are declared as {@code static}. Thus, they cannot be
 * placed into an inner (non-static) class.
 */
@SuppressWarnings("OverlyCoupledClass")
final class ValidationCodeGenerator implements ConstraintTranslator<Set<ClassMember>> {

    @SuppressWarnings("UnstableApiUsage")
    private static final Type listOfViolations =
            new TypeToken<List<ConstraintViolation>>() {}.getType();
    private static final MessageAccess messageAccess = MessageAccess.of("msg");

    private final List<CodeBlock> compiledConstraints;
    private final Set<ExternalConstraintFlag> externalConstraintFlags;
    private final AccumulateViolations violationAccumulator;
    private final FieldContext fieldContext;
    private final String methodName;
    private final MessageType type;

    /**
     * Creates a new {@code ValidationCodeGenerator}.
     *
     * <p>The {@code methodName} is the name of the method which must be generated. The method:
     * <ol>
     *     <li>must be {@code static};
     *     <li>must accept the validated message as the only argument;
     *     <li>must return an {@link ImmutableList} of {@link ConstraintViolation}s;
     *     <li>may be declared {@code private}.
     * </ol>
     *
     * <p>The method generated for the {@code methodName} is the de facto public API for validating
     * the message {@code type}.
     *
     * @param methodName
     *         the expected name of the message validating method
     * @param type
     *         the type of the validated message
     * @see ValidateMethod
     */
    ValidationCodeGenerator(String methodName, MessageType type) {
        this.methodName = checkNotEmptyOrBlank(methodName);
        this.type = checkNotNull(type);
        this.fieldContext = FieldContext.empty();
        this.compiledConstraints = new ArrayList<>();
        this.violationAccumulator =
                violation -> formatted("%s.add(%s);", VIOLATIONS, violation);
        this.externalConstraintFlags = new HashSet<>();
    }

    @Override
    public void visitRange(RangedConstraint<?> constraint) {
        FieldDeclaration field = constraint.field();
        checkRange(constraint, field, Bound.LOWER);
        checkRange(constraint, field, Bound.UPPER);
    }

    private void checkRange(RangedConstraint<?> constraint, FieldDeclaration field, Bound bound) {
        Range<ComparableNumber> range = constraint.range();
        if (bound.exists(range)) {
            Check check = fieldAccess -> bound.matches(fieldAccess, range).negate();
            CreateViolation violation = fieldAccess -> violation(constraint, field, fieldAccess);
            append(constraintCode(field)
                           .conditionCheck(check)
                           .createViolation(violation)
                           .validateOnlyIfSet()
                           .build());
        }
    }

    @Override
    public void visitRequired(RequiredConstraint constraint) {
        FieldDeclaration field = constraint.field();
        IsSet fieldIsSet = new IsSet(field);
        Check messageIsNotSet = fieldAccess -> fieldIsSet.invocation(messageAccess)
                                                         .negate();
        CreateViolation violation = fieldAccess -> violation(constraint, field);
        append(constraintCode(field)
                       .conditionCheck(messageIsNotSet)
                       .createViolation(violation)
                       .validateAsWhole()
                       .build());
    }

    @Override
    public void visitPattern(PatternConstraint constraint) {
        FieldDeclaration field = constraint.field();
        String pattern = constraint.optionValue()
                                   .getRegex();
        String matcher = "$T.compile($S, $L).matcher($L).";
        String method = constraint.allowsPartialMatch()
                        ? "find()"
                        : "matches()";
        Check check = fieldAccess -> fromCode(
                matcher + method, Pattern.class, pattern, constraint.flagsMask(), fieldAccess
        ).negate();
        CreateViolation violation = fieldAccess -> newViolation(field, constraint)
                .setFieldValue(fieldAccess)
                .addParam(pattern)
                .build();
        append(constraintCode(field)
                       .conditionCheck(check)
                       .createViolation(violation)
                       .validateOnlyIfSet()
                       .build());
    }

    /**
     * {@inheritDoc}
     *
     * <p>The generated code relies on {@link Duplicates#findIn(Collection)}.
     */
    @Override
    public void visitDistinct(DistinctConstraint constraint) {
        FieldDeclaration field = constraint.field();
        String duplicatesName = "duplicates" + field.name().toCamelCase();
        Function<FieldAccess, CodeBlock> duplicates =
                fieldAccess -> CodeBlock.of("$T<?> $N = $T.findIn($L);",
                                            Set.class,
                                            duplicatesName,
                                            Duplicates.class,
                                            fieldAccess);
        Check check = fieldAccess -> isEmpty(Expression.of(duplicatesName)).negate();
        CreateViolation violation = fieldAccess -> violation(constraint, field);
        append(constraintCode(field)
                       .preparingDeclarations(duplicates)
                       .conditionCheck(check)
                       .createViolation(violation)
                       .validateAsWhole()
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
        Check check = f -> fieldIsSet.invocation(messageAccess)
                                     .and(pairedIsSet.invocation(messageAccess).negate());
        CreateViolation createViolation = f -> newViolation(field, constraint)
                .addParam(field.name().value(), pairedFieldName)
                .build();
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
        Function<FieldAccess, CodeBlock> nestedViolations = obtainViolations(field, violationsVar);
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

    private Function<FieldAccess, CodeBlock>
    obtainViolations(FieldDeclaration field,
                     Expression<List<ConstraintViolation>> violationsVar) {
        ExternalConstraintFlag flag = new ExternalConstraintFlag(field);
        externalConstraintFlags.add(flag);
        IsSet isSet = new IsSet(field);
        return fieldAccess -> {
            CodeBlock assignViolations = isSet
                    .valueIsNotSet(fieldAccess)
                    .ifTrue(assignToEmpty(violationsVar))
                    .elseIf(flag.value(), externalViolations(field, violationsVar, fieldAccess))
                    .orElse(intrinsicViolations(field, violationsVar, fieldAccess));
            return CodeBlock.builder()
                            .addStatement("$T $N", listOfViolations, violationsVar.toString())
                            .addStatement(assignViolations)
                            .build();
        };
    }

    private static CodeBlock
    intrinsicViolations(FieldDeclaration field,
                        Expression<List<ConstraintViolation>> violationsVar,
                        FieldAccess fieldAccess) {
        return CodeBlock.of("$N = $T.violationsOf($L);",
                            violationsVar.toString(),
                            Validate.class,
                            unpackedMessage(field, fieldAccess));
    }

    private static CodeBlock assignToEmpty(Expression<List<ConstraintViolation>> violationsVar) {
        return CodeBlock.of("$N = $T.of();", violationsVar.toString(), ImmutableList.class);
    }

    private CodeBlock externalViolations(FieldDeclaration field,
                                         Expression<List<ConstraintViolation>> violationsVar,
                                         FieldAccess fieldAccess) {
        ClassName typeName = ClassName.bestGuess(type.javaClassName().value());
        Expression<FieldContext> fieldContextExpression =
                Expression.fromCode("$T.create($T.getDescriptor().findFieldByNumber($L))",
                                    FieldContext.class,
                                    typeName,
                                    field.number());
        return CodeBlock.of("$N = $T.validateAtRuntime($L, $L);",
                            violationsVar.toString(),
                            Validate.class,
                            unpackedMessage(field, fieldAccess),
                            fieldContextExpression);
    }

    private static Expression<?> unpackedMessage(FieldDeclaration field, FieldAccess fieldAccess) {
        return field.isAny()
               ? Expression.of(CodeBlock.of("$T.unpack($L)", AnyPacker.class, fieldAccess))
               : fieldAccess;
    }

    @Override
    public void visitRequiredField(RequiredFieldConstraint constraint) {
        ImmutableSet<Alternative> alternatives = constraint.alternatives();
        BooleanExpression fieldsAreSet = alternatives
                .stream()
                .map(alt -> alternativeIsSet(alt, messageAccess))
                .reduce(BooleanExpression::or)
                .orElseThrow(
                        () -> new IllegalStateException("`(required_field)` must not be empty.")
                );
        BooleanExpression condition = fieldsAreSet.negate();
        @SuppressWarnings("DuplicateStringLiteralInspection") // Duplicates in generated code.
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

    @Override
    public void visitRequiredOneof(IsRequiredConstraint constraint) {
        Expression<ProtocolMessageEnum> caseValue =
                messageAccess.oneofCase(constraint.declaration());
        BooleanExpression condition = fromCode("$L.getNumber() == $L", caseValue, 0);
        Expression<ConstraintViolation> violation = NewViolation
                .forMessage(fieldContext, type)
                .setMessage(constraint.errorMessage(fieldContext))
                .setField(fieldContext.fieldPath())
                .build();
        CodeBlock check = condition.ifTrue(violationAccumulator
                                                   .apply(violation)
                                                   .toCode())
                                   .toCode();
        compiledConstraints.add(check);
    }

    @Override
    public void visitCustom(CustomConstraint constraint) {
        throw unsupported(
                "Custom constraints, such as `%s`, cannot be compiled into source code.%n" +
                        "Please remove the constraint from code generator classpath.",
                constraint
        );
    }

    private void append(ConstraintCode constraintCode) {
        compiledConstraints.add(constraintCode.compile());
    }

    @Override
    public ImmutableSet<ClassMember> translate() {
        compileCustomConstraints();
        List<ClassMember> isSetMethods = type
                .fields()
                .stream()
                .map(IsSet::new)
                .map(IsSet::method)
                .map(Method::new)
                .collect(toList());
        ValidateMethod validateMethod =
                new ValidateMethod(type, methodName, messageAccess, compiledConstraints);
        List<ClassMember> externalFlags = externalConstraintFlags
                .stream()
                .map(ExternalConstraintFlag::asClassMember)
                .collect(toList());
        ImmutableSet<ClassMember> methods = ImmutableSet
                .<ClassMember>builder()
                .add(validateMethod.asClassMember())
                .addAll(isSetMethods)
                .addAll(externalFlags)
                .build();
        return methods;
    }

    private void compileCustomConstraints() {
        CodeBlock code = CodeBlock.of("$N.addAll($T.violationsOfCustomConstraints($L));$L",
                                      VIOLATIONS,
                                      Validate.class,
                                      messageAccess,
                                      lineSeparator());
        compiledConstraints.add(code);
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
}
