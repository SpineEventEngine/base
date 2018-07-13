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

import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import io.spine.base.ConversionException;
import io.spine.tools.compiler.MessageTypeCache;
import io.spine.tools.compiler.fieldtype.FieldType;
import io.spine.code.proto.FieldName;
import io.spine.validate.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.Modifier;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static io.spine.tools.compiler.validation.ClassNames.getClassName;
import static io.spine.tools.compiler.validation.ClassNames.getParameterClassName;
import static io.spine.tools.compiler.validation.ClassNames.getStringClassName;
import static io.spine.tools.compiler.validation.MethodConstructors.clearPrefix;
import static io.spine.tools.compiler.validation.MethodConstructors.clearProperty;
import static io.spine.tools.compiler.validation.MethodConstructors.createDescriptorStatement;
import static io.spine.tools.compiler.validation.MethodConstructors.createValidateStatement;
import static io.spine.tools.compiler.validation.MethodConstructors.getMessageBuilder;
import static io.spine.tools.compiler.validation.MethodConstructors.rawSuffix;
import static io.spine.tools.compiler.validation.MethodConstructors.returnThis;
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
    private final FieldDescriptorProto field;

    /**
     * Constructs the instance by the passed builder.
     *
     * <p>The passed builder {@linkplain SingularFieldConstructorBuilder#checkFields() ensures}
     * non-null values of its fields prior to calling this constructor.
     */
    @SuppressWarnings("ConstantConditions") // See Javadoc above.
    private SingularFieldMethodConstructor(SingularFieldConstructorBuilder builder) {
        super();
        this.fieldType = builder.getFieldType();
        this.field = builder.getField();
        this.fieldIndex = builder.getFieldIndex();
        this.builderGenericClassName = builder.getGenericClassName();
        MessageTypeCache messageTypeCache = builder.getTypeCache();
        this.fieldClassName = getParameterClassName(field, messageTypeCache);
        this.builderClassName = getClassName(builder.getJavaPackage(), builder.getJavaClass());
        FieldName fieldName = FieldName.of(field);
        this.fieldName = fieldName.javaCase();
        this.methodNamePart = fieldName.toCamelCase();
    }

    @Override
    public Collection<MethodSpec> construct() {
        Logger log = log();
        // The variable is used for tracing only.
        String javaFieldName = log.isTraceEnabled()
                ? FieldName.of(field)
                           .javaCase()
                : null;

        log.debug("The method construction for the {} singular field is started.", javaFieldName);
        List<MethodSpec> methods = newArrayList();
        methods.add(constructSetter());

        if (!fieldClassName.equals(getStringClassName())) {
            methods.add(constructRawSetter());
        }

        methods.add(constructGetter());
        methods.add(constructClearMethods());
        log.debug("The method construction for the {} singular field is finished.", javaFieldName);
        return methods;
    }

    private MethodSpec constructSetter() {
        log().debug("The setters construction for the singular field is started.");
        String methodName = fieldType.getSetterPrefix() + methodNamePart;
        String descriptorCodeLine = createDescriptorStatement(fieldIndex,
                                                                    builderGenericClassName);
        ParameterSpec parameter = createParameterSpec(field, false);

        String setStatement = format("%s.%s(%s)", getMessageBuilder(), methodName, fieldName);
        MethodSpec methodSpec =
                MethodSpec.methodBuilder(methodName)
                          .addModifiers(Modifier.PUBLIC)
                          .returns(builderClassName)
                          .addParameter(parameter)
                          .addException(ValidationException.class)
                          .addStatement(descriptorCodeLine, FieldDescriptor.class)
                          .addStatement(createValidateStatement(fieldName),
                                        field.getName())
                          .addStatement(setStatement)
                          .addStatement(returnThis())
                          .build();
        log().debug("The setters construction for the singular field is finished.");
        return methodSpec;
    }

    private MethodSpec constructGetter() {
        log().debug("The getter construction for the singular field is started.");
        String methodName = GETTER_PREFIX + methodNamePart;

        MethodSpec methodSpec =
                MethodSpec.methodBuilder(methodName)
                          .addModifiers(Modifier.PUBLIC)
                          .returns(fieldClassName)
                          .addStatement("return " + getMessageBuilder() + '.' + methodName + "()")
                          .build();
        log().debug("The getter construction for the singular method is finished.");
        return methodSpec;
    }

    private MethodSpec constructClearMethods() {
        log().debug("The 'clear..()' method construction for the singular field is started.");
        String methodBody = getMessageBuilder() + clearProperty(methodNamePart);

        MethodSpec methodSpec =
                MethodSpec.methodBuilder(clearPrefix() + methodNamePart)
                          .addModifiers(Modifier.PUBLIC)
                          .returns(builderClassName)
                          .addStatement(methodBody)
                          .addStatement(returnThis())
                          .build();
        log().debug("The 'clear..()' method construction for the singular method is finished.");
        return methodSpec;
    }

    @SuppressWarnings("DuplicateStringLiteralInspection")
    // It cannot be used as the constant across the project.
    // Although it has the equivalent literal they have the different meaning.
    private MethodSpec constructRawSetter() {
        log().debug("The raw setters construction is started.");
        String messageBuilderSetter = fieldType.getSetterPrefix() + methodNamePart;
        String methodName = messageBuilderSetter + rawSuffix();
        String descriptorCodeLine = createDescriptorStatement(fieldIndex,
                                                                    builderGenericClassName);
        ParameterSpec parameter = createParameterSpec(field, true);

        String convertedVariableName = "convertedValue";
        String convertedValue = format("final $T %s = convert(%s, $T.class)",
                                             convertedVariableName, fieldName);
        String setStatement = format("%s.%s(%s)",
                                           getMessageBuilder(),
                                           messageBuilderSetter,
                                           convertedVariableName);
        MethodSpec methodSpec =
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
                                        field.getName())
                          .addStatement(setStatement)
                          .addStatement(returnThis())
                          .build();
        log().debug("The raw setters construction is finished.");
        return methodSpec;
    }

    private ParameterSpec createParameterSpec(FieldDescriptorProto field, boolean raw) {
        ClassName methodParamClass = raw
                ? getStringClassName()
                : fieldClassName;
        String paramName = FieldName.of(field)
                                          .javaCase();
        ParameterSpec result = ParameterSpec.builder(methodParamClass, paramName)
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
            checkFields();
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
