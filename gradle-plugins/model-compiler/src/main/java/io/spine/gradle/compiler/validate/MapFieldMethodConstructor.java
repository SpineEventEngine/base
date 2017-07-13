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
import com.squareup.javapoet.TypeName;
import io.spine.base.ConversionException;
import io.spine.base.Types;
import io.spine.gradle.compiler.message.fieldtype.MapFieldType;
import io.spine.validate.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static io.spine.gradle.compiler.util.JavaCode.toJavaFieldName;
import static io.spine.gradle.compiler.validate.MethodConstructors.clearPrefix;
import static io.spine.gradle.compiler.validate.MethodConstructors.createConvertSingularValue;
import static io.spine.gradle.compiler.validate.MethodConstructors.createDescriptorStatement;
import static io.spine.gradle.compiler.validate.MethodConstructors.createValidateStatement;
import static io.spine.gradle.compiler.validate.MethodConstructors.getMessageBuilder;
import static io.spine.gradle.compiler.validate.MethodConstructors.rawSuffix;
import static io.spine.gradle.compiler.validate.MethodConstructors.removePrefix;
import static io.spine.gradle.compiler.validate.MethodConstructors.returnThis;
import static java.lang.String.format;

/**
 * A method constructor of the {@code MethodSpec} objects based on the Protobuf message declaration.
 *
 * <p>Constructs the {@code MethodSpec} objects for the map fields.
 *
 * @author Illia Shepilov
 */
class MapFieldMethodConstructor implements MethodConstructor {

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
        final FieldDescriptorProto fieldDescriptor = builder.getFieldDescriptor();
        this.genericClassName = builder.getGenericClassName();
        this.propertyName = toJavaFieldName(fieldDescriptor.getName(), true);
        this.javaFieldName = toJavaFieldName(fieldDescriptor.getName(), false);
        final String javaClass = builder.getJavaClass();
        final String javaPackage = builder.getJavaPackage();
        this.builderClassName = ClassNames.getClassName(javaPackage, javaClass);
        this.keyTypeName = fieldType.getKeyTypeName();
        this.valueTypeName = fieldType.getValueTypeName();
    }

    @Override
    public Collection<MethodSpec> construct() {
        log().trace("The methods construction for the map field {} is started.", javaFieldName);
        final List<MethodSpec> methods = newArrayList();
        methods.add(createGetter());
        methods.addAll(createMapMethods());
        methods.addAll(createRawMapMethods());
        log().trace("The methods construction for the map field {} is finished.", javaFieldName);
        return methods;
    }

    private MethodSpec createGetter() {
        log().trace("The getter construction for the map field is started.");
        final String methodName = "get" + propertyName;

        final String returnStatement = format("return %s.get%s()",
                                              getMessageBuilder(), propertyName);
        final MethodSpec methodSpec =
                MethodSpec.methodBuilder(methodName)
                          .addModifiers(Modifier.PUBLIC)
                          .returns(fieldType.getTypeName())
                          .addStatement(returnStatement)
                          .build();
        log().trace("The getter construction for the map field is finished.");
        return methodSpec;
    }


    private List<MethodSpec> createRawMapMethods() {
        log().trace("The raw methods construction for the map field is started.");
        final List<MethodSpec> methods = newArrayList();
        methods.add(createPutRawMethod());
        methods.add(createPutAllRawMethod());
        log().trace("The raw methods construction for the map field is finished.");
        return methods;
    }

    private List<MethodSpec> createMapMethods() {
        log().trace("The methods construction for the map field is started.");
        final List<MethodSpec> methods = newArrayList();
        methods.add(createPutMethod());
        methods.add(createClearMethod());
        methods.add(createPutAllMethod());
        methods.add(createRemoveMethod());
        log().trace("The methods construction for the map field is finished.");
        return methods;
    }

    private MethodSpec createPutMethod() {
        final String methodName = toJavaFieldName("put" + propertyName, false);
        final String descriptorCodeLine = createDescriptorStatement(fieldIndex, genericClassName);
        final String mapToValidate = MAP_TO_VALIDATE +
                "$T.singletonMap(" + KEY + ", " + VALUE + ')';
        final String putStatement = format("%s.put%s(%s, %s)",
                                           getMessageBuilder(), propertyName, KEY, VALUE);
        final MethodSpec result =
                MethodSpec.methodBuilder(methodName)
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
        final String methodName = toJavaFieldName("putRaw" + propertyName, false);
        final String descriptorCodeLine = createDescriptorStatement(fieldIndex, genericClassName);
        final String mapToValidate = MAP_TO_VALIDATE +
                "$T.singletonMap(convertedKey, convertedValue)";
        final String putStatement = format("%s.put%s(convertedKey, convertedValue)",
                                           getMessageBuilder(), propertyName);

        final MethodSpec result =
                MethodSpec.methodBuilder(methodName)
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
        final String descriptorCodeLine = createDescriptorStatement(fieldIndex, genericClassName);
        final String putAllStatement = format("%s.putAll%s(%s)",
                                              getMessageBuilder(), propertyName, MAP_PARAM_NAME);
        final String methodName = fieldType.getSetterPrefix() + propertyName;
        final MethodSpec result = MethodSpec.methodBuilder(methodName)
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
        final String descriptorCodeLine = createDescriptorStatement(fieldIndex, genericClassName);
        final String putAllStatement = format("%s.putAll%s(convertedValue)",
                                              getMessageBuilder(), propertyName);
        final String methodName = fieldType.getSetterPrefix() + rawSuffix() + propertyName;
        final MethodSpec result = MethodSpec.methodBuilder(methodName)
                                            .addModifiers(Modifier.PUBLIC)
                                            .returns(builderClassName)
                                            .addParameter(String.class, MAP_PARAM_NAME)
                                            .addException(ValidationException.class)
                                            .addException(ConversionException.class)
                                            .addStatement(descriptorCodeLine, FieldDescriptor.class)
                                            .addStatement(createGetConvertedMapValue(),
                                                          Map.class, keyTypeName,
                                                          valueTypeName, Types.class,
                                                          keyTypeName, valueTypeName)
                                            .addStatement(putAllStatement)
                                            .addStatement(returnThis())
                                            .build();
        return result;
    }

    private MethodSpec createRemoveMethod() {
        final String removeFromMap = format("%s.remove%s(%s)",
                                            getMessageBuilder(), propertyName, KEY);
        final MethodSpec result = MethodSpec.methodBuilder(removePrefix() + propertyName)
                                            .addModifiers(Modifier.PUBLIC)
                                            .returns(builderClassName)
                                            .addParameter(keyTypeName, KEY)
                                            .addStatement(removeFromMap)
                                            .addStatement(returnThis())
                                            .build();
        return result;
    }

    private MethodSpec createClearMethod() {
        final String clearMap = format("%s.clear%s()", getMessageBuilder(), propertyName);
        final MethodSpec result = MethodSpec.methodBuilder(clearPrefix() + propertyName)
                                            .addModifiers(Modifier.PUBLIC)
                                            .returns(builderClassName)
                                            .addStatement(clearMap)
                                            .addStatement(returnThis())
                                            .build();
        return result;
    }

    private static String createGetConvertedMapValue() {
        final String result = "final $T<$T, $T> convertedValue = " +
                "convert(map, $T.mapTypeOf($T.class, $T.class))";
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
            super.build();
            return new MapFieldMethodConstructor(this);
        }
    }

    private enum LogSingleton {
        INSTANCE;

        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(MapFieldMethodConstructor.class);
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }
}
