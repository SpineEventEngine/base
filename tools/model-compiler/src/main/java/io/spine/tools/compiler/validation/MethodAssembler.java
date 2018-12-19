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

package io.spine.tools.compiler.validation;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import io.spine.code.proto.FieldDeclaration;
import io.spine.code.proto.MessageType;
import io.spine.protobuf.Messages;
import io.spine.tools.compiler.field.type.FieldType;

import javax.lang.model.element.Modifier;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Serves as assembler for the generated methods based on the Protobuf message declaration.
 */
class MethodAssembler {

    private final MessageType type;

    MethodAssembler(MessageType messageType) {
        this.type = messageType;
    }

    /**
     * Creates the Java methods according to the Protobuf message declaration.
     *
     * @return the generated methods
     */
    Collection<MethodSpec> createMethods() {
        List<MethodSpec> methods = newArrayList();

        methods.add(createPrivateConstructor());
        methods.add(createNewBuilderMethod());
        methods.addAll(createFieldMethods());

        return methods;
    }

    private static MethodSpec createPrivateConstructor() {
        MethodSpec result = MethodSpec
                .constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .build();
        return result;
    }

    private MethodSpec createNewBuilderMethod() {
        ClassName validatingBuilderClass =
                ClassName.get(type.javaPackage()
                                  .value(), type.getValidatingBuilderClass()
                                                .value());
        MethodSpec buildMethod = MethodSpec
                .methodBuilder(Messages.METHOD_NEW_BUILDER)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(validatingBuilderClass)
                .addStatement("return new $T()", validatingBuilderClass)
                .build();
        return buildMethod;
    }

    private List<MethodSpec> createFieldMethods() {
        Factory factory = new Factory();
        ImmutableList.Builder<MethodSpec> result = ImmutableList.builder();
        int index = 0;
        for (FieldDeclaration field : type.fields()) {
            MethodConstructor method = factory.create(field, index);
            Collection<MethodSpec> methods = method.construct();
            result.addAll(methods);

            ++index;
        }

        return result.build();
    }

    /**
     * A factory for the method constructors.
     */
    private class Factory {

        /**
         * Returns the concrete method constructor according to
         * the passed {@code FieldDescriptorProto}.
         *
         * @param field the descriptor for the field
         * @param index the index of the field
         * @return the method constructor instance
         */
        private MethodConstructor create(FieldDeclaration field, int index) {
            return doCreate(builderFor(field), field, index);
        }

        private AbstractMethodBuilder builderFor(FieldDeclaration field) {
            if (field.isMap()) {
                return MapFieldMethod.newBuilder();
            }
            if (field.isRepeated()) {
                return RepeatedFieldMethod.newBuilder();
            }
            return SingularFieldMethod.newBuilder();
        }

        private MethodConstructor doCreate(AbstractMethodBuilder builder,
                                           FieldDeclaration field,
                                           int fieldIndex) {
            FieldType fieldType = FieldType.create(field);
            MethodConstructor methodConstructor =
                    builder.setField(field.descriptor())
                           .setFieldType(fieldType)
                           .setFieldIndex(fieldIndex)
                           .setJavaClass(type.javaClass().getName())
                           .setJavaPackage(type.javaPackage().value())
                           .setBuilderGenericClassName(ClassName.bestGuess(type.builderClass()
                                                                               .value()))
                           .build();
            return methodConstructor;
        }
    }
}
