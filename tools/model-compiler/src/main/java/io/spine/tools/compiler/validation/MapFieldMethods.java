/*
 * Copyright 2020, TeamDev. All rights reserved.
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
import io.spine.code.gen.java.FieldName;
import io.spine.logging.Logging;
import io.spine.tools.compiler.field.AccessorTemplates;
import io.spine.tools.compiler.field.type.MapFieldType;
import io.spine.validate.ValidationException;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.spine.tools.compiler.field.AccessorTemplates.allPutter;
import static io.spine.tools.compiler.field.AccessorTemplates.clearer;
import static io.spine.tools.compiler.field.AccessorTemplates.putter;
import static io.spine.tools.compiler.field.AccessorTemplates.remover;
import static io.spine.tools.compiler.validation.Methods.callMethod;
import static io.spine.tools.compiler.validation.Methods.getMessageBuilder;
import static io.spine.tools.compiler.validation.Methods.returnThis;
import static io.spine.tools.compiler.validation.Methods.returnValue;
import static java.lang.String.format;
import static javax.lang.model.element.Modifier.PUBLIC;

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

    /**
     * The name of the property represented by this field.
     */
    private final FieldName javaFieldName;
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
        this.javaFieldName = FieldName.from(io.spine.code.proto.FieldName.of(field.toProto()));
        this.keyTypeName = fieldType.getKeyTypeName();
        this.valueTypeName = fieldType.getValueTypeName();
    }

    @Override
    public Collection<MethodSpec> generate() {
        _debug().log("The methods construction for the map field `%s` is started.", javaFieldName);
        List<MethodSpec> methods = methods()
                .add(getter())
                .addAll(mapMethods())
                .addAll(rawMapMethods())
                .build();
        _debug().log("The methods construction for the map field `%s` is finished.", javaFieldName);
        return methods;
    }

    private MethodSpec getter() {
        _debug().log("The getter construction for the map field is started.");
        String methodName = AccessorTemplates.mapGetter()
                                             .format(javaFieldName);
        String returnStatement = returnValue(callMethod(getMessageBuilder(), methodName));
        MethodSpec methodSpec =
                MethodSpec.methodBuilder(methodName)
                          .addModifiers(PUBLIC)
                          .returns(fieldType.getTypeName())
                          .addStatement(returnStatement)
                          .build();
        _debug().log("The getter construction for the map field is finished.");
        return methodSpec;
    }

    private List<MethodSpec> rawMapMethods() {
        _debug().log("The raw methods construction for the map field is started.");
        List<MethodSpec> methods = methods(
                putRawMethod(),
                putAllRawMethod()
        );
        _debug().log("The raw methods construction for the map field is finished.");
        return methods;
    }

    private List<MethodSpec> mapMethods() {
        _debug().log("The methods construction for the map field is started.");
        List<MethodSpec> methods = methods(
                putMethod(),
                clearMethod(),
                putAllMethod(),
                removeMethod()
        );
        _debug().log("The methods construction for the map field is finished.");
        return methods;
    }

    private MethodSpec putMethod() {
        String methodName = putter().format(javaFieldName);
        String mapToValidate = MAP_TO_VALIDATE +
                "$T.singletonMap(" + KEY + ", " + VALUE + ')';
        String putStatement = callMethod(getMessageBuilder(),methodName, KEY, VALUE);
        MethodSpec result = newBuilderSetter(methodName)
                .addModifiers(PUBLIC)
                .addException(ValidationException.class)
                .addParameter(keyTypeName, KEY)
                .addParameter(valueTypeName, VALUE)
                .addStatement(descriptorDeclaration())
                .addStatement(ensureNotSetOnce())
                .addStatement(mapToValidate, Map.class, keyTypeName,
                              valueTypeName, Collections.class)
                .addStatement(validateStatement(MAP_TO_VALIDATE_PARAM_NAME, javaFieldName))
                .addStatement(putStatement)
                .addStatement(returnThis())
                .build();
        return result;
    }

    private MethodSpec putRawMethod() {
        String methodName = "putRaw" + javaFieldName;
        String mapToValidate = MAP_TO_VALIDATE +
                "$T.singletonMap(convertedKey, convertedValue)";
        String putStatement = format("%s.%s(convertedKey, convertedValue)",
                                     getMessageBuilder(), putter().format(javaFieldName));

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
                .addStatement(ensureNotSetOnce())
                .addStatement(mapToValidate, Map.class, keyTypeName,
                              valueTypeName, Collections.class)
                .addStatement(validateStatement(MAP_TO_VALIDATE_PARAM_NAME, javaFieldName))
                .addStatement(putStatement)
                .addStatement(returnThis())
                .build();
        return result;
    }

    private MethodSpec putAllMethod() {
        String putAllStatement = callMethod(getMessageBuilder(),
                                            allPutter().format(javaFieldName),
                                            MAP_PARAM_NAME);
        String methodName = fieldType.primarySetterTemplate()
                                     .format(javaFieldName);
        MethodSpec result = newBuilderSetter(methodName)
                .addParameter(fieldType.getTypeName(), MAP_PARAM_NAME)
                .addException(ValidationException.class)
                .addStatement(descriptorDeclaration())
                .addStatement(ensureNotSetOnce())
                .addStatement(validateStatement(MAP_PARAM_NAME, javaFieldName))
                .addStatement(putAllStatement)
                .addStatement(returnThis())
                .build();
        return result;
    }

    private MethodSpec putAllRawMethod() {
        String putAllStatement = format(FMT_CONVERTED_VALUE_STATEMENT,
                                        getMessageBuilder(), allPutter().format(javaFieldName));
        String methodName = fieldType.primarySetterTemplate()
                                     .format(javaFieldName);
        MethodSpec result = newBuilderSetter(methodName)
                .addParameter(String.class, MAP_PARAM_NAME)
                .addException(ValidationException.class)
                .addException(ConversionException.class)
                .addStatement(descriptorDeclaration())
                .addStatement(ensureNotSetOnce())
                .addStatement(createGetConvertedMapValue(),
                              Map.class, keyTypeName, valueTypeName,
                              keyTypeName, valueTypeName)
                .addStatement(putAllStatement)
                .addStatement(returnThis())
                .build();

        return result;
    }

    private MethodSpec removeMethod() {
        String methodName = remover().format(javaFieldName);
        String removeFromMap = callMethod(getMessageBuilder(), methodName, KEY);
        MethodSpec result = newBuilderSetter(methodName)
                .addParameter(keyTypeName, KEY)
                .addStatement(descriptorDeclaration())
                .addStatement(ensureNotSetOnce())
                .addStatement(removeFromMap)
                .addStatement(returnThis())
                .build();
        return result;
    }

    private MethodSpec clearMethod() {
        String methodName = clearer().format(javaFieldName);
        String clearMap = callMethod(getMessageBuilder(), methodName);
        MethodSpec result = newBuilderSetter(methodName)
                .addStatement(descriptorDeclaration())
                .addStatement(ensureNotSetOnce())
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
