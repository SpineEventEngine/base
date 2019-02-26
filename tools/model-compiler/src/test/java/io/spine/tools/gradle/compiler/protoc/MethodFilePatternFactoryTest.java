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
import io.spine.tools.protoc.GeneratedMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("MethodFilePatternFactory should")
final class MethodFilePatternFactoryTest {

    @DisplayName("not allow duplicate")
    @Nested
    class DoNotAllowDuplicate {

        @DisplayName("postfix pattern")
        @Test
        void postfixPattern() {
            String postfix = "test.proto";
            MethodFilePatternFactory patternFactory = factory();
            MethodPostfixPattern firstAddedPattern = patternFactory.endsWith(postfix);
            firstAddedPattern.withMethodFactory("io.spine.text.TestFactory");
            MethodPostfixPattern secondAddedPattern = patternFactory.endsWith(postfix);
            secondAddedPattern.ignore();

            ImmutableSet<FilePattern<GeneratedMethod>> patterns = patternFactory.patterns();
            assertEquals(1, patterns.size());
            FilePattern<GeneratedMethod> actualPattern = patterns.iterator()
                                                                 .next();
            assertNull(((GeneratedMethodConfig) actualPattern).methodFactory());
        }

        @DisplayName("prefix pattern")
        @Test
        void prefixPattern() {
            String prefix = "test";
            MethodFilePatternFactory patternFactory = factory();
            MethodPrefixPattern firstAddedPattern = patternFactory.startsWith(prefix);
            firstAddedPattern.withMethodFactory("io.spine.text.TestFactory");
            MethodPrefixPattern secondAddedPattern = patternFactory.startsWith(prefix);
            secondAddedPattern.ignore();

            ImmutableSet<FilePattern<GeneratedMethod>> patterns = patternFactory.patterns();
            assertEquals(1, patterns.size());
            FilePattern<GeneratedMethod> actualPattern = patterns.iterator()
                                                                 .next();
            assertNull(((GeneratedMethodConfig) actualPattern).methodFactory());
        }

        @DisplayName("regex pattern")
        @Test
        void regexPattern() {
            String regex = ".*test.*";
            MethodFilePatternFactory patternFactory = factory();
            MethodRegexPattern firstAddedPattern = patternFactory.regex(regex);
            firstAddedPattern.withMethodFactory("io.spine.text.TestFactory");
            MethodRegexPattern secondAddedPattern = patternFactory.regex(regex);
            secondAddedPattern.ignore();

            ImmutableSet<FilePattern<GeneratedMethod>> patterns = patternFactory.patterns();
            assertEquals(1, patterns.size());
            FilePattern<GeneratedMethod> actualPattern = patterns.iterator()
                                                                 .next();
            assertNull(((GeneratedMethodConfig) actualPattern).methodFactory());
        }
    }

    @DisplayName("allow same prefix and postfix patterns")
    @Test
    void allowSamePrefixAndPostfixPatterns() {
        String postfix = "test";
        String prefix = "test";
        String regex = "test";
        String methodFactory = "io.spine.text.TestFactory";
        MethodFilePatternFactory patternFactory = factory();
        MethodPostfixPattern postfixPattern = patternFactory.endsWith(postfix);
        postfixPattern.withMethodFactory(methodFactory);
        MethodPrefixPattern prefixPattern = patternFactory.startsWith(prefix);
        prefixPattern.withMethodFactory(methodFactory);
        MethodRegexPattern regexPattern = patternFactory.regex(regex);
        regexPattern.withMethodFactory(methodFactory);

        ImmutableSet<FilePattern<GeneratedMethod>> patterns = patternFactory.patterns();
        assertEquals(3, patterns.size());
    }

    MethodFilePatternFactory factory() {
        return new MethodFilePatternFactory();
    }
}
