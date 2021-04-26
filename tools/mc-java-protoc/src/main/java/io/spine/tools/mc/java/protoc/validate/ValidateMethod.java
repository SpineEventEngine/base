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

package io.spine.tools.mc.java.protoc.validate;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import io.spine.code.java.SimpleClassName;
import io.spine.tools.java.code.ClassMember;
import io.spine.tools.java.code.Method;
import io.spine.type.MessageType;
import io.spine.validate.ConstraintViolation;

import java.lang.reflect.Type;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.squareup.javapoet.ClassName.bestGuess;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * The generated method which performs message validation.
 *
 * <p>This method:
 * <ol>
 *     <li>is {@code private};
 *     <li>is {@code static};
 *     <li>accepts the validated message as the only argument;
 *     <li>returns an {@link ImmutableList} of {@link ConstraintViolation}s.
 * </ol>
 */
final class ValidateMethod {

    static final String VIOLATIONS = "violations";
    @SuppressWarnings("UnstableApiUsage")
    static final Type immutableListOfViolations =
            new TypeToken<ImmutableList<ConstraintViolation>>() {}.getType();
    @SuppressWarnings("UnstableApiUsage")
    private static final Type listBuilderOfViolations =
            new TypeToken<ImmutableList.Builder<ConstraintViolation>>() {}.getType();

    private final MessageType validatedType;
    private final String methodName;
    private final MessageAccess parameter;
    private final ImmutableList<CodeBlock> compiledConstraints;

    ValidateMethod(MessageType validatedType,
                   String methodName,
                   MessageAccess parameter,
                   Iterable<CodeBlock> compiledConstraints) {
        this.validatedType = checkNotNull(validatedType);
        this.methodName = checkNotNull(methodName);
        this.parameter = checkNotNull(parameter);
        this.compiledConstraints = ImmutableList.copyOf(compiledConstraints);
    }

    /**
     * Creates a {@code ClassMember} representing this method.
     */
    ClassMember asClassMember() {
        return new Method(spec());
    }

    /**
     * Creates a {@code MethodSpec} for this method.
     */
    private MethodSpec spec() {
        SimpleClassName messageSimpleName = validatedType.javaClassName().toSimple();
        MethodSpec validateMethod = MethodSpec
                .methodBuilder(methodName)
                .addModifiers(PRIVATE, STATIC)
                .returns(immutableListOfViolations)
                .addParameter(bestGuess(messageSimpleName.value()), parameter.toString())
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
}
