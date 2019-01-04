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

package io.spine.gradle.compiler;

import io.spine.test.annotation.ServiceProviderInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ProtoAnnotatorPlugin should")
class ProtoAnnotatorPluginTest {

    @Test
    @DisplayName("annotate SPI elements with provided annotation")
    void annotateSpi() {
        assertSpi(SpiMessage.class);
        assertSpi(SpiServiceGrpc.class);
        assertSpi(ImplicitlySpiMessage.class);
        assertSpi(ImplicitlySpiServiceGrpc.class);
    }

    private static void assertSpi(AnnotatedElement element) {
        assertAnnotated(element, ServiceProviderInterface.class);
    }

    private static void assertAnnotated(AnnotatedElement element,
                                        Class<? extends Annotation> expected) {
        assertTrue(element.isAnnotationPresent(expected),
                   format("%s must be %s.", element, expected));
    }

    private static void assertNotAnnotated(AnnotatedElement element,
                                           Class<? extends Annotation> notExpected) {
        assertFalse(element.isAnnotationPresent(notExpected),
                    format("%s must NOT be %s.", element, notExpected));
    }
}
