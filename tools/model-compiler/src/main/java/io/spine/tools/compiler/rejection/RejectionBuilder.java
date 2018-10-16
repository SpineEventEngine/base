/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.tools.compiler.rejection;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import io.spine.code.java.SimpleClassName;
import io.spine.code.proto.FieldName;
import io.spine.protobuf.Messages;
import io.spine.tools.compiler.field.FieldDeclaration;
import io.spine.validate.Validate;

import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static io.spine.tools.compiler.rejection.FormattedCodeBlock.lineSeparator;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * Specification of a rejection builder.
 *
 * <p>A generated builder validates rejection messages using
 * {@link io.spine.validate.Validate#checkValid(com.google.protobuf.Message)}.
 */
class RejectionBuilder {

    private static final NoArgMethod newBuilder = new NoArgMethod(Messages.METHOD_NEW_BUILDER);
    private static final String BUILDER_FIELD = "builder";

    private final GeneratedRejectionDeclaration rejection;
    private final SimpleClassName name;
    private final List<FieldDeclaration> rejectionFields;

    RejectionBuilder(GeneratedRejectionDeclaration rejection,
                     List<FieldDeclaration> rejectionFields) {
        this.rejection = rejection;
        this.rejectionFields = ImmutableList.copyOf(rejectionFields);
        this.name = SimpleClassName.ofBuilder();
    }

    /**
     * Obtains the method to create the builder.
     *
     * @return the {@code newInstance} specification
     */
    MethodSpec newBuilder() {
        return MethodSpec
                .methodBuilder(newBuilder.name())
                .addModifiers(PUBLIC, STATIC)
                .addJavadoc(withNewLine("@return a new builder for the rejection"))
                .returns(thisType())
                .addStatement("return new $L()", name.value())
                .build();
    }

    /**
     * Obtains the declaration for the builder.
     *
     * @return the builder type specification
     */
    TypeSpec typeDeclaration() {
        return TypeSpec
                .classBuilder(name.value())
                .addModifiers(PUBLIC, STATIC)
                .addJavadoc(classJavadoc())
                .addField(initializedProtoBuilder())
                .addMethod(constructor())
                .addMethods(setters())
                .addMethod(rejectionMessage())
                .addMethod(build())
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
        CodeBlock javadoc = withNewLine("Prevent direct instantiation of the builder.");
        return constructorBuilder()
                .addJavadoc(javadoc)
                .addModifiers(PRIVATE)
                .build();
    }

    private MethodSpec rejectionMessage() {
        CodeBlock javadoc = withNewLine("Obtains the rejection and validates it.");
        return MethodSpec
                .methodBuilder("rejectionMessage")
                .addModifiers(PRIVATE)
                .addJavadoc(javadoc)
                .returns(protoRejection())
                .addStatement("$T message = $L.build()", protoRejection(), BUILDER_FIELD)
                .addStatement("$T.checkValid(message)", Validate.class)
                .addStatement("return message")
                .build();
    }

    @SuppressWarnings("DuplicateStringLiteralInspection") // The same string has different semantics
    private MethodSpec build() {
        CodeBlock javadoc = withNewLine("Creates the rejection from the builder and validates it.");
        return MethodSpec
                .methodBuilder("build")
                .addModifiers(PUBLIC)
                .addJavadoc(javadoc)
                .returns(throwableRejection())
                .addStatement("return new $T(this)", throwableRejection())
                .build();
    }

    private CodeBlock classJavadoc() {
        String rejectionName = rejection.simpleTypeName();
        return CodeBlock.builder()
                        .add("The builder for the {@code $L} rejection.", rejectionName)
                        .add(lineSeparator())
                        .build();
    }

    private FieldSpec initializedProtoBuilder() {
        ClassName protoBuilderClass = protoRejection().nestedClass(SimpleClassName.ofBuilder()
                                                                                  .value());
        return FieldSpec
                .builder(protoBuilderClass, BUILDER_FIELD, PRIVATE, FINAL)
                .initializer("$T.newBuilder()", protoRejection())
                .build();
    }

    private List<MethodSpec> setters() {
        List<MethodSpec> methods = newArrayList();
        for (FieldDeclaration field : rejectionFields) {
            MethodSpec setter = fieldSetter(field);
            methods.add(setter);
        }
        return methods;
    }

    private MethodSpec fieldSetter(FieldDeclaration field) {
        FieldName fieldName = field.name();
        String parameterName = fieldName.javaCase();
        String methodName = field.setterName();
        MethodSpec.Builder methodBuilder = MethodSpec
                .methodBuilder(methodName)
                .addModifiers(PUBLIC)
                .returns(thisType())
                .addParameter(field.typeName(), parameterName)
                .addStatement("$L.$L($L)", BUILDER_FIELD, methodName, parameterName)
                .addStatement("return this");
        Optional<String> comments = field.leadingComments();
        comments.ifPresent(
                text -> methodBuilder.addJavadoc(FormattedCodeBlock.from(text)
                                                                   .asJavadoc()));
        return methodBuilder.build();
    }

    /**
     * Obtains the class name of this builder.
     *
     * @return class name for the builder
     */
    private ClassName thisType() {
        return throwableRejection().nestedClass(name.value());
    }

    private ClassName protoRejection() {
        return rejection.rejectionMessage();
    }

    private ClassName throwableRejection() {
        return rejection.throwableRejection();
    }

    private static CodeBlock withNewLine(String text) {
        return CodeBlock.builder()
                        .add(text)
                        .add(lineSeparator())
                        .build();
    }
}
