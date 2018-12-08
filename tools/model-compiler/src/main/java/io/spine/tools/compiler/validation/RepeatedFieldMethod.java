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
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import io.spine.base.ConversionException;
import io.spine.code.proto.FieldName;
import io.spine.code.proto.ScalarType;
import io.spine.logging.Logging;
import io.spine.tools.compiler.TypeCache;
import io.spine.tools.compiler.field.type.FieldType;
import io.spine.validate.ValidationException;

import javax.lang.model.element.Modifier;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM;
import static io.spine.tools.compiler.validation.ClassNames.getParameterClassName;
import static io.spine.tools.compiler.validation.MethodConstructors.clearPrefix;
import static io.spine.tools.compiler.validation.MethodConstructors.clearProperty;
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
class RepeatedFieldMethod extends AbstractMethod implements Logging {

    private static final String VALUE = "value";
    private static final String INDEX = "index";
    private static final String ADD_PREFIX = "add";
    private static final String SET_PREFIX = "set";
    private static final String ADD_RAW_PREFIX = "addRaw";
    private static final String SET_RAW_PREFIX = "setRaw";
    private static final String CONVERTED_VALUE = "convertedValue";

    private final FieldType fieldType;
    private final String javaFieldName;
    private final String methodNamePart;
    private final ClassName listElementClassName;
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
    private RepeatedFieldMethod(RepeatedFieldMethodsBuilder builder) {
        super(builder);
        this.fieldType = builder.getFieldType();
        this.fieldDescriptor = builder.getField();
        FieldName fieldName = FieldName.of(fieldDescriptor);
        this.javaFieldName = fieldName.javaCase();
        this.methodNamePart = fieldName.toCamelCase();
        TypeCache typeCache = builder.getTypeCache();
        this.listElementClassName = getParameterClassName(fieldDescriptor, typeCache);
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
        String addValueStatement = getMessageBuilder() + '.'
                + ADD_PREFIX + methodNamePart + "(convertedValue)";
        MethodSpec result = newBuilderSetter(methodName)
                .addParameter(String.class, VALUE)
                .addException(ValidationException.class)
                .addException(ConversionException.class)
                .addStatement(ConvertStatement.of(VALUE, listElementClassName)
                                              .value())
                .addStatement(descriptorDeclaration())
                .addStatement(validateStatement(CONVERTED_VALUE, fieldDescriptor.getName()))
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
        String modificationStatement =
                format("%s.%s%s(%s, convertedValue)",
                       getMessageBuilder(), realBuilderCallPrefix, methodNamePart, INDEX);
        MethodSpec result = newBuilderSetter(methodName)
                .addParameter(TypeName.INT, INDEX)
                .addParameter(String.class, VALUE)
                .addException(ValidationException.class)
                .addException(ConversionException.class)
                .addStatement(ConvertStatement.of(VALUE, listElementClassName)
                                              .value())
                .addStatement(descriptorDeclaration())
                .addStatement(validateStatement(CONVERTED_VALUE, fieldDescriptor.getName()))
                .addStatement(modificationStatement)
                .addStatement(returnThis())
                .build();
        return result;
    }

    private MethodSpec createRawAddAllMethod() {
        String methodName = fieldType.getSetterPrefix() + rawSuffix() + methodNamePart;
        String addAllValues = getMessageBuilder()
                + format(".addAll%s(%s)", methodNamePart, CONVERTED_VALUE);
        MethodSpec result = newBuilderSetter(methodName)
                .addParameter(String.class, VALUE)
                .addException(ValidationException.class)
                .addException(ConversionException.class)
                .addStatement(createGetConvertedCollectionValue(),
                              List.class,
                              listElementClassName,
                              listElementClassName)
                .addStatement(descriptorDeclaration())
                .addStatement(validateStatement(CONVERTED_VALUE, fieldDescriptor.getName()))
                .addStatement(addAllValues)
                .addStatement(returnThis())
                .build();
        return result;
    }

    private MethodSpec createAddAllMethod() {
        String methodName = fieldType.getSetterPrefix() + methodNamePart;
        ClassName rawType = ClassName.get(List.class);
        ParameterizedTypeName parameter = ParameterizedTypeName.get(rawType,
                                                                    listElementClassName);
        String fieldName = fieldDescriptor.getName();
        String addAllValues = getMessageBuilder()
                + format(".addAll%s(%s)", methodNamePart, VALUE);
        MethodSpec result = newBuilderSetter(methodName)
                .addParameter(parameter, VALUE)
                .addException(ValidationException.class)
                .addStatement(descriptorDeclaration())
                .addStatement(validateStatement(VALUE, fieldName))
                .addStatement(addAllValues)
                .addStatement(returnThis())
                .build();
        return result;
    }

    private MethodSpec createAddObjectMethod() {
        String methodName = ADD_PREFIX + methodNamePart;
        String addValue = format("%s.%s%s(%s)",
                                 getMessageBuilder(), ADD_PREFIX, methodNamePart, VALUE);
        MethodSpec result = newBuilderSetter(methodName)
                .addParameter(listElementClassName, VALUE)
                .addException(ValidationException.class)
                .addStatement(descriptorDeclaration())
                .addStatement(validateStatement(VALUE, javaFieldName))
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
        MethodSpec result = newBuilderSetter(methodName)
                .addParameter(TypeName.INT, INDEX)
                .addStatement(addValue)
                .addStatement(returnThis())
                .build();
        return result;
    }

    private MethodSpec modifyCollectionByIndex(String methodPrefix) {
        String methodName = methodPrefix + methodNamePart;
        String modificationStatement = format("%s.%s%s(%s, %s)", getMessageBuilder(),
                                              methodPrefix, methodNamePart, INDEX, VALUE);
        MethodSpec result = newBuilderSetter(methodName)
                .addParameter(TypeName.INT, INDEX)
                .addParameter(listElementClassName, VALUE)
                .addException(ValidationException.class)
                .addStatement(descriptorDeclaration())
                .addStatement(validateStatement(VALUE, javaFieldName))
                .addStatement(modificationStatement)
                .addStatement(returnThis())
                .build();
        return result;
    }

    private MethodSpec createClearMethod() {
        String clearField = getMessageBuilder() + clearProperty(methodNamePart);
        String methodName = clearPrefix() + methodNamePart;
        MethodSpec result = newBuilderSetter(methodName)
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
    static RepeatedFieldMethodsBuilder newBuilder() {
        return new RepeatedFieldMethodsBuilder();
    }

    /**
     * A builder for the {@code RepeatedFieldMethodConstructor} class.
     */
    static class RepeatedFieldMethodsBuilder
            extends AbstractMethodBuilder<RepeatedFieldMethod> {

        @Override
        RepeatedFieldMethod build() {
            checkFields();
            return new RepeatedFieldMethod(this);
        }
    }
}
