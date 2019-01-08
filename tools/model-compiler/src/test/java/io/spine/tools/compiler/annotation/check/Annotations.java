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

package io.spine.tools.compiler.annotation.check;

import io.spine.annotation.Internal;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.AnnotationTargetSource;

import java.lang.annotation.Annotation;
import java.util.Optional;

/**
 * Utilities for working with annotations in the generated code.
 */
class Annotations {

    private static final Class<? extends Annotation> ANNOTATION_CLASS = Internal.class;

    /** Prevents instantiation of this utility class. */
    private Annotations() {
    }

    static Optional<? extends AnnotationSource<?>>
    findInternalAnnotation(AnnotationTargetSource<?, ?> javaSource) {
        return findAnnotation(javaSource, ANNOTATION_CLASS);
    }

    static Optional<? extends AnnotationSource<?>>
    findAnnotation(AnnotationTargetSource<?, ?> javaSource,
                   Class<? extends Annotation> annotationType) {
        String annotationName = annotationType.getName();
        Optional<? extends AnnotationSource<?>> annotation = javaSource
                .getAnnotations()
                .stream()
                .filter(annotationSource -> annotationSource.getQualifiedName()
                                                            .equals(annotationName))
                .findAny();
        return annotation;
    }
}
