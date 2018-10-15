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

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import io.spine.code.java.SimpleClassName;
import io.spine.code.proto.FieldName;
import io.spine.protobuf.Messages;
import io.spine.tools.compiler.field.type.FieldType;
import io.spine.validate.Validate;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.squareup.javapoet.MethodSpec.constructorBuilder;
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
    //TODO:2018-10-12:dmytro.grankin: Get rid of the field?
    // Introduce `FieldDeclaration`, which composes the field name and the field type
    // and generates the setter name
    private final Map<String, FieldType> rejectionFields;

    RejectionBuilder(GeneratedRejectionDeclaration rejection,
                     Map<String, FieldType> fields) {
        this.rejection = rejection;
        rejectionFields = fields;
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
                .addJavadoc("@return a new builder for the rejection")
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
        return constructorBuilder()
                .addJavadoc("Prevent direct instantiation of the builder.")
                .addModifiers(PRIVATE)
                .build();
    }

    private MethodSpec rejectionMessage() {
        return MethodSpec
                .methodBuilder("rejectionMessage")
                .addModifiers(PRIVATE)
                .addJavadoc("Obtains the rejection and validates it.")
                .returns(protoRejection())
                .addStatement("$T message = $L.build()", protoRejection(), BUILDER_FIELD)
                .addStatement("$T.checkValid(message)", Validate.class)
                .addStatement("return message")
                .build();
    }

    @SuppressWarnings("DuplicateStringLiteralInspection") // The same string has different semantics
    private MethodSpec build() {
        return MethodSpec
                .methodBuilder("build")
                .addModifiers(PUBLIC)
                .addJavadoc("Creates the rejection from the builder and validates it.")
                .returns(throwableRejection())
                .addStatement("return new $T(this)", throwableRejection())
                .build();
    }

    private String classJavadoc() {
        return "The builder for the " + rejection.simpleTypeName() + " rejection.";
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
        for (Map.Entry<String, FieldType> field : rejectionFields.entrySet()) {
            MethodSpec setter = fieldSetter(field.getKey(), field.getValue());
            methods.add(setter);
        }
        return methods;
    }

    private MethodSpec fieldSetter(String fieldName, FieldType type) {
        FieldName fName = FieldName.of(fieldName);
        String parameterName = fName.javaCase();
        String methodName = type.getSetterPrefix() + fName.toCamelCase();
        return MethodSpec
                .methodBuilder(methodName)
                .addModifiers(PUBLIC)
                .returns(thisType())
                //TODO:2018-10-12:dmytro.grankin: Javadoc
                .addParameter(type.getTypeName(), parameterName)
                .addStatement("$L.$L($L)", BUILDER_FIELD, methodName, parameterName)
                .addStatement("return this")
                .build();
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
}
