/*
 * Copyright 2019, TeamDev. All rights reserved.
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
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import io.spine.code.gen.java.FieldName;

import javax.lang.model.element.Modifier;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.code.gen.java.Annotations.canIgnoreReturnValue;

/**
 * An abstract base for method constructors.
 */
abstract class AbstractMethodGroup implements MethodGroup {

    static final String FMT_CONVERTED_VALUE_STATEMENT = "%s.%s(convertedValue)";

    /** The name of the {@code FieldDescriptor} variable. */
    private static final String FIELD_DESCRIPTOR_NAME = "fieldDescriptor";

    private final int fieldIndex;

    /** The class name of the message containing the field. */
    private final ClassName messageClass;
    private final ClassName builderClass;

    AbstractMethodGroup(AbstractMethodGroupBuilder builder) {
        this.fieldIndex = builder.getFieldIndex();
        this.messageClass = builder.getGenericClassName();
        String javaPackage = checkNotNull(builder.getJavaPackage());
        String javaClass = checkNotNull(builder.getJavaClass());
        this.builderClass = ClassName.get(javaPackage, javaClass);
    }

    static ImmutableList.Builder<MethodSpec> methods() {
        return ImmutableList.builder();
    }

    static ImmutableList<MethodSpec> methods(MethodSpec... spec) {
        return ImmutableList.copyOf(spec);
    }

    /**
     * Obtains the the statement, which declares the descriptor
     * for the {@linkplain #fieldIndex field}.
     *
     * @return the statement like {@code
     *         FieldDescriptor fieldDescriptor = Msg.getDescriptor().getFields().get(fieldIndex)}
     */
    final String descriptorDeclaration() {
        CodeBlock getField = getFieldByIndex();
        CodeBlock codeBlock =
                CodeBlock.builder()
                         .add("$T $N = ", FieldDescriptor.class, FIELD_DESCRIPTOR_NAME)
                         .add(getField)
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
    private CodeBlock getFieldByIndex() {
        return CodeBlock.of("$T.getDescriptor().getFields().get($L)", messageClass, fieldIndex);
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
    static String validateStatement(String fieldValue, FieldName fieldName) {
        checkNotNull(fieldValue);
        checkNotNull(fieldName);
        CodeBlock codeBlock = CodeBlock.of("validate($N, $N, $S)",
                                           FIELD_DESCRIPTOR_NAME, fieldValue, fieldName);
        return codeBlock.toString();
    }

    /** Creates a statement that calls the {@code validateSetOnce} method. */
    static String validateSetOnce(String newValue) {
        CodeBlock codeBlock = CodeBlock.of("validateSetOnce($N, $N)",
                                           FIELD_DESCRIPTOR_NAME, newValue);
        return codeBlock.toString();
    }

    static String ensureNotSetOnce() {
        CodeBlock codeBlock = CodeBlock.of("checkNotSetOnce($N)", FIELD_DESCRIPTOR_NAME);
        return codeBlock.toString();
    }
}
