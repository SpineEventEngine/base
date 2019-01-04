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

import io.spine.test.annotation.Alpha;
import io.spine.test.annotation.Attempt;
import io.spine.test.annotation.Private;
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

    @Test
    @DisplayName("not annotate non-SPI elements")
    void notAnnotateSpi() {
        assertNotSpi(InternalMessage.class);
        assertNotSpi(ImplicitlyInternalMessage.class);
        assertNotSpi(ImplicitlyInternalServiceGrpc.class);
    }

    private static void assertSpi(AnnotatedElement element) {
        assertAnnotated(element, ServiceProviderInterface.class);
    }

    private static void assertNotSpi(AnnotatedElement element) {
        assertNotAnnotated(element, ServiceProviderInterface.class);
    }

    @Test
    @DisplayName("annotate internal API elements with provided annotation")
    void annotateInternal() throws NoSuchMethodException {
        assertInternal(InternalMessage.class);
        assertInternal(Scaffolding.class.getDeclaredMethod("getHidden"));
        assertInternal(ImplicitlyInternalMessage.class);
        assertInternal(ImplicitlyInternalServiceGrpc.class);
    }

    @Test
    @DisplayName("not annotate non-internal API elements")
    void notAnnotateInternal() throws NoSuchMethodException {
        assertNotInternal(SpiMessage.class);
        assertNotInternal(Scaffolding.class.getDeclaredMethod("getExperiment"));
        assertNotInternal(SpiServiceGrpc.class);
        assertNotInternal(ImplicitlySpiMessage.class);
        assertNotInternal(ImplicitlySpiServiceGrpc.class);
    }

    private static void assertInternal(AnnotatedElement element) {
        assertAnnotated(element, Private.class);
    }

    private static void assertNotInternal(AnnotatedElement element) {
        assertNotAnnotated(element, Private.class);
    }

    @Test
    @DisplayName("annotate experimental API elements with provided annotation")
    void annotateExperimental() throws NoSuchMethodException {
        assertExperimental(ExperimentalMessage.class);
        assertExperimental(Scaffolding.class.getDeclaredMethod("getExperiment"));
        assertExperimental(ImplicitlyExperimentalMessage.class);
        assertExperimental(ImplicitlyExperimentalServiceGrpc.class);
    }

    @Test
    @DisplayName("not annotate non-experimental API elements")
    void notAnnotateExperimental() throws NoSuchMethodException {
        assertNotExperimental(SpiMessage.class);
        assertNotExperimental(Scaffolding.class.getDeclaredMethod("getHidden"));
        assertNotExperimental(SpiServiceGrpc.class);
        assertNotExperimental(ImplicitlySpiMessage.class);
        assertNotExperimental(ImplicitlySpiServiceGrpc.class);
    }

    private static void assertExperimental(AnnotatedElement element) {
        assertAnnotated(element, Attempt.class);
    }

    private static void assertNotExperimental(AnnotatedElement element) {
        assertNotAnnotated(element, Attempt.class);
    }

    @Test
    @DisplayName("annotate beta API elements with provided annotation")
    void annotateBeta() throws NoSuchMethodException {
        assertBeta(BetaMessage.class);
        assertBeta(Scaffolding.class.getDeclaredMethod("getLatinLetter"));
        assertBeta(ImplicitlyBetaMessage.class);
        assertBeta(ImplicitlyBetaServiceGrpc.class);
    }

    @Test
    @DisplayName("not annotate non-beta API elements")
    void notAnnotateBeta() throws NoSuchMethodException {
        assertNotBeta(ExperimentalMessage.class);
        assertNotBeta(Scaffolding.class.getDeclaredMethod("getHidden"));
        assertNotBeta(ImplicitlyExperimentalMessage.class);
        assertNotBeta(ImplicitlyExperimentalServiceGrpc.class);
    }

    private static void assertBeta(AnnotatedElement element) {
        assertAnnotated(element, Alpha.class);
    }

    private static void assertNotBeta(AnnotatedElement element) {
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
