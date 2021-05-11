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

package io.spine.tools.fs;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.Assertions.assertIllegalArgument;
import static io.spine.testing.Assertions.assertIllegalState;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static io.spine.tools.fs.DirectoryPattern.INCLUDE_NESTED;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("`DirectoryPattern` should")
class DirectoryPatternTest {

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester().testAllPublicStaticMethods(DirectoryPattern.class);
    }

    @Test
    @DisplayName("not be empty")
    void notEmpty() {
        assertIllegalArgument(() -> DirectoryPattern.of(""));
    }

    @Test
    @DisplayName("be equal if directory name and inclusion are same")
    void equals() {
        new EqualsTester()
                .addEqualityGroup(DirectoryPattern.of("a"), DirectoryPattern.of("a"))
                .addEqualityGroup(DirectoryPattern.of("b"))
                .addEqualityGroup(DirectoryPattern.of("b/*"))
                .testEquals();
    }

    @Nested
    @DisplayName("obtain a directory name for")
    class DirName {

        @Test
        @DisplayName("non-nested format")
        void nameForNonNested() {
            String name = "original";
            DirectoryPattern pattern = DirectoryPattern.of(name);
            assertDirName(pattern, name);
        }

        @Test
        @DisplayName("nested format")
        void nameForNested() {
            DirectoryPattern pattern = DirectoryPattern.of("work/*");
            assertDirName(pattern, "work");
        }

        private void assertDirName(DirectoryPattern pattern, String expectedName) {
            assertThat(pattern.directoryName().value()).isEqualTo(expectedName);
        }
    }

    @Nested
    @DisplayName("match")
    class Matching {

        private void assertTransform(DirectoryPattern pattern,
                                     String originReference,
                                     String expectedReference) {
            DirectoryReference origin = DirectoryReference.of(originReference);
            DirectoryReference transformed = pattern.transform(origin);
            assertThat(transformed.value())
                    .isEqualTo(expectedReference);
        }

        @Test
        @DisplayName("match the same directory")
        void matchSame() {
            String name = "protos";
            DirectoryPattern pattern = DirectoryPattern.of(name);
            boolean matches = matches(pattern, name);
            assertTrue(matches);
            assertTransform(pattern, name, name);
        }


        @Test
        @DisplayName("nested directories if specified")
        void matchNested() {
            DirectoryPattern pattern = DirectoryPattern.of("foo/*");
            String directory = "foo/bar";
            boolean matches = matches(pattern, directory);
            assertTrue(matches);
            assertTransform(pattern, directory, directory);
        }

        @Nested
        @DisplayName("a directory if it matches")
        class DirIfMatches {

            @Test
            @DisplayName("a pattern ending")
            void matchAccordingToPatternEnding() {
                DirectoryPattern pattern = DirectoryPattern.of("base/nested");
                String directory = "nested";
                boolean matches = matches(pattern, directory);
                assertTrue(matches);
                assertTransform(pattern, directory, pattern.directoryName().value());
            }

            @Test
            @DisplayName("a middle element of the pattern")
            void matchAccordingToPatternMiddle() {
                DirectoryPattern pattern = DirectoryPattern.of("base/nested" + INCLUDE_NESTED);
                String directory = "nested/l2";
                boolean matches = matches(pattern, directory);
                assertTrue(matches);
                assertTransform(pattern, directory, "base/nested/l2");
            }
        }
    }

    @Test
    @DisplayName("not match nested directories by default")
    void notMatchNested() {
        DirectoryPattern pattern = DirectoryPattern.of("first");
        boolean matches = matches(pattern, "first/second");
        assertFalse(matches);
    }


    @Test
    @DisplayName("not match a directory only if the root is same")
    void notMatchIfOnlyRootSame() {
        DirectoryPattern pattern = DirectoryPattern.of("proto/spine/base" + INCLUDE_NESTED);
        String directory = "spine/users";
        boolean matches = matches(pattern, directory);
        assertFalse(matches);
    }

    @Test
    @DisplayName("not match if pattern is longer than directory reference")
    void notMatchIfPatternIsLonger() {
        DirectoryPattern pattern = DirectoryPattern.of("spine/foo/bar" + INCLUDE_NESTED);
        boolean matches = matches(pattern, "spine/foo");
        assertFalse(matches);
    }

    @Test
    @DisplayName("not transform if not matches")
    void notTransformNonMatching() {
        DirectoryPattern pattern = DirectoryPattern.of("a");
        DirectoryReference directory = DirectoryReference.of("b");
        assertIllegalState(() -> pattern.transform(directory));
    }

    private static boolean matches(DirectoryPattern pattern, String directoryToMatch) {
        DirectoryReference directory = DirectoryReference.of(directoryToMatch);
        return pattern.matches(directory);
    }

    @Test
    @DisplayName("cut the trailing separator in the directory name")
    void cutSuffix() {
        String pureRef = "r/e";
        DirectoryReference dir = DirectoryPattern.of(pureRef + '/').directoryName();
        assertThat(dir.value())
                .isEqualTo(pureRef);

        dir = DirectoryPattern.of(pureRef + INCLUDE_NESTED).directoryName();
        assertThat(dir.value())
                .isEqualTo(pureRef);
    }

    @Nested
    @DisplayName("compare instances")
    class Comparison {

        private DirectoryPattern p1;
        private DirectoryPattern p2;

        @Test
        @DisplayName("alphabetically")
        void asText() {
            String pat1 = "a/b/";
            p1 = DirectoryPattern.of(pat1);
            p2 = DirectoryPattern.of("c/d/");

            assertCompareSymmetrically();
            assertThat(p1.compareTo(DirectoryPattern.of(pat1)))
                    .isEqualTo(0);
        }

        @Test
        @DisplayName("taking in account suffix")
        void withSuffix() {
            p1 = DirectoryPattern.of("a/b/");
            p2 = DirectoryPattern.of("a/b" + INCLUDE_NESTED);

            assertCompareSymmetrically();
        }

        private void assertCompareSymmetrically() {
            assertThat(p1.compareTo(p2)).isLessThan(0);
            assertThat(p2.compareTo(p1)).isGreaterThan(0);
        }
    }

    @Test
    @DisplayName("prohibit infix wildcard")
    void noInfix() {
        assertIllegalArgument(() -> DirectoryPattern.of("something/in/*/between"));
    }
}
