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

package io.spine.tools.mc.java.annotation.check;

import io.spine.annotation.Internal;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.AnnotationTargetSource;

import java.lang.annotation.Annotation;
import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * Utilities for finding annotations in the generated code.
 */
final class Annotations {

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
        AnnotationSource<?> annotation = javaSource.getAnnotation(annotationName);
        return ofNullable(annotation);
    }
}
