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

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;

import javax.lang.model.element.Modifier;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.compiler.annotation.Annotations.canIgnoreReturnValue;
import static io.spine.tools.compiler.validation.ClassNames.getClassName;
import static io.spine.tools.compiler.validation.MethodConstructors.createDescriptorStatement;

/**
 * An abstract base for method constructors.
 */
abstract class AbstractMethodConstructor implements MethodConstructor {

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

    /** Returns the statement, which declares the descriptor for the field. */
    final String descriptorCodeLine() {
        return createDescriptorStatement(fieldIndex, messageClass);
    }

    /** Returns the class name of the validating builder. */
    final ClassName builderClass() {
        return builderClass;
    }

    final MethodSpec.Builder newBuilderSetter(String methodName) {
        return MethodSpec
                .methodBuilder(methodName)
                .addAnnotation(canIgnoreReturnValue())
                .addModifiers(Modifier.PUBLIC)
                .returns(builderClass());
    }
}
