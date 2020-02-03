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

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import io.spine.annotation.Internal;

import javax.annotation.Generated;

/**
 * A factory for Java annotation specs.
 */
public final class Annotations {

    private static final AnnotationSpec GENERATED =
            AnnotationSpec.builder(Generated.class)
                          .addMember(GeneratedBySpine.instance().fieldName(),
                                     CodeBlock.of(GeneratedBySpine.instance().codeBlock()))
                          .build();

    private static final AnnotationSpec CAN_IGNORE_RETURN_VALUE =
            AnnotationSpec.builder(CanIgnoreReturnValue.class)
                          .build();

    /**
     * Prevents the utility class instantiation.
     */
    private Annotations() {
    }

    /**
     * Obtains annotation spec. for
     * {@link javax.annotation.Generated @Generated("by Spine Model Compiler")}
     */
    @Internal
    public static AnnotationSpec generatedBySpineModelCompiler() {
        return GENERATED;
    }

    /**
     * Obtains {@link com.google.errorprone.annotations.CanIgnoreReturnValue @CanIgnoreReturnValue}
     * annotation spec.
     */
    public static AnnotationSpec canIgnoreReturnValue() {
        return CAN_IGNORE_RETURN_VALUE;
    }
}
