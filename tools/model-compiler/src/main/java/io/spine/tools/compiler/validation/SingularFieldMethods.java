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

import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import io.spine.base.ConversionException;
import io.spine.code.proto.FieldName;
import io.spine.logging.Logging;
import io.spine.tools.compiler.field.type.FieldType;
import io.spine.validate.ValidationException;

import javax.lang.model.element.Modifier;
import java.util.Collection;

import static io.spine.tools.compiler.validation.Methods.clearPrefix;
import static io.spine.tools.compiler.validation.Methods.clearProperty;
import static io.spine.tools.compiler.validation.Methods.getMessageBuilder;
import static io.spine.tools.compiler.validation.Methods.rawSuffix;
import static io.spine.tools.compiler.validation.Methods.returnThis;
import static java.lang.String.format;

/**
 * A method constructor of the {@code MethodSpec} objects for the
 * validating builders generation based on the Protobuf message declaration.
 *
 * <p>Constructs the {@code MethodSpec} objects for the singular fields.
 */
class SingularFieldMethods extends AbstractMethodGroup implements Logging {

    private static final String GETTER_PREFIX = "get";
    private static final ClassName STRING_CLASS_NAME = ClassName.get(String.class);

    private final String fieldName;
    private final String methodNamePart;
    private final FieldType fieldType;
    private final TypeName fieldTypeName;
    private final FieldDescriptor field;

    /**
     * Constructs the instance by the passed builder.
     *
     * <p>The passed builder {@linkplain SingularFieldMethods.Builder#checkFields() ensures}
     * non-null values of its fields prior to calling this constructor.
     */
    @SuppressWarnings("ConstantConditions") // See Javadoc above.
    private SingularFieldMethods(Builder builder) {
        super(builder);
        this.fieldType = builder.getFieldType();
        this.field = builder.getField();
        this.fieldTypeName = fieldType.getTypeName();
        FieldName fieldName = FieldName.of(field.toProto());
        this.fieldName = fieldName.javaCase();
        this.methodNamePart = fieldName.toCamelCase();
    }

    /**
     * Returns the {@code ClassName} for the {@code String} class.
     *
     * @return the constructed {@code ClassName}
     */
    static ClassName stringClassName() {
        return STRING_CLASS_NAME;
    }

    @Override
    public Collection<MethodSpec> generate() {
        String javaFieldName = FieldName.of(field.toProto())
                                        .javaCase();
        _debug("The method construction for the {} singular field is started.", javaFieldName);
        ImmutableList.Builder<MethodSpec> methods = methods()
                .add(setter());

        if (!fieldTypeName.equals(stringClassName())) {
            methods.add(rawSetterMethod());
        }

        methods.add(getter())
               .add(clearMethod());
        _debug("The method construction for the {} singular field is finished.", javaFieldName);
        return methods.build();
    }

    private MethodSpec setter() {
        _debug("The setters construction for the singular field is started.");
        String methodName = fieldType.getSetterPrefix() + methodNamePart;
        ParameterSpec parameter = createParameterSpec(field.toProto(), false);

        String setStatement = format("%s.%s(%s)", getMessageBuilder(), methodName, fieldName);
        MethodSpec methodSpec =
                newBuilderSetter(methodName)
                          .addParameter(parameter)
                          .addException(ValidationException.class)
                          .addStatement(descriptorDeclaration())
                          .addStatement(validateSetOnce())
                          .addStatement(validateStatement(fieldName, field.getName()))
                          .addStatement(setStatement)
                          .addStatement(returnThis())
                          .build();
        _debug("The setters construction for the singular field is finished.");
        return methodSpec;
    }

    private MethodSpec getter() {
        _debug("The getter construction for the singular field is started.");
        String methodName = GETTER_PREFIX + methodNamePart;

        @SuppressWarnings("DuplicateStringLiteralInspection") MethodSpec methodSpec =
                MethodSpec.methodBuilder(methodName)
                          .addModifiers(Modifier.PUBLIC)
                          .returns(fieldTypeName)
                          .addStatement("return " + getMessageBuilder() + '.' + methodName + "()")
                          .build();
        _debug("The getter construction for the singular method is finished.");
        return methodSpec;
    }

    private MethodSpec clearMethod() {
        _debug("The 'clear..()' method construction for the singular field is started.");
        String methodBody = getMessageBuilder() + clearProperty(methodNamePart);

        String methodName = clearPrefix() + methodNamePart;
        MethodSpec methodSpec =
                newBuilderSetter(methodName)
                          .addStatement(methodBody)
                          .addStatement(returnThis())
                          .build();
        _debug("The 'clear..()' method construction for the singular method is finished.");
        return methodSpec;
    }

    private MethodSpec rawSetterMethod() {
        _debug("The raw setters construction is started.");
        String messageBuilderSetter = fieldType.getSetterPrefix() + methodNamePart;
        String methodName = messageBuilderSetter + rawSuffix();
        ParameterSpec parameter = createParameterSpec(field.toProto(), true);

        ConvertStatement convertStatement = ConvertStatement.of(fieldName, fieldTypeName);
        String convertedVariableName = convertStatement.convertedVariableName();
        String setStatement = format("%s.%s(%s)",
                                     getMessageBuilder(),
                                     messageBuilderSetter,
                                     convertedVariableName);
        MethodSpec methodSpec =
                newBuilderSetter(methodName)
                          .addParameter(parameter)
                          .addException(ValidationException.class)
                          .addException(ConversionException.class)
                          .addStatement(descriptorDeclaration())
                          .addStatement(convertStatement.value())
                          .addStatement(validateStatement(convertedVariableName, field.getName()))
                          .addStatement(setStatement)
                          .addStatement(returnThis())
                          .build();
        _debug("The raw setters construction is finished.");
        return methodSpec;
    }

    private ParameterSpec createParameterSpec(FieldDescriptorProto field, boolean raw) {
        TypeName methodParamType = raw
                                     ? stringClassName()
                                     : fieldTypeName;
        String paramName = FieldName.of(field)
                                    .javaCase();
        ParameterSpec result = ParameterSpec.builder(methodParamType, paramName)
                                            .build();
        return result;
    }

    /**
     * Creates a new builder for the {@code SingularFieldMethodConstructor} class.
     *
     * @return constructed builder
     */
    static Builder newBuilder() {
        return new Builder();
    }

    /**
     * A builder class for the {@code SingularFieldMethodConstructor} class.
     */
    static class Builder extends AbstractMethodGroupBuilder<SingularFieldMethods> {

        @Override
        SingularFieldMethods build() {
            checkFields();
            return new SingularFieldMethods(this);
        }
    }
}
