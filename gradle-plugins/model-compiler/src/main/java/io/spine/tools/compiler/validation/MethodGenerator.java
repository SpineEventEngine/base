/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import io.spine.protobuf.Messages;
import io.spine.tools.compiler.MessageTypeCache;
import io.spine.tools.compiler.fieldtype.FieldType;
import io.spine.tools.compiler.fieldtype.FieldTypeFactory;

import javax.lang.model.element.Modifier;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static io.spine.tools.compiler.fieldtype.FieldTypes.isMap;
import static io.spine.tools.compiler.fieldtype.FieldTypes.isRepeated;

/**
 * Serves as assembler for the generated methods based on the Protobuf message declaration.
 *
 * @author Illia Shepilov
 */
class MethodGenerator {

    private final String javaClass;
    private final String javaPackage;
    private final ClassName builderGenericClassName;
    private final MessageTypeCache messageTypeCache;
    private final DescriptorProto descriptor;

    MethodGenerator(VBType metadata, MessageTypeCache messageTypeCache) {
        this.javaClass = metadata.getJavaClass();
        this.javaPackage = metadata.getJavaPackage();
        this.descriptor = metadata.getDescriptor();
        this.messageTypeCache = messageTypeCache;
        final String className = descriptor.getName();
        builderGenericClassName = ClassNames.getValidatorMessageClassName(javaPackage,
                                                                          messageTypeCache,
                                                                          className);
    }

    /**
     * Creates the Java methods according to the Protobuf message declaration.
     *
     * @return the generated methods
     */
    Collection<MethodSpec> createMethods() {
        final List<MethodSpec> methods = newArrayList();

        methods.add(createPrivateConstructor());
        methods.add(createNewBuilderMethod());
        methods.addAll(createGeneratedSettersAndGetters());

        return methods;
    }

    private static MethodSpec createPrivateConstructor() {
        final MethodSpec result = MethodSpec.constructorBuilder()
                                            .addModifiers(Modifier.PRIVATE)
                                            .build();
        return result;
    }

    private MethodSpec createNewBuilderMethod() {
        final ClassName builderClass = ClassNames.getClassName(javaPackage, javaClass);
        final MethodSpec buildMethod = MethodSpec.methodBuilder(Messages.METHOD_NEW_BUILDER)
                                                 .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                                 .returns(builderClass)
                                                 .addStatement("return new $T()", builderClass)
                                                 .build();
        return buildMethod;
    }

    private Collection<MethodSpec> createGeneratedSettersAndGetters() {
        final MethodConstructorFactory methodConstructorFactory = new MethodConstructorFactory();
        final List<MethodSpec> setters = newArrayList();
        int index = 0;
        for (FieldDescriptorProto fieldDescriptor : descriptor.getFieldList()) {
            final MethodConstructor methodConstructor =
                    methodConstructorFactory.getMethodConstructor(fieldDescriptor, index);
            final Collection<MethodSpec> methods = methodConstructor.construct();
            setters.addAll(methods);

            ++index;
        }

        return setters;
    }

    /**
     * A factory for the method constructors.
     */
    private class MethodConstructorFactory {

        /**
         * Returns the concrete method constructor according to
         * the passed {@code FieldDescriptorProto}
         *
         * @param fieldDescriptor the descriptor for the field
         * @param fieldIndex      the index of the field
         * @return the method constructor instance
         */
        private MethodConstructor getMethodConstructor(FieldDescriptorProto fieldDescriptor,
                                                       int fieldIndex) {
            if (isMap(fieldDescriptor)) {
                return createMethodConstructor(MapFieldMethodConstructor.newBuilder(),
                                               fieldDescriptor,
                                               fieldIndex);
            }
            if (isRepeated(fieldDescriptor)) {
                return createMethodConstructor(RepeatedFieldMethodConstructor.newBuilder(),
                                               fieldDescriptor,
                                               fieldIndex);
            }
            return createMethodConstructor(SingularFieldMethodConstructor.newBuilder(),
                                           fieldDescriptor,
                                           fieldIndex);
        }

        private MethodConstructor createMethodConstructor(AbstractMethodConstructorBuilder builder,
                                                          FieldDescriptorProto dscr,
                                                          int fieldIndex) {
            final FieldTypeFactory factory =
                    new FieldTypeFactory(descriptor, messageTypeCache.getCachedTypes());
            final FieldType fieldType = factory.create(dscr);
            final MethodConstructor methodConstructor =
                    builder.setFieldDescriptor(dscr)
                           .setFieldType(fieldType)
                           .setFieldIndex(fieldIndex)
                           .setJavaClass(javaClass)
                           .setJavaPackage(javaPackage)
                           .setBuilderGenericClassName(builderGenericClassName)
                           .setMessageTypeCache(messageTypeCache)
                           .build();
            return methodConstructor;
        }
    }
}
