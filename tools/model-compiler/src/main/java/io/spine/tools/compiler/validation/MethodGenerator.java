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
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import io.spine.protobuf.Messages;
import io.spine.tools.compiler.MessageTypeCache;
import io.spine.tools.compiler.field.type.FieldType;
import io.spine.tools.compiler.field.type.FieldTypeFactory;

import javax.lang.model.element.Modifier;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static io.spine.tools.compiler.field.type.FieldTypes.isMap;
import static io.spine.tools.compiler.field.type.FieldTypes.isRepeated;
import static io.spine.tools.compiler.validation.ClassNames.getValidatorMessageClassName;

/**
 * Serves as assembler for the generated methods based on the Protobuf message declaration.
 *
 * @author Illia Shepilov
 */
class MethodGenerator {

    private final String javaClass;
    private final String javaPackage;
    private final ClassName builderGenericClassName;
    private final MessageTypeCache typeCache;
    private final DescriptorProto message;

    MethodGenerator(VBType type, MessageTypeCache typeCache) {
        this.javaClass = type.getJavaClass();
        this.javaPackage = type.getJavaPackage();
        this.message = type.getDescriptor();
        this.typeCache = typeCache;
        String className = message.getName();
        this.builderGenericClassName =
                getValidatorMessageClassName(javaPackage, typeCache, className);
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
        MethodSpec result = MethodSpec.constructorBuilder()
                                      .addModifiers(Modifier.PRIVATE)
                                      .build();
        return result;
    }

    private MethodSpec createNewBuilderMethod() {
        ClassName builderClass = ClassNames.getClassName(javaPackage, javaClass);
        MethodSpec buildMethod = MethodSpec.methodBuilder(Messages.METHOD_NEW_BUILDER)
                                           .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                           .returns(builderClass)
                                           .addStatement("return new $T()", builderClass)
                                           .build();
        return buildMethod;
    }

    private List<MethodSpec> createFieldMethods() {
        Factory factory = new Factory();
        ImmutableList.Builder<MethodSpec> result = ImmutableList.builder();
        int index = 0;
        for (FieldDescriptorProto field : message.getFieldList()) {
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
         * the passed {@code FieldDescriptorProto}
         *
         * @param field the descriptor for the field
         * @param index the index of the field
         * @return the method constructor instance
         */
        private MethodConstructor create(FieldDescriptorProto field, int index) {
            if (isMap(field)) {
                return doCreate(MapFieldMethodConstructor.newBuilder(), field, index);
            }
            if (isRepeated(field)) {
                return doCreate(RepeatedFieldMethodConstructor.newBuilder(), field, index);
            }
            return doCreate(SingularFieldMethodConstructor.newBuilder(), field, index);
        }

        private MethodConstructor doCreate(AbstractMethodConstructorBuilder builder,
                                           FieldDescriptorProto field,
                                           int fieldIndex) {
            FieldTypeFactory factory =
                    new FieldTypeFactory(message, typeCache.getCachedTypes());
            FieldType fieldType = factory.create(field);
            MethodConstructor methodConstructor =
                    builder.setField(field)
                           .setFieldType(fieldType)
                           .setFieldIndex(fieldIndex)
                           .setJavaClass(javaClass)
                           .setJavaPackage(javaPackage)
                           .setBuilderGenericClassName(builderGenericClassName)
                           .setTypeCache(typeCache)
                           .build();
            return methodConstructor;
        }
    }
}
