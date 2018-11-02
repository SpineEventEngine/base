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
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;

import javax.lang.model.element.Modifier;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.compiler.annotation.Annotations.canIgnoreReturnValue;
import static io.spine.tools.compiler.validation.ClassNames.getClassName;

/**
 * An abstract base for method constructors.
 */
abstract class AbstractMethodConstructor implements MethodConstructor {

    /** The name of the {@code FieldDescriptor} variable. */
    private static final String FIELD_DESCRIPTOR_NAME = "fieldDescriptor";

    private final int fieldIndex;

    /** The class name of the message containing the field. */
    private final ClassName messageClass;
    private final ClassName builderClass;

    AbstractMethodConstructor(AbstractMethodConstructorBuilder builder) {
        this.fieldIndex = builder.getFieldIndex();
        this.messageClass = builder.getGenericClassName();
        String javaPackage = checkNotNull(builder.getJavaPackage());
        String javaClass = checkNotNull(builder.getJavaClass());
        this.builderClass = getClassName(javaPackage, javaClass);
    }

    /**
     * Obtains the the statement, which declares the descriptor
     * for the {@linkplain #fieldIndex field}.
     *
     * @return the statement like {@code
     *         FieldDescriptor fieldDescriptor = Msg.getDescriptor().getFields().get(fieldIndex)}
     */
    final String descriptorDeclaration() {
        CodeBlock codeBlock =
                CodeBlock.builder()
                         .add("$T $N = ", FieldDescriptor.class, FIELD_DESCRIPTOR_NAME)
                         .add(getFieldDescriptor())
                         .build();
        return codeBlock.toString();
    }

    /**
     * Obtains a builder for a {@code public} method with a return type of a validating builder.
     *
     * @param methodName
     *         the name of the setter
     * @return the pre-initialized method builder
     */
    final MethodSpec.Builder newBuilderSetter(String methodName) {
        return MethodSpec
                .methodBuilder(methodName)
                .addAnnotation(canIgnoreReturnValue())
                .addModifiers(Modifier.PUBLIC)
                .returns(builderClass());
    }

    /** Return the code block, which obtains the {@linkplain #fieldIndex field}. */
    private CodeBlock getFieldDescriptor() {
        return CodeBlock.of("$T.getDescriptor().getFields().get($L)",
                            messageClass, fieldIndex);
    }

    /** Returns the class name of the validating builder. */
    private ClassName builderClass() {
        return builderClass;
    }

    /**
     * Creates the validate statement.
     *
     * @param fieldValue
     *         the value to validate
     * @param fieldName
     *         the name of the field to validate
     * @return the constructed statement
     */
    static String validateStatement(String fieldValue, String fieldName) {
        checkNotNull(fieldValue);
        checkNotNull(fieldName);
        CodeBlock codeBlock = CodeBlock.of("validate($N, $N, $S)",
                                           FIELD_DESCRIPTOR_NAME, fieldValue, fieldName);
        return codeBlock.toString();
    }
}
