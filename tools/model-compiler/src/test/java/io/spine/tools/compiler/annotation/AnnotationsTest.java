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

package io.spine.tools.compiler.annotation;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.TypeName;
import io.spine.code.gen.java.Annotations;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.annotation.Generated;

import static io.spine.code.gen.java.Annotations.canIgnoreReturnValue;
import static io.spine.code.gen.java.Annotations.generatedBySpineModelCompiler;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Annotations utility class should")
class AnnotationsTest extends UtilityClassTest<Annotations> {

    AnnotationsTest() {
        super(Annotations.class);
    }

    @Test
    @DisplayName("provide Model Compiler annotation")
    void ofModelCompiler() {
        AnnotationSpec spec = generatedBySpineModelCompiler();
        assertEquals(spec.type, TypeName.get(Generated.class));
    }

    @Test
    @DisplayName("provide CanIgnoreReturnValue annotation")
    void ofCanIgnoreReturnValue() {
        AnnotationSpec spec = canIgnoreReturnValue();
        assertEquals(spec.type, TypeName.get(CanIgnoreReturnValue.class));
    }
}
