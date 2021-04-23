/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.tools.java.gen;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import io.spine.tools.java.javadoc.JavadocText;

import static javax.lang.model.element.Modifier.PRIVATE;

/**
 * A spec of a {@code private} parameter-less constructor.
 */
public final class EmptyPrivateCtor implements MethodCodeSpec {

    private static final EmptyPrivateCtor INSTANCE = new EmptyPrivateCtor();

    /**
     * Prevents instantiation of this class in favor of using a single created {@link #INSTANCE}.
     */
    private EmptyPrivateCtor() {
    }

    /**
     * Returns a spec for private empty parameter-less constructor.
     */
    public static MethodSpec spec() {
        MethodSpec spec = INSTANCE.methodSpec();
        return spec;
    }

    @Override
    public MethodSpec methodSpec() {
        MethodSpec result = MethodSpec
                .constructorBuilder()
                .addJavadoc(javadoc())
                .addModifiers(PRIVATE)
                .build();
        return result;
    }

    /**
     * Obtains a class-level Javadoc.
     */
    private static CodeBlock javadoc() {
        JavadocText javadoc = JavadocText.fromEscaped("Prevents instantiation of this class.")
                                         .withNewLine();
        CodeBlock value = CodeBlock
                .builder()
                .add(javadoc.value())
                .build();
        return value;
    }
}
