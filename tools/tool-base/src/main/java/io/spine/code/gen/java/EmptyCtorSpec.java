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

package io.spine.code.gen.java;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import io.spine.code.javadoc.JavadocText;

import javax.lang.model.element.Modifier;

import static javax.lang.model.element.Modifier.PRIVATE;

/**
 * A spec of an empty parameter-less constructor.
 */
public final class EmptyCtorSpec implements GeneratedMethodSpec {

    private static final EmptyCtorSpec INSTANCE = new EmptyCtorSpec();

    /**
     * Prevents instantiation of this class in favor of using a single created {@link #INSTANCE}.
     */
    private EmptyCtorSpec() {
    }

    /**
     * Returns a spec for private empty parameter-less constructor.
     */
    public static MethodSpec privateEmptyCtor() {
        MethodSpec spec = INSTANCE.methodSpec(PRIVATE)
                                  .toBuilder()
                                  .addJavadoc(privateCtorJavadoc())
                                  .build();
        return spec;
    }

    @Override
    public MethodSpec methodSpec(Modifier... modifiers) {
        MethodSpec result = MethodSpec
                .constructorBuilder()
                .addModifiers(modifiers)
                .build();
        return result;
    }

    /**
     * Obtains a class-level Javadoc.
     */
    private static CodeBlock privateCtorJavadoc() {
        JavadocText javadoc = JavadocText.fromEscaped("Prevents instantiation of this class.")
                                         .withNewLine();
        CodeBlock value = CodeBlock
                .builder()
                .add(javadoc.value())
                .build();
        return value;
    }
}
