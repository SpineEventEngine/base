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
package io.spine.tools.mc.java.gradle.rejections;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import io.spine.base.RejectionThrowable;
import io.spine.base.RejectionType;
import io.spine.tools.java.code.field.FieldName;
import io.spine.tools.java.code.GeneratedBy;
import io.spine.tools.java.JavaPoetName;
import io.spine.tools.java.code.TypeSpec;
import io.spine.code.java.PackageName;
import io.spine.code.java.SimpleClassName;
import io.spine.tools.java.javadoc.JavadocText;
import io.spine.logging.Logging;

import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * A spec for a generated rejection type.
 *
 * <p>The generated type extends {@link RejectionThrowable} and encloses an instance of the
 * corresponding {@linkplain io.spine.base.RejectionMessage rejection message}.
 */
final class RThrowableSpec implements TypeSpec, Logging {

    private static final NoArgMethod messageThrown = new NoArgMethod("messageThrown");

    private final RejectionType declaration;
    private final JavaPoetName messageClass;

    private final RThrowableBuilderSpec builder;

    /**
     * Creates a new instance.
     *
     *  @param type
     *         a rejection declaration
     */
    public RThrowableSpec(RejectionType type) {
        this.declaration = type;
        this.messageClass = JavaPoetName.of(type.messageClass());
        this.builder = new RThrowableBuilderSpec(
                type, messageClass, JavaPoetName.of(type.throwableClass())
        );
    }

    @Override
    public PackageName packageName() {
        PackageName packageName = declaration.javaPackage();
        return packageName;
    }

    @Override
    public com.squareup.javapoet.TypeSpec toPoet() {
        SimpleClassName className = declaration.simpleJavaClassName();
        com.squareup.javapoet.TypeSpec rejection =
                com.squareup.javapoet.TypeSpec.classBuilder(className.value())
                        .addJavadoc(classJavadoc())
                        .addAnnotation(GeneratedBy.spineModelCompiler())
                        .addModifiers(PUBLIC)
                        .superclass(RejectionThrowable.class)
                        .addField(serialVersionUID())
                        .addMethod(constructor())
                        .addMethod(messageThrown())
                        .addMethod(builder.newBuilder())
                        .addType(builder.toPoet())
                        .build();
        return rejection;
    }

    @SuppressWarnings("DuplicateStringLiteralInspection") // Random duplication.
    private MethodSpec constructor() {
        _debug().log("Creating the constructor for the type `%s`.",
                    declaration.simpleJavaClassName());
        ParameterSpec builderParameter = builder.asParameter();
        CodeBlock buildRejectionMessage = builder.buildRejectionMessage();
        return constructorBuilder()
                .addJavadoc(constructorJavadoc(builderParameter))
                .addModifiers(PRIVATE)
                .addParameter(builderParameter)
                .addStatement("super($L)", buildRejectionMessage.toString())
                .build();
    }

    private MethodSpec messageThrown() {
        String methodSignature = messageThrown.signature();
        _debug().log("Adding method `%s`.", methodSignature);
        TypeName returnType = messageClass.value();
        return MethodSpec.methodBuilder(messageThrown.name())
                         .addAnnotation(Override.class)
                         .addModifiers(PUBLIC)
                         .returns(returnType)
                         .addStatement("return ($T) super.$L", returnType, methodSignature)
                         .build();
    }

    /**
     * A Javadoc content for the rejection.
     *
     * @return the class-level Javadoc content
     */
    private CodeBlock classJavadoc() {
        JavadocText leadingComments =
                declaration.leadingComments()
                           .map(text -> JavadocText.fromUnescaped(text)
                                                   .inPreTags()
                                                   .withNewLine())
                           .orElse(JavadocText.fromEscaped(""));
        PackageName rejectionPackage = declaration.javaPackage();
        CodeBlock sourceProtoNote = CodeBlock
                .builder()
                .add("Rejection based on proto type ")
                .add("{@code $L.$L}", rejectionPackage, declaration.simpleJavaClassName())
                .build();
        return CodeBlock
                .builder()
                .add(leadingComments.value())
                .add(JavadocText.fromEscaped(sourceProtoNote.toString())
                                .withNewLine()
                                .value())
                .build();
    }

    /**
     * A Javadoc content for the rejection constructor.
     *
     * @param builderParameter
     *         the name of a rejection builder parameter
     * @return the constructor Javadoc content
     */
    private static CodeBlock constructorJavadoc(ParameterSpec builderParameter) {
        JavadocText generalPart = JavadocText.fromUnescaped("Creates a new instance.")
                                             .withNewLine()
                                             .withNewLine();
        CodeBlock paramsBlock = CodeBlock.of("@param $N the builder for the rejection",
                                             builderParameter);
        JavadocText paramsPart = JavadocText.fromEscaped(paramsBlock.toString())
                                            .withNewLine();
        return CodeBlock.builder()
                        .add(generalPart.value())
                        .add(paramsPart.value())
                        .build();
    }

    private static FieldSpec serialVersionUID() {
        return FieldSpec.builder(long.class,
                                 FieldName.serialVersionUID()
                                          .value(),
                                 PRIVATE, STATIC, FINAL)
                        .initializer("0L")
                        .build();
    }
}
