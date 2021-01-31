/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.tools.compiler.gen.rejection;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import io.spine.base.RejectionType;
import io.spine.code.gen.java.JavaPoetName;
import io.spine.code.gen.java.TypeSpec;
import io.spine.code.java.PackageName;
import io.spine.code.java.SimpleClassName;
import io.spine.code.javadoc.JavadocText;
import io.spine.code.proto.FieldDeclaration;
import io.spine.code.proto.FieldName;
import io.spine.protobuf.Messages;
import io.spine.tools.compiler.field.type.FieldType;
import io.spine.tools.compiler.gen.NoArgMethod;
import io.spine.validate.Validate;

import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * Generates code for a rejection builder.
 *
 * <p>A generated builder validates rejection messages using
 * {@link io.spine.validate.Validate#checkValid(com.google.protobuf.Message)}.
 */
final class RejectionBuilderSpec implements TypeSpec {

    private static final NoArgMethod newBuilder = new NoArgMethod(Messages.METHOD_NEW_BUILDER);
    private static final String BUILDER_FIELD = "builder";

    private final RejectionType rejection;
    private final JavaPoetName messageClass;
    private final JavaPoetName throwableClass;
    private final SimpleClassName name;

    RejectionBuilderSpec(RejectionType rejection,
                         JavaPoetName messageClass,
                         JavaPoetName throwableClass) {
        this.rejection = rejection;
        this.messageClass = messageClass;
        this.throwableClass = throwableClass;
        this.name = SimpleClassName.ofBuilder();
    }

    @Override
    public PackageName packageName() {
        PackageName packageName = rejection.javaPackage();
        return packageName;
    }

    @Override
    public com.squareup.javapoet.TypeSpec toPoet() {
        com.squareup.javapoet.TypeSpec result = com.squareup.javapoet.TypeSpec
                .classBuilder(name.value())
                .addModifiers(PUBLIC, STATIC)
                .addJavadoc(classJavadoc().value())
                .addField(initializedProtoBuilder())
                .addMethod(constructor())
                .addMethods(setters())
                .addMethod(rejectionMessage())
                .addMethod(build())
                .build();
        return result;
    }

    /**
     * Obtains the method to create the builder.
     *
     * @return the {@code newInstance} specification
     */
    MethodSpec newBuilder() {
        @SuppressWarnings("DuplicateStringLiteralInspection") // The duplicated string is in
                // tests of the code which cannot share common constants with this class.
                // For the time being let's keep it as is.
        JavadocText javadoc = JavadocText.fromEscaped("@return a new builder for the rejection")
                                         .withNewLine();
        return MethodSpec
                .methodBuilder(newBuilder.name())
                .addModifiers(PUBLIC, STATIC)
                .addJavadoc(javadoc.value())
                .returns(thisType())
                .addStatement("return new $L()", name.value())
                .build();
    }

    /**
     * Obtains the builder as a parameter.
     *
     * @return the parameter specification for this builder
     */
    ParameterSpec asParameter() {
        return ParameterSpec
                .builder(thisType(), BUILDER_FIELD)
                .build();
    }

    /**
     * A code block, which builds and validates the rejection message.
     *
     * <p>The code block is not a statement (there is no semicolon) since
     * it is intended to be passes to a constructor.
     *
     * @return the code block to obtain a rejection message
     */
    CodeBlock buildRejectionMessage() {
        return CodeBlock
                .builder()
                .add("$N.$N()", asParameter(), rejectionMessage())
                .build();
    }

    private static MethodSpec constructor() {
        String rawJavadoc = "Prevent direct instantiation of the builder.";
        JavadocText javadoc = JavadocText.fromEscaped(rawJavadoc)
                                         .withNewLine();
        return constructorBuilder()
                .addJavadoc(javadoc.value())
                .addModifiers(PRIVATE)
                .build();
    }

    private MethodSpec rejectionMessage() {
        JavadocText javadoc = JavadocText.fromEscaped("Obtains the rejection and validates it.")
                                         .withNewLine();
        return MethodSpec
                .methodBuilder("rejectionMessage")
                .addModifiers(PRIVATE)
                .addJavadoc(javadoc.value())
                .returns(messageClass.value())
                .addStatement("$T message = $L.build()", messageClass.value(), BUILDER_FIELD)
                .addStatement("$T.checkValid(message)", Validate.class)
                .addStatement("return message")
                .build();
    }

    @SuppressWarnings("DuplicateStringLiteralInspection") // The same string has different semantics
    private MethodSpec build() {
        String rawJavadoc = "Creates the rejection from the builder and validates it.";
        JavadocText javadoc = JavadocText.fromEscaped(rawJavadoc)
                                         .withNewLine();
        return MethodSpec
                .methodBuilder("build")
                .addModifiers(PUBLIC)
                .addJavadoc(javadoc.value())
                .returns(throwableClass.value())
                .addStatement("return new $T(this)", throwableClass.value())
                .build();
    }

    private JavadocText classJavadoc() {
        String rejectionName = rejection.simpleJavaClassName()
                                        .value();
        String javadocText = CodeBlock
                .builder()
                .add("The builder for the {@code $L} rejection.", rejectionName)
                .build()
                .toString();
        return JavadocText.fromEscaped(javadocText)
                          .withNewLine();
    }

    @SuppressWarnings("DuplicateStringLiteralInspection") // Random generated code duplication.
    private FieldSpec initializedProtoBuilder() {
        ClassName protoBuilderClass = messageClass.className()
                                                  .nestedClass(SimpleClassName.ofBuilder()
                                                                              .value());
        return FieldSpec
                .builder(protoBuilderClass, BUILDER_FIELD, PRIVATE, FINAL)
                .initializer("$T.newBuilder()", messageClass.value())
                .build();
    }

    private List<MethodSpec> setters() {
        List<MethodSpec> methods = newArrayList();
        ImmutableList<FieldDeclaration> fields = rejection.fields();
        for (FieldDeclaration field : fields) {
            FieldType fieldType = FieldType.of(field);
            MethodSpec setter = fieldSetter(field, fieldType);
            methods.add(setter);
        }
        return methods;
    }

    private MethodSpec fieldSetter(FieldDeclaration field, FieldType fieldType) {
        FieldName fieldName = field.name();
        String parameterName = fieldName.javaCase();
        String methodName = fieldType.primarySetterTemplate()
                                     .format(io.spine.code.gen.java.FieldName.from(fieldName));
        MethodSpec.Builder methodBuilder = MethodSpec
                .methodBuilder(methodName)
                .addModifiers(PUBLIC)
                .returns(thisType())
                .addParameter(fieldType.getTypeName(), parameterName)
                .addStatement("$L.$L($L)", BUILDER_FIELD, methodName, parameterName)
                .addStatement("return this");
        Optional<String> comments = field.leadingComments();
        comments.ifPresent(
                text -> methodBuilder.addJavadoc(JavadocText.fromUnescaped(text)
                                                            .inPreTags()
                                                            .value()));
        return methodBuilder.build();
    }

    /**
     * Obtains the class name of this builder.
     *
     * @return class name for the builder
     */
    private ClassName thisType() {
        return throwableClass.className()
                             .nestedClass(name.value());
    }
}
