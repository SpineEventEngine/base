/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.tools.gradle.compiler.protoc;

import com.google.common.collect.ImmutableSet;
import io.spine.tools.protoc.GeneratedInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("InterfaceFilePatternFactory should")
final class InterfaceFilePatternFactoryTest {

    @DisplayName("not allow duplicate")
    @Nested
    class DoNotAllowDuplicate {

        @DisplayName("postfix pattern")
        @Test
        void postfixPattern() {
            String postfix = "test.proto";
            InterfaceFilePatternFactory patternFactory = factory();
            InterfacePostfixPattern firstAddedPattern = patternFactory.endsWith(postfix);
            firstAddedPattern.markWith("io.spine.text.TestInterface");
            InterfacePostfixPattern secondAddedPattern = patternFactory.endsWith(postfix);
            secondAddedPattern.ignore();

            ImmutableSet<FilePattern<GeneratedInterface>> patterns = patternFactory.patterns();
            assertEquals(1, patterns.size());
            FilePattern<GeneratedInterface> actualPattern = patterns.iterator()
                                                                    .next();
            assertNull(((GeneratedInterfaceConfig) actualPattern).interfaceName());
        }

        @DisplayName("prefix pattern")
        @Test
        void prefixPattern() {
            String prefix = "test";
            InterfaceFilePatternFactory patternFactory = factory();
            InterfacePrefixPattern firstAddedPattern = patternFactory.startsWith(prefix);
            firstAddedPattern.markWith("io.spine.text.TestInterface");
            InterfacePrefixPattern secondAddedPattern = patternFactory.startsWith(prefix);
            secondAddedPattern.ignore();

            ImmutableSet<FilePattern<GeneratedInterface>> patterns = patternFactory.patterns();
            assertEquals(1, patterns.size());
            FilePattern<GeneratedInterface> actualPattern = patterns.iterator()
                                                                    .next();
            assertNull(((GeneratedInterfaceConfig) actualPattern).interfaceName());
        }
    }

    @DisplayName("allow same prefix and postfix patterns")
    @Test
    void allowSamePrefixAndPostfixPatterns() {
        String postfix = "test.proto";
        String prefix = "test";
        InterfaceFilePatternFactory patternFactory = factory();
        InterfacePostfixPattern firstAddedPattern = patternFactory.endsWith(postfix);
        firstAddedPattern.markWith("io.spine.text.TestInterface");
        InterfacePrefixPattern secondAddedPattern = patternFactory.startsWith(prefix);
        secondAddedPattern.ignore();

        ImmutableSet<FilePattern<GeneratedInterface>> patterns = patternFactory.patterns();
        assertEquals(2, patterns.size());
    }

    InterfaceFilePatternFactory factory() {
        return new InterfaceFilePatternFactory();
    }
}
