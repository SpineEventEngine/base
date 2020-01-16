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

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import io.spine.base.ConversionException;
import io.spine.code.gen.java.FieldName;
import io.spine.code.proto.FieldDeclaration;
import io.spine.logging.Logging;
import io.spine.tools.compiler.field.AccessorTemplate;
import io.spine.tools.compiler.field.AccessorTemplates;
import io.spine.tools.compiler.field.type.FieldType;
import io.spine.validate.ValidationException;

import javax.lang.model.element.Modifier;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.compiler.field.AccessorTemplates.adder;
import static io.spine.tools.compiler.field.AccessorTemplates.allAdder;
import static io.spine.tools.compiler.field.AccessorTemplates.clearer;
import static io.spine.tools.compiler.field.AccessorTemplates.listGetter;
import static io.spine.tools.compiler.field.AccessorTemplates.remover;
import static io.spine.tools.compiler.field.AccessorTemplates.setter;
import static io.spine.tools.compiler.validation.Methods.callMethod;
import static io.spine.tools.compiler.validation.Methods.getMessageBuilder;
import static io.spine.tools.compiler.validation.Methods.returnThis;
import static io.spine.tools.compiler.validation.Methods.returnValue;
import static java.lang.String.format;

/**
 * A method constructor of the {@code MethodSpec} objects based on the Protobuf message declaration.
 *
 * <p>Constructs the {@code MethodSpec} objects for the repeated fields.
 */
final class RepeatedFieldMethods extends AbstractMethodGroup implements Logging {

    @SuppressWarnings("DuplicateStringLiteralInspection")
    // It cannot be used as the constant across the project.
    // Although it has the equivalent literal they have the different meaning.
    private static final String VALUE = "value";
    @SuppressWarnings("DuplicateStringLiteralInspection")
    // It cannot be used as the constant across the project.
    // Although it has the equivalent literal they have the different meaning.
    private static final String INDEX = "index";

    private static final String CONVERTED_VALUE = "convertedValue";

    private final FieldType fieldType;
    private final FieldName javaFieldName;
    private final ClassName listElementClassName;
    private final boolean isScalarOrEnum;

    /**
     * Creates a new builder for the {@code RepeatedFieldMethodConstructor} class.
     *
     * @return created builder
     */
    static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Constructs the {@code RepeatedFieldMethodConstructor}.
     *
     * @param builder the {@code RepeatedFieldMethodConstructorBuilder} instance
     */
    private RepeatedFieldMethods(Builder builder) {
        super(builder);
        this.fieldType = checkNotNull(builder.getFieldType());
        FieldDescriptor field = checkNotNull(builder.getField());
        this.javaFieldName = FieldName.from(io.spine.code.proto.FieldName.of(field.toProto()));
        FieldDeclaration fieldDeclaration = new FieldDeclaration(field);
        String fieldJavaClass = fieldDeclaration.javaTypeName();
        String dottedForm = io.spine.code.java.ClassName.toDotted(fieldJavaClass);
        this.listElementClassName = ClassName.bestGuess(dottedForm);
        this.isScalarOrEnum = fieldDeclaration.isScalar() || fieldDeclaration.isEnum();
    }

    @Override
    public Collection<MethodSpec> generate() {
        _debug().log("The methods construction for the repeated field `%s` is started.",
                     javaFieldName);
        ImmutableList.Builder<MethodSpec> methods = methods()
                .add(getter())
                .addAll(repeatedMethods())
                .addAll(repeatedRawMethods());
        _debug().log("The methods construction for the repeated field `%s` is finished.",
                     javaFieldName);
        return methods.build();
    }

    private MethodSpec getter() {
        _debug().log("The getter construction for the repeated field is started.");

        String methodName = AccessorTemplates.getter().format(javaFieldName);
        ClassName rawType = ClassName.get(List.class);
        ParameterizedTypeName returnType = ParameterizedTypeName.get(rawType, listElementClassName);
        String returnStatement =
                returnValue(callMethod(getMessageBuilder(), listGetter().format(javaFieldName)));
        MethodSpec methodSpec = MethodSpec
                .methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(returnType)
                .addStatement(returnStatement)
                .build();

        _debug().log("The getter construction for the repeated field is finished.");
        return methodSpec;
    }

    private Collection<MethodSpec> repeatedRawMethods() {
        _debug().log("The raw methods construction for the repeated field is is started.");
        ImmutableList.Builder<MethodSpec> methods = methods()
                .add(rawAddObjectMethod())
                .add(rawSetObjectByIndexMethod())
                .add(rawAddAllMethod());
        // Some methods are not available in Protobuf Message.Builder for scalar types.
        if (!isScalarOrEnum) {
            methods.add(createRawAddObjectByIndexMethod());
        }
        _debug().log("The raw methods construction for the repeated field is is finished.");
        return methods.build();
    }

    private Collection<MethodSpec> repeatedMethods() {
        ImmutableList.Builder<MethodSpec> methods = methods()
                .add(clearMethod())
                .add(addObjectMethod())
                .add(setObjectByIndexMethod())
                .add(addAllMethod());

        // Some methods are not available in Protobuf Message.Builder for scalar types and enums.
        if (!isScalarOrEnum) {
            methods.add(addObjectByIndexMethod())
                   .add(removeObjectByIndexMethod());
        }
        return methods.build();
    }

    private MethodSpec rawAddObjectMethod() {
        String methodName = adder().toRaw().format(javaFieldName);
        String messageBuilderMethod = adder().format(javaFieldName);
        String addValueStatement = format(FMT_CONVERTED_VALUE_STATEMENT,
                                          getMessageBuilder(), messageBuilderMethod);
        MethodSpec result = newBuilderSetter(methodName)
                .addParameter(String.class, VALUE)
                .addException(ValidationException.class)
                .addException(ConversionException.class)
                .addStatement(convertStatement())
                .addStatement(descriptorDeclaration())
                .addStatement(ensureNotSetOnce())
                .addStatement(validateStatement(CONVERTED_VALUE, javaFieldName))
                .addStatement(addValueStatement)
                .addStatement(returnThis())
                .build();
        return result;
    }

    private MethodSpec createRawAddObjectByIndexMethod() {
        MethodSpec result = modifyCollectionByIndexWithRaw(adder());
        return result;
    }

    private MethodSpec rawSetObjectByIndexMethod() {
        return modifyCollectionByIndexWithRaw(setter());
    }

    private MethodSpec modifyCollectionByIndexWithRaw(AccessorTemplate methodTemplate) {
        String methodName = methodTemplate.toRaw().format(javaFieldName);
        String modificationStatement =
                format("%s.%s(%s, convertedValue)",
                       getMessageBuilder(), methodTemplate.format(javaFieldName), INDEX);
        MethodSpec result = newBuilderSetter(methodName)
                .addParameter(TypeName.INT, INDEX)
                .addParameter(String.class, VALUE)
                .addException(ValidationException.class)
                .addException(ConversionException.class)
                .addStatement(convertStatement())
                .addStatement(descriptorDeclaration())
                .addStatement(ensureNotSetOnce())
                .addStatement(validateStatement(CONVERTED_VALUE, javaFieldName))
                .addStatement(modificationStatement)
                .addStatement(returnThis())
                .build();
        return result;
    }

    /**
     * Obtains a string vale of the conversion statement.
     */
    private String convertStatement() {
        ConvertStatement statement = ConvertStatement.of(VALUE, listElementClassName);
        String value = statement.value();
        return value;
    }

    private MethodSpec rawAddAllMethod() {
        String methodName = fieldType.primarySetterTemplate()
                                     .toRaw()
                                     .format(javaFieldName);
        String addAllValues = addAllStatement(CONVERTED_VALUE);
        MethodSpec result = newBuilderSetter(methodName)
                .addParameter(String.class, VALUE)
                .addException(ValidationException.class)
                .addException(ConversionException.class)
                .addStatement(createGetConvertedCollectionValue(),
                              List.class,
                              listElementClassName,
                              listElementClassName)
                .addStatement(descriptorDeclaration())
                .addStatement(ensureNotSetOnce())
                .addStatement(validateStatement(CONVERTED_VALUE, javaFieldName))
                .addStatement(addAllValues)
                .addStatement(returnThis())
                .build();
        return result;
    }

    private MethodSpec addAllMethod() {
        String methodName = fieldType.primarySetterTemplate().format(javaFieldName);
        ClassName rawType = ClassName.get(List.class);
        ParameterizedTypeName parameter = ParameterizedTypeName.get(rawType, listElementClassName);
        String addAllValues = addAllStatement(VALUE);
        MethodSpec result = newBuilderSetter(methodName)
                .addParameter(parameter, VALUE)
                .addException(ValidationException.class)
                .addStatement(descriptorDeclaration())
                .addStatement(ensureNotSetOnce())
                .addStatement(validateStatement(VALUE, javaFieldName))
                .addStatement(addAllValues)
                .addStatement(returnThis())
                .build();
        return result;
    }

    private String addAllStatement(String parameter) {
        String addAllMethodName = allAdder().format(javaFieldName);
        String addAllValues = callMethod(getMessageBuilder(), addAllMethodName, parameter);
        return addAllValues;
    }

    private MethodSpec addObjectMethod() {
        String methodName = adder().format(javaFieldName);
        String addValue = callMethod(getMessageBuilder(), methodName, VALUE);
        String descriptorDeclaration = descriptorDeclaration();
        MethodSpec result = newBuilderSetter(methodName)
                .addParameter(listElementClassName, VALUE)
                .addException(ValidationException.class)
                .addStatement(descriptorDeclaration)
                .addStatement(ensureNotSetOnce())
                .addStatement(validateStatement(VALUE, javaFieldName))
                .addStatement(addValue)
                .addStatement(returnThis())
                .build();
        return result;
    }

    private MethodSpec addObjectByIndexMethod() {
        return modifyCollectionByIndex(adder());
    }

    private MethodSpec setObjectByIndexMethod() {
        return modifyCollectionByIndex(setter());
    }

    private MethodSpec removeObjectByIndexMethod() {
        String methodName = remover().format(javaFieldName);
        String addValue = callMethod(getMessageBuilder(), methodName, INDEX);
        MethodSpec result = newBuilderSetter(methodName)
                .addParameter(TypeName.INT, INDEX)
                .addStatement(descriptorDeclaration())
                .addStatement(ensureNotSetOnce())
                .addStatement(addValue)
                .addStatement(returnThis())
                .build();
        return result;
    }

    private MethodSpec modifyCollectionByIndex(AccessorTemplate template) {
        String methodName = template.format(javaFieldName);
        String modificationStatement = callMethod(getMessageBuilder(), methodName, INDEX, VALUE);
        MethodSpec result = newBuilderSetter(methodName)
                .addParameter(TypeName.INT, INDEX)
                .addParameter(listElementClassName, VALUE)
                .addException(ValidationException.class)
                .addStatement(descriptorDeclaration())
                .addStatement(ensureNotSetOnce())
                .addStatement(validateStatement(VALUE, javaFieldName))
                .addStatement(modificationStatement)
                .addStatement(returnThis())
                .build();
        return result;
    }

    private MethodSpec clearMethod() {
        String methodName = clearer().format(javaFieldName);
        String clearField = callMethod(getMessageBuilder(), clearer().format(javaFieldName));
        MethodSpec result = newBuilderSetter(methodName)
                .addStatement(descriptorDeclaration())
                .addStatement(ensureNotSetOnce())
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
     * A builder for the {@code RepeatedFieldMethodConstructor} class.
     */
    static class Builder extends AbstractMethodGroupBuilder<RepeatedFieldMethods> {

        @Override
        RepeatedFieldMethods build() {
            checkFields();
            return new RepeatedFieldMethods(this);
        }
    }
}
