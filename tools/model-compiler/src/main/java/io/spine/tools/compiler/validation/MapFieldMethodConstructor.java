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
import com.squareup.javapoet.TypeName;
import io.spine.base.ConversionException;
import io.spine.code.proto.FieldName;
import io.spine.logging.Logging;
import io.spine.tools.compiler.fieldtype.MapFieldType;
import io.spine.validate.ValidationException;

import javax.lang.model.element.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static io.spine.tools.compiler.annotation.Annotations.canIgnoreReturnValue;
import static io.spine.tools.compiler.validation.MethodConstructors.clearPrefix;
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
 * <p>Constructs the {@code MethodSpec} objects for the map fields.
 *
 * @author Illia Shepilov
 */
class MapFieldMethodConstructor implements MethodConstructor, Logging {

    private static final String KEY = "key";
    private static final String VALUE = "value";
    private static final String MAP_PARAM_NAME = "map";
    private static final String MAP_TO_VALIDATE_PARAM_NAME = "mapToValidate";
    private static final String MAP_TO_VALIDATE = "final $T<$T, $T> mapToValidate = ";

    private final int fieldIndex;
    private final String javaFieldName;

    /**
     * The name of the property represented by this field.
     *
     * <p>Effectively equal to {@linkplain #javaFieldName Java field name} with the first letter in
     * upper case.
     */
    private final String propertyName;
    private final TypeName keyTypeName;
    private final TypeName valueTypeName;
    private final MapFieldType fieldType;
    private final ClassName genericClassName;
    private final ClassName builderClassName;

    /**
     * Creates the {@code MapFieldMethodConstructor}.
     *
     * @param builder the {MapFieldMethodsConstructorBuilder} instance
     */
    @SuppressWarnings("ConstantConditions")
    // The fields are checked in the {@code #build()} method
    // of the {@code MapFieldMethodConstructorBuilder} class.
    private MapFieldMethodConstructor(MapFieldMethodsConstructorBuilder builder) {
        super();
        this.fieldType = (MapFieldType) builder.getFieldType();
        this.fieldIndex = builder.getFieldIndex();
        FieldDescriptorProto fieldDescriptor = builder.getField();
        this.genericClassName = builder.getGenericClassName();
        FieldName fieldName = FieldName.of(fieldDescriptor);
        this.propertyName = fieldName.toCamelCase();
        this.javaFieldName = fieldName.javaCase();
        String javaClass = builder.getJavaClass();
        String javaPackage = builder.getJavaPackage();
        this.builderClassName = ClassNames.getClassName(javaPackage, javaClass);
        this.keyTypeName = fieldType.getKeyTypeName();
        this.valueTypeName = fieldType.getValueTypeName();
    }

    @Override
    public Collection<MethodSpec> construct() {
        log().debug("The methods construction for the map field {} is started.", javaFieldName);
        List<MethodSpec> methods = newArrayList();
        methods.add(createGetter());
        methods.addAll(createMapMethods());
        methods.addAll(createRawMapMethods());
        log().debug("The methods construction for the map field {} is finished.", javaFieldName);
        return methods;
    }

    private MethodSpec createGetter() {
        log().debug("The getter construction for the map field is started.");
        String methodName = "get" + propertyName;

        String returnStatement = format("return %s.get%sMap()",
                                        getMessageBuilder(), propertyName);
        MethodSpec methodSpec =
                MethodSpec.methodBuilder(methodName)
                          .addModifiers(Modifier.PUBLIC)
                          .returns(fieldType.getTypeName())
                          .addStatement(returnStatement)
                          .build();
        log().debug("The getter construction for the map field is finished.");
        return methodSpec;
    }

    private List<MethodSpec> createRawMapMethods() {
        log().debug("The raw methods construction for the map field is started.");
        List<MethodSpec> methods = newArrayList();
        methods.add(createPutRawMethod());
        methods.add(createPutAllRawMethod());
        log().debug("The raw methods construction for the map field is finished.");
        return methods;
    }

    private List<MethodSpec> createMapMethods() {
        log().debug("The methods construction for the map field is started.");
        List<MethodSpec> methods = newArrayList();
        methods.add(createPutMethod());
        methods.add(createClearMethod());
        methods.add(createPutAllMethod());
        methods.add(createRemoveMethod());
        log().debug("The methods construction for the map field is finished.");
        return methods;
    }

    private MethodSpec createPutMethod() {
        String methodName = "put" + propertyName;
        String descriptorCodeLine = createDescriptorStatement(fieldIndex, genericClassName);
        String mapToValidate = MAP_TO_VALIDATE +
                "$T.singletonMap(" + KEY + ", " + VALUE + ')';
        String putStatement = format("%s.put%s(%s, %s)",
                                     getMessageBuilder(), propertyName, KEY, VALUE);
        MethodSpec result = MethodSpec
                .methodBuilder(methodName)
                .addAnnotation(canIgnoreReturnValue())
                .returns(builderClassName)
                .addModifiers(Modifier.PUBLIC)
                .addException(ValidationException.class)
                .addParameter(keyTypeName, KEY)
                .addParameter(valueTypeName, VALUE)
                .addStatement(descriptorCodeLine, FieldDescriptor.class)
                .addStatement(mapToValidate, Map.class, keyTypeName,
                              valueTypeName, Collections.class)
                .addStatement(createValidateStatement(MAP_TO_VALIDATE_PARAM_NAME),
                              javaFieldName)
                .addStatement(putStatement)
                .addStatement(returnThis())
                .build();
        return result;
    }

    private MethodSpec createPutRawMethod() {
        String methodName = "putRaw" + propertyName;
        String descriptorCodeLine = createDescriptorStatement(fieldIndex, genericClassName);
        String mapToValidate = MAP_TO_VALIDATE +
                "$T.singletonMap(convertedKey, convertedValue)";
        String putStatement = format("%s.put%s(convertedKey, convertedValue)",
                                     getMessageBuilder(), propertyName);

        MethodSpec result = MethodSpec
                .methodBuilder(methodName)
                .addAnnotation(canIgnoreReturnValue())
                .returns(builderClassName)
                .addModifiers(Modifier.PUBLIC)
                .addException(ValidationException.class)
                .addException(ConversionException.class)
                .addParameter(String.class, KEY)
                .addParameter(String.class, VALUE)
                .addStatement(createConvertSingularValue(KEY),
                              keyTypeName, keyTypeName)
                .addStatement(createConvertSingularValue(VALUE),
                              valueTypeName, valueTypeName)
                .addStatement(descriptorCodeLine, FieldDescriptor.class)
                .addStatement(mapToValidate, Map.class, keyTypeName,
                              valueTypeName, Collections.class)
                .addStatement(createValidateStatement(MAP_TO_VALIDATE_PARAM_NAME),
                              javaFieldName)
                .addStatement(putStatement)
                .addStatement(returnThis())
                .build();
        return result;
    }

    private MethodSpec createPutAllMethod() {
        String descriptorCodeLine = createDescriptorStatement(fieldIndex, genericClassName);
        String putAllStatement = format("%s.putAll%s(%s)",
                                        getMessageBuilder(), propertyName, MAP_PARAM_NAME);
        String methodName = fieldType.getSetterPrefix() + propertyName;
        MethodSpec result = MethodSpec
                .methodBuilder(methodName)
                .addAnnotation(canIgnoreReturnValue())
                .addModifiers(Modifier.PUBLIC)
                .returns(builderClassName)
                .addParameter(fieldType.getTypeName(), MAP_PARAM_NAME)
                .addException(ValidationException.class)
                .addStatement(descriptorCodeLine, FieldDescriptor.class)
                .addStatement(createValidateStatement(MAP_PARAM_NAME),
                              javaFieldName)
                .addStatement(putAllStatement)
                .addStatement(returnThis())
                .build();
        return result;
    }

    private MethodSpec createPutAllRawMethod() {
        String descriptorCodeLine = createDescriptorStatement(fieldIndex, genericClassName);
        String putAllStatement = format("%s.putAll%s(convertedValue)",
                                        getMessageBuilder(), propertyName);
        String methodName = fieldType.getSetterPrefix() + rawSuffix() + propertyName;
        MethodSpec result = MethodSpec
                .methodBuilder(methodName)
                .addAnnotation(canIgnoreReturnValue())
                .addModifiers(Modifier.PUBLIC)
                .returns(builderClassName)
                .addParameter(String.class, MAP_PARAM_NAME)
                .addException(ValidationException.class)
                .addException(ConversionException.class)
                .addStatement(descriptorCodeLine, FieldDescriptor.class)
                .addStatement(createGetConvertedMapValue(),
                              Map.class, keyTypeName, valueTypeName,
                              keyTypeName, valueTypeName)
                .addStatement(putAllStatement)
                .addStatement(returnThis())
                .build();

        return result;
    }

    private MethodSpec createRemoveMethod() {
        String removeFromMap = format("%s.remove%s(%s)",
                                      getMessageBuilder(), propertyName, KEY);
        MethodSpec result = MethodSpec
                .methodBuilder(removePrefix() + propertyName)
                .addAnnotation(canIgnoreReturnValue())
                .addModifiers(Modifier.PUBLIC)
                .returns(builderClassName)
                .addParameter(keyTypeName, KEY)
                .addStatement(removeFromMap)
                .addStatement(returnThis())
                .build();
        return result;
    }

    private MethodSpec createClearMethod() {
        String clearMap = format("%s.clear%s()", getMessageBuilder(), propertyName);
        MethodSpec result = MethodSpec
                .methodBuilder(clearPrefix() + propertyName)
                .addAnnotation(canIgnoreReturnValue())
                .addModifiers(Modifier.PUBLIC)
                .returns(builderClassName)
                .addStatement(clearMap)
                .addStatement(returnThis())
                .build();
        return result;
    }

    private static String createGetConvertedMapValue() {
        String result = "final $T<$T, $T> convertedValue = " +
                "convertToMap(map, $T.class, $T.class)";
        return result;
    }

    /**
     * Creates a new builder for the {@code MapFieldMethodsConstructor} class.
     *
     * @return created builder
     */
    static MapFieldMethodsConstructorBuilder newBuilder() {
        return new MapFieldMethodsConstructorBuilder();
    }

    /**
     * A builder for the {@code MapFieldMethodsConstructor} class.
     */
    static class MapFieldMethodsConstructorBuilder
            extends AbstractMethodConstructorBuilder<MapFieldMethodConstructor> {
        @Override
        MapFieldMethodConstructor build() {
            checkFields();
            return new MapFieldMethodConstructor(this);
        }
    }
}
