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

package io.spine.tools.java.code.annotation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.spine.annotation.Internal;
import io.spine.code.java.ClassName;

import java.nio.file.Paths;

/**
 * A test-only implementation of {@link Annotator}.
 *
 * @see StubAnnotator.Factory
 */
final class StubAnnotator extends Annotator {

    private StubAnnotator() {
        super(ClassName.of(Internal.class), ImmutableList.of(), Paths.get("./"));
    }

    @Override
    public void annotate() {
        // NoOp.
    }

    /**
     * A test-only implementation of {@link AnnotatorFactory}.
     *
     * <p>All the methods memoize parameter values and return a {@link StubAnnotator}.
     */
    static final class Factory implements AnnotatorFactory {

        private ClassName annotationName;
        private ApiOption option;
        private ClassNamePattern classNamePattern;
        private ImmutableSet<MethodPattern> methodPatterns;

        @Override
        public Annotator createFileAnnotator(ClassName annotation, ApiOption option) {
            this.annotationName = annotation;
            this.option = option;
            return new StubAnnotator();
        }

        @Override
        public Annotator createMessageAnnotator(ClassName annotation, ApiOption option) {
            this.annotationName = annotation;
            this.option = option;
            return new StubAnnotator();
        }

        @Override
        public Annotator createFieldAnnotator(ClassName annotation, ApiOption option) {
            this.annotationName = annotation;
            this.option = option;
            return new StubAnnotator();
        }

        @Override
        public Annotator createServiceAnnotator(ClassName annotation, ApiOption option) {
            this.annotationName = annotation;
            this.option = option;
            return new StubAnnotator();
        }

        @Override
        public Annotator createPatternAnnotator(ClassName annotation, ClassNamePattern pattern) {
            this.annotationName = annotation;
            this.classNamePattern = pattern;
            return new StubAnnotator();
        }

        @Override
        public Annotator createMethodAnnotator(ClassName annotation,
                                               ImmutableSet<MethodPattern> patterns) {
            this.annotationName = annotation;
            this.methodPatterns = patterns;
            return new StubAnnotator();
        }

        ClassName annotationName() {
            return annotationName;
        }

        ApiOption option() {
            return option;
        }

        ClassNamePattern classNamePattern() {
            return classNamePattern;
        }

        ImmutableSet<MethodPattern> methodPatterns() {
            return methodPatterns;
        }
    }
}
