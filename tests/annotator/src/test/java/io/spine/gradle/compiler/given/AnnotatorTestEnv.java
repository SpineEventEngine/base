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

package io.spine.gradle.compiler.given;

import io.spine.test.annotation.Alpha;
import io.spine.test.annotation.Attempt;
import io.spine.test.annotation.Private;
import io.spine.test.annotation.ServiceProviderInterface;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test methods and assertions for the annotator plugin tests.
 */
public final class AnnotatorTestEnv {

    /**
     * Prevents the utility class instantiation.
     */
    private AnnotatorTestEnv() {
    }

    public static void assertSpi(AnnotatedElement element) {
        assertAnnotated(element, ServiceProviderInterface.class);
    }

    public static void assertNotSpi(AnnotatedElement element) {
        assertNotAnnotated(element, ServiceProviderInterface.class);
    }

    public static void assertInternal(AnnotatedElement element) {
        assertAnnotated(element, Private.class);
    }

    public static void assertNotInternal(AnnotatedElement element) {
        assertNotAnnotated(element, Private.class);
    }

    public static void assertExperimental(AnnotatedElement element) {
        assertAnnotated(element, Attempt.class);
    }

    public static void assertNotExperimental(AnnotatedElement element) {
        assertNotAnnotated(element, Attempt.class);
    }

    public static void assertBeta(AnnotatedElement element) {
        assertAnnotated(element, Alpha.class);
    }

    public static void assertNotBeta(AnnotatedElement element) {
        assertNotAnnotated(element, Alpha.class);
    }

    private static void assertAnnotated(AnnotatedElement element,
                                        Class<? extends Annotation> expected) {
        assertTrue(element.isAnnotationPresent(expected),
                   format("%s must be annotated with %s.", element, expected.getSimpleName()));
    }

    private static void assertNotAnnotated(AnnotatedElement element,
                                           Class<? extends Annotation> notExpected) {
        assertFalse(element.isAnnotationPresent(notExpected),
                    format("%s must NOT be annotated with %s.",
                           element,
                           notExpected.getSimpleName()));
    }
}
