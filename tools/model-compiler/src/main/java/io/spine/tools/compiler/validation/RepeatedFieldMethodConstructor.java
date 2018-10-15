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
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import io.spine.base.ConversionException;
import io.spine.code.proto.FieldName;
import io.spine.code.proto.ScalarType;
import io.spine.logging.Logging;
import io.spine.tools.compiler.MessageTypeCache;
import io.spine.tools.compiler.field.type.FieldType;
import io.spine.validate.ValidationException;

import javax.lang.model.element.Modifier;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM;
import static io.spine.tools.compiler.annotation.Annotations.canIgnoreReturnValue;
import static io.spine.tools.compiler.validation.ClassNames.getClassName;
import static io.spine.tools.compiler.validation.ClassNames.getParameterClassName;
import static io.spine.tools.compiler.validation.MethodConstructors.clearPrefix;
import static io.spine.tools.compiler.validation.MethodConstructors.clearProperty;
import static io.spine.tools.compiler.validation.MethodConstructors.createConvertSingularValue;
import static io.spine.tools.compiler.validation.MethodConstructors.createDescriptorStatement;
import static io.spine.tools.compiler.validation.MethodConstructors.createValidateStatement;
import static io.spine.tools.compiler.validation.MethodConstructors.getMessageBuilder;
import static io.spine.tools.compiler.validation.MethodConstructors.rawSuffix;
import static io.spine.tools.compiler.validation.MethodConstructors.removePrefix;
import static io.spine.tools.compiler.validation.MethodConstructors.returnThis;
import static java.lang.String.format;

/**
 * A method constructor of the {@code MethodSpec} objects based on the Protobuf message declaration.
 *
 * <p>Constructs the {@code MethodSpec} objects for the repeated fields.
 *
 * @author Illia Shepilov
 */
@SuppressWarnings("DuplicateStringLiteralInspection")
// It cannot be used as the constant across the project.
// Although it has the equivalent literal they have the different meaning.
class RepeatedFieldMethodConstructor implements MethodConstructor, Logging {

    private static final String VALUE = "value";
    private static final String INDEX = "index";
    private static final String ADD_PREFIX = "add";
    private static final String SET_PREFIX = "set";
    private static final String ADD_RAW_PREFIX = "addRaw";
    private static final String SET_RAW_PREFIX = "setRaw";
    private static final String CONVERTED_VALUE = "convertedValue";

    private final int fieldIndex;
    private final FieldType fieldType;
    private final String javaFieldName;
    private final String methodNamePart;
    private final ClassName builderClassName;
    private final ClassName listElementClassName;
    private final ClassName builderGenericClassName;
    private final FieldDescriptorProto fieldDescriptor;
    private final boolean isScalarOrEnum;

    /**
     * Constructs the {@code RepeatedFieldMethodConstructor}.
     *
     * @param builder the {@code RepeatedFieldMethodConstructorBuilder} instance
     */
    @SuppressWarnings("ConstantConditions")
    // The fields are checked in the {@code #build()} method
    // of the {@code RepeatedFieldMethodsConstructorBuilder} class.
    private RepeatedFieldMethodConstructor(RepeatedFieldMethodsConstructorBuilder builder) {
        super();
        this.fieldType = builder.getFieldType();
        this.fieldIndex = builder.getFieldIndex();
        this.fieldDescriptor = builder.getField();
        this.builderGenericClassName = builder.getGenericClassName();
        FieldName fieldName = FieldName.of(fieldDescriptor);
        this.javaFieldName = fieldName.javaCase();
        this.methodNamePart = fieldName.toCamelCase();
        String javaClass = builder.getJavaClass();
        String javaPackage = builder.getJavaPackage();
        this.builderClassName = getClassName(javaPackage, javaClass);
        MessageTypeCache messageTypeCache = builder.getTypeCache();
        this.listElementClassName = getParameterClassName(fieldDescriptor, messageTypeCache);
        this.isScalarOrEnum = isScalarType(fieldDescriptor) || isEnumType(fieldDescriptor);
    }

    @Override
    public Collection<MethodSpec> construct() {
        log().debug("The methods construction for the {} repeated field is started.",
                    javaFieldName);

        List<MethodSpec> methods = newArrayList();
        methods.add(createGetter());
        methods.addAll(createRepeatedMethods());
        methods.addAll(createRepeatedRawMethods());

        log().debug("The methods construction for the {} repeated field is finished.",
                    javaFieldName);
        return methods;
    }

    private MethodSpec createGetter() {
        log().debug("The getter construction for the repeated field is started.");

        String methodName = "get" + methodNamePart;
        ClassName rawType = ClassName.get(List.class);
        ParameterizedTypeName returnType = ParameterizedTypeName.get(rawType,
                                                                     listElementClassName);
        String returnStatement = format("return %s.get%sList()",
                                        getMessageBuilder(), methodNamePart);
        MethodSpec methodSpec = MethodSpec
                .methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(returnType)
                .addStatement(returnStatement)
                .build();

        log().debug("The getter construction for the repeated field is finished.");
        return methodSpec;
    }

    private Collection<MethodSpec> createRepeatedRawMethods() {
        log().debug("The raw methods construction for the repeated field is is started.");

        List<MethodSpec> methods = newArrayList();
        methods.add(createRawAddObjectMethod());
        methods.add(createRawSetObjectByIndexMethod());
        methods.add(createRawAddAllMethod());

        // Some methods are not available in Protobuf Message.Builder for scalar types.
        if (!isScalarOrEnum) {
            methods.add(createRawAddObjectByIndexMethod());
        }

        log().debug("The raw methods construction for the repeated field is is finished.");
        return methods;
    }

    private Collection<MethodSpec> createRepeatedMethods() {
        List<MethodSpec> methods = newArrayList();

        methods.add(createClearMethod());
        methods.add(createAddObjectMethod());
        methods.add(createSetObjectByIndexMethod());
        methods.add(createAddAllMethod());

        // Some methods are not available in Protobuf Message.Builder for scalar types and enums.
        if (!isScalarOrEnum) {
            methods.add(createAddObjectByIndexMethod());
            methods.add(createRemoveObjectByIndexMethod());
        }
        return methods;
    }

    private static boolean isScalarType(FieldDescriptorProto fieldDescriptor) {
        boolean isScalarType = false;
        Type type = fieldDescriptor.getType();
        for (ScalarType scalarType : ScalarType.values()) {
            if (scalarType.getProtoScalarType() == type) {
                isScalarType = true;
            }
        }
        return isScalarType;
    }

    private static boolean isEnumType(FieldDescriptorProto fieldDescriptor) {
        Type type = fieldDescriptor.getType();
        boolean result = type == TYPE_ENUM;
        return result;
    }

    private MethodSpec createRawAddObjectMethod() {
        String methodName = ADD_RAW_PREFIX + methodNamePart;
        String descriptorCodeLine = createDescriptorStatement(fieldIndex,
                                                              builderGenericClassName);
        String addValueStatement = getMessageBuilder() + '.'
                + ADD_PREFIX + methodNamePart + "(convertedValue)";
        String convertStatement = createValidateStatement(CONVERTED_VALUE);
        MethodSpec result = MethodSpec
                .methodBuilder(methodName)
                .addAnnotation(canIgnoreReturnValue())
                .returns(builderClassName)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, VALUE)
                .addException(ValidationException.class)
                .addException(ConversionException.class)
                .addStatement(createConvertSingularValue(VALUE),
                              listElementClassName,
                              listElementClassName)
                .addStatement(descriptorCodeLine, FieldDescriptor.class)
                .addStatement(convertStatement,
                              fieldDescriptor.getName())
                .addStatement(addValueStatement)
                .addStatement(returnThis())
                .build();
        return result;
    }

    private MethodSpec createRawAddObjectByIndexMethod() {
        MethodSpec result = modifyCollectionByIndexWithRaw(ADD_RAW_PREFIX, ADD_PREFIX);
        return result;
    }

    private MethodSpec createRawSetObjectByIndexMethod() {
        return modifyCollectionByIndexWithRaw(SET_RAW_PREFIX, SET_PREFIX);
    }

    private MethodSpec modifyCollectionByIndexWithRaw(String methodNamePrefix,
                                                      String realBuilderCallPrefix) {
        String methodName = methodNamePrefix + methodNamePart;
        String descriptorCodeLine = createDescriptorStatement(fieldIndex,
                                                              builderGenericClassName);
        String modificationStatement =
                format("%s.%s%s(%s, convertedValue)",
                       getMessageBuilder(), realBuilderCallPrefix, methodNamePart, INDEX);
        String convertStatement = createValidateStatement(CONVERTED_VALUE);
        MethodSpec result = MethodSpec
                .methodBuilder(methodName)
                .addAnnotation(canIgnoreReturnValue())
                .returns(builderClassName)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.INT, INDEX)
                .addParameter(String.class, VALUE)
                .addException(ValidationException.class)
                .addException(ConversionException.class)
                .addStatement(createConvertSingularValue(VALUE),
                              listElementClassName,
                              listElementClassName)
                .addStatement(descriptorCodeLine, FieldDescriptor.class)
                .addStatement(convertStatement,
                              fieldDescriptor.getName())
                .addStatement(modificationStatement)
                .addStatement(returnThis())
                .build();
        return result;
    }

    private MethodSpec createRawAddAllMethod() {
        String methodName = fieldType.getSetterPrefix() + rawSuffix() + methodNamePart;
        String descriptorCodeLine = createDescriptorStatement(fieldIndex,
                                                              builderGenericClassName);
        String addAllValues = getMessageBuilder()
                + format(".addAll%s(%s)", methodNamePart, CONVERTED_VALUE);
        MethodSpec result = MethodSpec
                .methodBuilder(methodName)
                .addAnnotation(canIgnoreReturnValue())
                .returns(builderClassName)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, VALUE)
                .addException(ValidationException.class)
                .addException(ConversionException.class)
                .addStatement(createGetConvertedCollectionValue(),
                              List.class,
                              listElementClassName,
                              listElementClassName)
                .addStatement(descriptorCodeLine, FieldDescriptor.class)
                .addStatement(createValidateStatement(CONVERTED_VALUE),
                              fieldDescriptor.getName())
                .addStatement(addAllValues)
                .addStatement(returnThis())
                .build();
        return result;
    }

    private MethodSpec createAddAllMethod() {
        String methodName = fieldType.getSetterPrefix() + methodNamePart;
        String descriptorCodeLine = createDescriptorStatement(fieldIndex,
                                                              builderGenericClassName);
        ClassName rawType = ClassName.get(List.class);
        ParameterizedTypeName parameter = ParameterizedTypeName.get(rawType,
                                                                    listElementClassName);
        String fieldName = fieldDescriptor.getName();
        String addAllValues = getMessageBuilder()
                + format(".addAll%s(%s)", methodNamePart, VALUE);
        MethodSpec result = MethodSpec
                .methodBuilder(methodName)
                .addAnnotation(canIgnoreReturnValue())
                .returns(builderClassName)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(parameter, VALUE)
                .addException(ValidationException.class)
                .addStatement(descriptorCodeLine,
                              FieldDescriptor.class)
                .addStatement(createValidateStatement(VALUE),
                              fieldName)
                .addStatement(addAllValues)
                .addStatement(returnThis())
                .build();
        return result;
    }

    private MethodSpec createAddObjectMethod() {
        String methodName = ADD_PREFIX + methodNamePart;
        String descriptorCodeLine = createDescriptorStatement(fieldIndex,
                                                              builderGenericClassName);
        String addValue = format("%s.%s%s(%s)",
                                 getMessageBuilder(), ADD_PREFIX, methodNamePart, VALUE);
        MethodSpec result = MethodSpec
                .methodBuilder(methodName)
                .addAnnotation(canIgnoreReturnValue())
                .returns(builderClassName)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(listElementClassName, VALUE)
                .addException(ValidationException.class)
                .addStatement(descriptorCodeLine, FieldDescriptor.class)
                .addStatement(createValidateStatement(VALUE),
                              javaFieldName)
                .addStatement(addValue)
                .addStatement(returnThis())
                .build();
        return result;
    }

    private MethodSpec createAddObjectByIndexMethod() {
        return modifyCollectionByIndex(ADD_PREFIX);
    }

    private MethodSpec createSetObjectByIndexMethod() {
        return modifyCollectionByIndex(SET_PREFIX);
    }

    private MethodSpec createRemoveObjectByIndexMethod() {
        String methodName = removePrefix() + methodNamePart;
        String addValue = format("%s.%s%s(%s)", getMessageBuilder(),
                                 removePrefix(), methodNamePart, INDEX);
        MethodSpec result = MethodSpec
                .methodBuilder(methodName)
                .addAnnotation(canIgnoreReturnValue())
                .returns(builderClassName)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.INT, INDEX)
                .addStatement(addValue)
                .addStatement(returnThis())
                .build();
        return result;
    }

    private MethodSpec modifyCollectionByIndex(String methodPrefix) {
        String methodName = methodPrefix + methodNamePart;
        String descriptorCodeLine = createDescriptorStatement(fieldIndex,
                                                              builderGenericClassName);
        String modificationStatement = format("%s.%s%s(%s, %s)", getMessageBuilder(),
                                              methodPrefix, methodNamePart, INDEX, VALUE);
        MethodSpec result = MethodSpec
                .methodBuilder(methodName)
                .addAnnotation(canIgnoreReturnValue())
                .returns(builderClassName)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.INT, INDEX)
                .addParameter(listElementClassName, VALUE)
                .addException(ValidationException.class)
                .addStatement(descriptorCodeLine, FieldDescriptor.class)
                .addStatement(createValidateStatement(VALUE),
                              javaFieldName)
                .addStatement(modificationStatement)
                .addStatement(returnThis())
                .build();
        return result;
    }

    private MethodSpec createClearMethod() {
        String clearField = getMessageBuilder() + clearProperty(methodNamePart);
        MethodSpec result = MethodSpec
                .methodBuilder(clearPrefix() + methodNamePart)
                .addAnnotation(canIgnoreReturnValue())
                .addModifiers(Modifier.PUBLIC)
                .returns(builderClassName)
                .addStatement(clearField)
                .addStatement(returnThis())
                .build();
        return result;
    }

    private static String createGetConvertedCollectionValue() {
        String result = "final $T<$T> convertedValue = convertToList(value, $T.class)";
        return result;
    }

    /**
     * Creates a new builder for the {@code RepeatedFieldMethodConstructor} class.
     *
     * @return created builder
     */
    static RepeatedFieldMethodsConstructorBuilder newBuilder() {
        return new RepeatedFieldMethodsConstructorBuilder();
    }

    /**
     * A builder for the {@code RepeatedFieldMethodConstructor} class.
     */
    static class RepeatedFieldMethodsConstructorBuilder
            extends AbstractMethodConstructorBuilder<RepeatedFieldMethodConstructor> {

        @Override
        RepeatedFieldMethodConstructor build() {
            checkFields();
            return new RepeatedFieldMethodConstructor(this);
        }
    }
}
