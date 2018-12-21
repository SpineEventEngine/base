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

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import io.spine.base.ConversionException;
import io.spine.code.proto.FieldName;
import io.spine.logging.Logging;
import io.spine.tools.compiler.field.type.MapFieldType;
import io.spine.validate.ValidationException;

import javax.lang.model.element.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.spine.tools.compiler.validation.Methods.clearPrefix;
import static io.spine.tools.compiler.validation.Methods.getMessageBuilder;
import static io.spine.tools.compiler.validation.Methods.rawSuffix;
import static io.spine.tools.compiler.validation.Methods.removePrefix;
import static io.spine.tools.compiler.validation.Methods.returnThis;
import static java.lang.String.format;

/**
 * A method constructor of the {@code MethodSpec} objects based on the Protobuf message declaration.
 *
 * <p>Constructs the {@code MethodSpec} objects for the map fields.
 */
class MapFieldMethods extends AbstractMethodGroup implements Logging {

    private static final String KEY = "key";
    @SuppressWarnings("DuplicateStringLiteralInspection") // specific semantic
    private static final String VALUE = "value";
    private static final String MAP_PARAM_NAME = "map";
    private static final String MAP_TO_VALIDATE_PARAM_NAME = "mapToValidate";
    private static final String MAP_TO_VALIDATE = "final $T<$T, $T> mapToValidate = ";

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

    /**
     * Creates the {@code MapFieldMethodConstructor}.
     *
     * @param builder the {MapFieldMethodsConstructorBuilder} instance
     */
    @SuppressWarnings("ConstantConditions")
    // The fields are checked in the {@code #build()} method
    // of the {@code MapFieldMethodConstructorBuilder} class.
    private MapFieldMethods(Builder builder) {
        super(builder);
        this.fieldType = (MapFieldType) builder.getFieldType();
        FieldDescriptor field = builder.getField();
        FieldName fieldName = FieldName.of(field.toProto());
        this.propertyName = fieldName.toCamelCase();
        this.javaFieldName = fieldName.javaCase();
        this.keyTypeName = fieldType.getKeyTypeName();
        this.valueTypeName = fieldType.getValueTypeName();
    }

    @Override
    public Collection<MethodSpec> generate() {
        _debug("The methods construction for the map field {} is started.", javaFieldName);
        List<MethodSpec> methods = methods()
                .add(getter())
                .addAll(mapMethods())
                .addAll(rawMapMethods())
                .build();
        _debug("The methods construction for the map field {} is finished.", javaFieldName);
        return methods;
    }

    private MethodSpec getter() {
        _debug("The getter construction for the map field is started.");
        String methodName = "get" + propertyName;

        String returnStatement = format("return %s.get%sMap()",
                                        getMessageBuilder(), propertyName);
        MethodSpec methodSpec =
                MethodSpec.methodBuilder(methodName)
                          .addModifiers(Modifier.PUBLIC)
                          .returns(fieldType.getTypeName())
                          .addStatement(returnStatement)
                          .build();
        _debug("The getter construction for the map field is finished.");
        return methodSpec;
    }

    private List<MethodSpec> rawMapMethods() {
        _debug("The raw methods construction for the map field is started.");
        List<MethodSpec> methods = methods(
                putRawMethod(),
                putAllRawMethod()
        );
        _debug("The raw methods construction for the map field is finished.");
        return methods;
    }

    private List<MethodSpec> mapMethods() {
        _debug("The methods construction for the map field is started.");
        List<MethodSpec> methods = methods(
                putMethod(),
                clearMethod(),
                putAllMethod(),
                removeMethod()
        );
        _debug("The methods construction for the map field is finished.");
        return methods;
    }

    private MethodSpec putMethod() {
        String methodName = "put" + propertyName;
        String mapToValidate = MAP_TO_VALIDATE +
                "$T.singletonMap(" + KEY + ", " + VALUE + ')';
        String putStatement = format("%s.put%s(%s, %s)",
                                     getMessageBuilder(), propertyName, KEY, VALUE);
        MethodSpec result = newBuilderSetter(methodName)
                .addModifiers(Modifier.PUBLIC)
                .addException(ValidationException.class)
                .addParameter(keyTypeName, KEY)
                .addParameter(valueTypeName, VALUE)
                .addStatement(descriptorDeclaration())
                .addStatement(mapToValidate, Map.class, keyTypeName,
                              valueTypeName, Collections.class)
                .addStatement(validateStatement(MAP_TO_VALIDATE_PARAM_NAME, javaFieldName))
                .addStatement(putStatement)
                .addStatement(returnThis())
                .build();
        return result;
    }

    private MethodSpec putRawMethod() {
        String methodName = "putRaw" + propertyName;
        String mapToValidate = MAP_TO_VALIDATE +
                "$T.singletonMap(convertedKey, convertedValue)";
        String putStatement = format("%s.put%s(convertedKey, convertedValue)",
                                     getMessageBuilder(), propertyName);

        MethodSpec result = newBuilderSetter(methodName)
                .addException(ValidationException.class)
                .addException(ConversionException.class)
                .addParameter(String.class, KEY)
                .addParameter(String.class, VALUE)
                .addStatement(ConvertStatement.of(KEY, keyTypeName)
                                              .value())
                .addStatement(ConvertStatement.of(VALUE, valueTypeName)
                                              .value())
                .addStatement(descriptorDeclaration())
                .addStatement(mapToValidate, Map.class, keyTypeName,
                              valueTypeName, Collections.class)
                .addStatement(validateStatement(MAP_TO_VALIDATE_PARAM_NAME, javaFieldName))
                .addStatement(putStatement)
                .addStatement(returnThis())
                .build();
        return result;
    }

    private MethodSpec putAllMethod() {
        String putAllStatement = format("%s.putAll%s(%s)",
                                        getMessageBuilder(), propertyName, MAP_PARAM_NAME);
        String methodName = fieldType.getSetterPrefix() + propertyName;
        MethodSpec result = newBuilderSetter(methodName)
                .addParameter(fieldType.getTypeName(), MAP_PARAM_NAME)
                .addException(ValidationException.class)
                .addStatement(descriptorDeclaration())
                .addStatement(validateStatement(MAP_PARAM_NAME, javaFieldName))
                .addStatement(putAllStatement)
                .addStatement(returnThis())
                .build();
        return result;
    }

    private MethodSpec putAllRawMethod() {
        String putAllStatement = format("%s.putAll%s(convertedValue)",
                                        getMessageBuilder(), propertyName);
        String methodName = fieldType.getSetterPrefix() + rawSuffix() + propertyName;
        MethodSpec result = newBuilderSetter(methodName)
                .addParameter(String.class, MAP_PARAM_NAME)
                .addException(ValidationException.class)
                .addException(ConversionException.class)
                .addStatement(descriptorDeclaration())
                .addStatement(createGetConvertedMapValue(),
                              Map.class, keyTypeName, valueTypeName,
                              keyTypeName, valueTypeName)
                .addStatement(putAllStatement)
                .addStatement(returnThis())
                .build();

        return result;
    }

    private MethodSpec removeMethod() {
        String removeFromMap = format("%s.remove%s(%s)",
                                      getMessageBuilder(), propertyName, KEY);
        String methodName = removePrefix() + propertyName;
        MethodSpec result = newBuilderSetter(methodName)
                .addParameter(keyTypeName, KEY)
                .addStatement(removeFromMap)
                .addStatement(returnThis())
                .build();
        return result;
    }

    private MethodSpec clearMethod() {
        String clearMap = format("%s.clear%s()", getMessageBuilder(), propertyName);
        String methodName = clearPrefix() + propertyName;
        MethodSpec result = newBuilderSetter(methodName)
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
    static Builder newBuilder() {
        return new Builder();
    }

    /**
     * A builder for the {@code MapFieldMethodsConstructor} class.
     */
    static class Builder extends AbstractMethodGroupBuilder<MapFieldMethods> {
        @Override
        MapFieldMethods build() {
            checkFields();
            return new MapFieldMethods(this);
        }
    }
}
