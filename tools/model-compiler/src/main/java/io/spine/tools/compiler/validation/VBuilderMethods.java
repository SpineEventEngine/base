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

package io.spine.tools.compiler.validation;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.protobuf.Descriptors;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import io.spine.code.java.SimpleClassName;
import io.spine.code.proto.FieldDeclaration;
import io.spine.code.proto.MessageType;
import io.spine.protobuf.Messages;
import io.spine.tools.compiler.field.type.FieldType;

import javax.lang.model.element.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static io.spine.tools.compiler.validation.Methods.callSuper;
import static io.spine.tools.compiler.validation.Methods.returnThis;

/**
 * Serves as assembler for the generated methods based on the Protobuf message declaration.
 */
final class VBuilderMethods {

    private final MessageType type;
    @SuppressWarnings("DuplicateStringLiteralInspection") // local semantic.
    private static final String MERGE_FROM_METHOD_PARAMETER_NAME = "message";

    private VBuilderMethods(MessageType messageType) {
        this.type = messageType;
    }

    static ImmutableList<MethodSpec> methodsOf(MessageType type) {
        VBuilderMethods methods = new VBuilderMethods(type);
        return methods.all();
    }

    /**
     * Creates the Java methods according to the Protobuf message declaration.
     *
     * @return the generated methods
     */
    ImmutableList<MethodSpec> all() {
        return ImmutableList.<MethodSpec>builder()
                .add(privateConstructor())
                .add(newBuilderMethod())
                .addAll(fieldMethods())
                .add(mergeFromMethod())
                .build();
    }

    private static MethodSpec privateConstructor() {
        MethodSpec result = MethodSpec
                .constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .build();
        return result;
    }

    private MethodSpec newBuilderMethod() {
        ClassName vbClass = validatingBuilderClass();
        MethodSpec buildMethod = MethodSpec
                .methodBuilder(Messages.METHOD_NEW_BUILDER)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(vbClass)
                .addStatement("return new $T()", vbClass)
                .build();
        return buildMethod;
    }

    private ClassName validatingBuilderClass() {
        return ClassName.get(type.javaPackage()
                                 .value(), type.validatingBuilderClass()
                                               .value());
    }

    private MethodSpec mergeFromMethod() {
        String methodName = "mergeFrom";
        SimpleClassName vBuilderClass = type.validatingBuilderClass();
        ClassName className = ClassName.bestGuess(vBuilderClass.toString());
        ClassName messageClass = messageClass();
        MethodSpec mergeFrom = MethodSpec
                .methodBuilder(methodName)
                .addAnnotation(CanIgnoreReturnValue.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(messageClass, MERGE_FROM_METHOD_PARAMETER_NAME)
                .addStatement(checkAllFields())
                .addStatement(callSuper(methodName, MERGE_FROM_METHOD_PARAMETER_NAME))
                .addStatement(returnThis())
                .returns(className)
                .build();
        return mergeFrom;
    }

    /**
     * Returns a statement that loops over all fields in the specified {@code message},
     * and validates whether a {@code (set_once)} validation rule is being violated.
     *
     * Example of a returned statement:
     *          <pre>
     *          {@code
     *              Map<FieldDescriptor, Object> fieldsMap = message.getAllFields();
     *              for (Map.Entry<FieldDescriptor, Object> entry : message.entrySet() {
     *                      validateSetOnce(entry.getKey());
     *              }
     *          }
     *          </pre>
     *
     * @return a statement that checks whether all fields are present during a {@code mergeFrom()}
     */
    private static String checkAllFields() {
        String fieldsMap = "fieldsMap";
        String loopLocalVariable = "entry";
        CodeBlock codeBlock = CodeBlock
                .builder()
                .addStatement(
                        "$T<$T, $T> $N = $N.getAllFields()",
                        ClassName.get(Map.class),
                        ClassName.get(Descriptors.FieldDescriptor.class),
                        ClassName.get(Object.class),
                        fieldsMap,
                        MERGE_FROM_METHOD_PARAMETER_NAME)
                .beginControlFlow(
                        "for ($T<$T, $T> $N : $N.entrySet())",
                        ClassName.get(Map.Entry.class),
                        ClassName.get(Descriptors.FieldDescriptor.class),
                        ClassName.get(Object.class),
                        loopLocalVariable,
                        fieldsMap)
                .addStatement("validateSetOnce($N.getKey())", loopLocalVariable)
                .endControlFlow()
                .build();
        return codeBlock.toString();
    }

    private List<MethodSpec> fieldMethods() {
        Factory factory = new Factory();
        ImmutableList.Builder<MethodSpec> result = ImmutableList.builder();
        int index = 0;
        for (FieldDeclaration field : type.fields()) {
            MethodGroup method = factory.create(field, index);
            Collection<MethodSpec> methods = method.generate();
            result.addAll(methods);

            ++index;
        }

        return result.build();
    }

    private ClassName messageClass() {
        String fullyQualifiedDotted = type.javaClassName()
                                          .toDotted()
                                          .value();
        return ClassName.bestGuess(fullyQualifiedDotted);
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
        private MethodGroup create(FieldDeclaration field, int index) {
            FieldType fieldType = FieldType.of(field);
            MethodGroup methodGroup = builderFor(field)
                    .setField(field.descriptor())
                    .setFieldType(fieldType)
                    .setFieldIndex(index)
                    // The name of the Validating Builder class.
                    .setJavaClass(type.validatingBuilderClass()
                                      .value())
                    .setJavaPackage(type.javaPackage()
                                        .value())
                    .setGenericClassName(messageClass())
                    .build();
            return methodGroup;
        }

        private AbstractMethodGroupBuilder builderFor(FieldDeclaration field) {
            if (field.isMap()) {
                return MapFieldMethods.newBuilder();
            }
            if (field.isRepeated()) {
                return RepeatedFieldMethods.newBuilder();
            }
            return SingularFieldMethods.newBuilder();
        }

    }
}
