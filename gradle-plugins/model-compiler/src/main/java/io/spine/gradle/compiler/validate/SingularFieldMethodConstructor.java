/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

package io.spine.gradle.compiler.validate;

import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import io.spine.base.ConversionException;
import io.spine.gradle.compiler.message.MessageTypeCache;
import io.spine.gradle.compiler.message.fieldtype.FieldType;
import io.spine.validate.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.Modifier;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static io.spine.tools.java.JavaCode.toJavaFieldName;
import static io.spine.gradle.compiler.validate.MethodConstructors.clearPrefix;
import static io.spine.gradle.compiler.validate.MethodConstructors.clearProperty;
import static io.spine.gradle.compiler.validate.MethodConstructors.createDescriptorStatement;
import static io.spine.gradle.compiler.validate.MethodConstructors.createValidateStatement;
import static io.spine.gradle.compiler.validate.MethodConstructors.getMessageBuilder;
import static io.spine.gradle.compiler.validate.MethodConstructors.rawSuffix;
import static io.spine.gradle.compiler.validate.MethodConstructors.returnThis;
import static java.lang.String.format;

/**
 * A method constructor of the {@code MethodSpec} objects for the
 * validating builders generation based on the Protobuf message declaration.
 *
 * <p>Constructs the {@code MethodSpec} objects for the singular fields.
 *
 * @author Illia Shepilov
 */
class SingularFieldMethodConstructor implements MethodConstructor {

    private static final String GETTER_PREFIX = "get";

    private final int fieldIndex;
    private final String fieldName;
    private final String methodNamePart;
    private final FieldType fieldType;
    private final ClassName fieldClassName;
    private final ClassName builderClassName;
    private final ClassName builderGenericClassName;
    private final FieldDescriptorProto fieldDescriptor;

    /**
     * Creates the {@code SingularFieldMethodConstructor}.
     *
     * @param builder the {@code SingularFieldMethodConstructorBuilder} instance
     */
    @SuppressWarnings("ConstantConditions")
    // The fields are checked in the {@code #build()} method
    // of the {@code SingularFieldConstructorBuilder} class.
    private SingularFieldMethodConstructor(SingularFieldConstructorBuilder builder) {
        super();
        this.fieldType = builder.getFieldType();
        this.fieldDescriptor = builder.getFieldDescriptor();
        this.fieldIndex = builder.getFieldIndex();
        this.builderGenericClassName = builder.getGenericClassName();
        final MessageTypeCache messageTypeCache = builder.getMessageTypeCache();
        this.fieldClassName = ClassNames.getParameterClassName(fieldDescriptor, messageTypeCache);
        final String javaClass = builder.getJavaClass();
        final String javaPackage = builder.getJavaPackage();
        this.builderClassName = ClassNames.getClassName(javaPackage, javaClass);
        this.fieldName = toJavaFieldName(fieldDescriptor.getName(), false);
        this.methodNamePart = toJavaFieldName(fieldName, true);
    }

    @Override
    public Collection<MethodSpec> construct() {
        final String javaFieldName = toJavaFieldName(fieldDescriptor.getName(), false);
        log().trace("The method construction for the {} singular field is started.", javaFieldName);
        final List<MethodSpec> methods = newArrayList();
        methods.add(constructSetter());

        if (!fieldClassName.equals(ClassNames.getStringClassName())) {
            methods.add(constructRawSetter());
        }

        methods.add(constructGetter());
        methods.add(constructClearMethods());
        log().trace("The method construction for the {} singular field is finished.",
                    javaFieldName);
        return methods;
    }

    private MethodSpec constructSetter() {
        log().trace("The setters construction for the singular field is started.");
        final String methodName = fieldType.getSetterPrefix() + methodNamePart;
        final String descriptorCodeLine = createDescriptorStatement(fieldIndex,
                                                                    builderGenericClassName);
        final ParameterSpec parameter = createParameterSpec(fieldDescriptor, false);

        final String setStatement = format("%s.%s(%s)", getMessageBuilder(), methodName, fieldName);
        final MethodSpec methodSpec =
                MethodSpec.methodBuilder(methodName)
                          .addModifiers(Modifier.PUBLIC)
                          .returns(builderClassName)
                          .addParameter(parameter)
                          .addException(ValidationException.class)
                          .addStatement(descriptorCodeLine, FieldDescriptor.class)
                          .addStatement(createValidateStatement(fieldName),
                                        fieldDescriptor.getName())
                          .addStatement(setStatement)
                          .addStatement(returnThis())
                          .build();
        log().trace("The setters construction for the singular field is finished.");
        return methodSpec;
    }

    private MethodSpec constructGetter() {
        log().trace("The getter construction for the singular field is started.");
        final String methodName = GETTER_PREFIX + methodNamePart;

        final MethodSpec methodSpec =
                MethodSpec.methodBuilder(methodName)
                          .addModifiers(Modifier.PUBLIC)
                          .returns(fieldClassName)
                          .addStatement("return " + getMessageBuilder() + '.' + methodName + "()")
                          .build();
        log().trace("The getter construction for the singular method is finished.");
        return methodSpec;
    }

    private MethodSpec constructClearMethods() {
        log().trace("The 'clear..()' method construction for the singular field is started.");
        final String methodBody = getMessageBuilder() + clearProperty(methodNamePart);

        final MethodSpec methodSpec =
                MethodSpec.methodBuilder(clearPrefix() + methodNamePart)
                          .addModifiers(Modifier.PUBLIC)
                          .returns(builderClassName)
                          .addStatement(methodBody)
                          .addStatement(returnThis())
                          .build();
        log().trace("The 'clear..()' method construction for the singular method is finished.");
        return methodSpec;
    }

    @SuppressWarnings("DuplicateStringLiteralInspection")
    // It cannot be used as the constant across the project.
    // Although it has the equivalent literal they have the different meaning.
    private MethodSpec constructRawSetter() {
        log().trace("The raw setters construction is started.");
        final String messageBuilderSetter = fieldType.getSetterPrefix() + methodNamePart;
        final String methodName = messageBuilderSetter + rawSuffix();
        final String descriptorCodeLine = createDescriptorStatement(fieldIndex,
                                                                    builderGenericClassName);
        final ParameterSpec parameter = createParameterSpec(fieldDescriptor, true);

        final String convertedVariableName = "convertedValue";
        final String convertedValue = format("final $T %s = convert(%s, $T.class)",
                                             convertedVariableName, fieldName);
        final String setStatement = format("%s.%s(%s)",
                                           getMessageBuilder(),
                                           messageBuilderSetter,
                                           convertedVariableName);
        final MethodSpec methodSpec =
                MethodSpec.methodBuilder(methodName)
                          .addModifiers(Modifier.PUBLIC)
                          .returns(builderClassName)
                          .addParameter(parameter)
                          .addException(ValidationException.class)
                          .addException(ConversionException.class)
                          .addStatement(descriptorCodeLine, FieldDescriptor.class)
                          .addStatement(convertedValue,
                                        fieldClassName, fieldClassName)
                          .addStatement(createValidateStatement(convertedVariableName),
                                        fieldDescriptor.getName())
                          .addStatement(setStatement)
                          .addStatement(returnThis())
                          .build();
        log().trace("The raw setters construction is finished.");
        return methodSpec;
    }

    private ParameterSpec createParameterSpec(FieldDescriptorProto fieldDescriptor, boolean raw) {
        final ClassName methodParamClass = raw ? ClassNames.getStringClassName() : fieldClassName;
        final String paramName = toJavaFieldName(fieldDescriptor.getName(), false);
        final ParameterSpec result = ParameterSpec.builder(methodParamClass, paramName)
                                                  .build();
        return result;
    }

    /**
     * Creates a new builder for the {@code SingularFieldMethodConstructor} class.
     *
     * @return constructed builder
     */
    static SingularFieldConstructorBuilder newBuilder() {
        return new SingularFieldConstructorBuilder();
    }

    /**
     * A builder class for the {@code SingularFieldMethodConstructor} class.
     */
    static class SingularFieldConstructorBuilder
            extends AbstractMethodConstructorBuilder<SingularFieldMethodConstructor> {

        @Override
        SingularFieldMethodConstructor build() {
            super.build();
            return new SingularFieldMethodConstructor(this);
        }
    }

    private enum LogSingleton {
        INSTANCE;

        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(SingularFieldMethodConstructor.class);
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }
}
