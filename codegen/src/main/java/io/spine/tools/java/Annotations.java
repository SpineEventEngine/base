/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

package io.spine.tools.java;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;

import javax.annotation.Generated;

/**
 * A factory for Java annotation specs.
 *
 * @author Dmytro Dashenkov
 */
public final class Annotations {

    @SuppressWarnings("DuplicateStringLiteralInspection")
        // Each occurrence has a different semantics.
    private static final String GENERATED_FIELD_NAME = "value";

    private static final AnnotationSpec GENERATED =
            AnnotationSpec.builder(Generated.class)
                          .addMember(GENERATED_FIELD_NAME,
                                     CodeBlock.of("\"by Spine Model Compiler\""))
                          .build();

    /**
     * Prevents the utility class instantiation.
     */
    private Annotations() {
    }

    /**
     * Generates {@code \@Generated("by Spine Model Compiler")} annotation spec.
     *
     * @return an {@link AnnotationSpec} describing the {@link Generated javax.annotation.Generated}
     *         annotation
     */
    public static AnnotationSpec generatedBySpineModelCompiler() {
        return GENERATED;
    }
}
