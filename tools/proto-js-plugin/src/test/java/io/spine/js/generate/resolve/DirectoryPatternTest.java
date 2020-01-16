/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.js.generate.resolve;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import io.spine.code.fs.js.DirectoryReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("FolderPattern should")
class DirectoryPatternTest {

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester().testAllPublicStaticMethods(DirectoryPattern.class);
    }

    @Test
    @DisplayName("not be empty")
    void notEmpty() {
        assertThrows(
                IllegalArgumentException.class,
                () -> DirectoryPattern.of("")
        );
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

    @Test
    @DisplayName("obtain name for the non-nested format")
    void nameForNonNested() {
        String name = "original";
        DirectoryPattern pattern = DirectoryPattern.of(name);
        assertEquals(name, pattern.directoryName()
                                  .value());
    }

    @Test
    @DisplayName("obtain name for the nested format")
    void nameForNested() {
        DirectoryPattern pattern = DirectoryPattern.of("work/*");
        assertEquals("work", pattern.directoryName()
                                    .value());
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
    @DisplayName("match nested directories if specified")
    void matchNested() {
        DirectoryPattern pattern = DirectoryPattern.of("foo/*");
        String directory = "foo/bar";
        boolean matches = matches(pattern, directory);
        assertTrue(matches);
        assertTransform(pattern, directory, directory);
    }

    @Test
    @DisplayName("not match nested directories by default")
    void notMatchNested() {
        DirectoryPattern pattern = DirectoryPattern.of("first");
        boolean matches = matches(pattern, "first/second");
        assertFalse(matches);
    }

    @Test
    @DisplayName("match directory if it matches the pattern ending")
    void matchAccordingToPatternEnding() {
        DirectoryPattern pattern = DirectoryPattern.of("base/nested");
        String directory = "nested";
        boolean matches = matches(pattern, directory);
        assertTrue(matches);
        assertTransform(pattern, directory, pattern.directoryName()
                                                   .value());
    }

    @Test
    @DisplayName("match directory if it matches the pattern middle element")
    void matchAccordingToPatternMiddle() {
        DirectoryPattern pattern = DirectoryPattern.of("base/nested/*");
        String directory = "nested/l2";
        boolean matches = matches(pattern, directory);
        assertTrue(matches);
        assertTransform(pattern, directory, "base/nested/l2");
    }

    @Test
    @DisplayName("not match a directory only if the root is same")
    void notMatchIfOnlyRootSame() {
        DirectoryPattern pattern = DirectoryPattern.of("proto/spine/base/*");
        String directory = "spine/users";
        boolean matches = matches(pattern, directory);
        assertFalse(matches);
    }

    @Test
    @DisplayName("not match if pattern is longer than directory reference")
    void notMatchIfPatternIsLonger() {
        DirectoryPattern pattern = DirectoryPattern.of("spine/foo/bar/*");
        boolean matches = matches(pattern, "spine/foo");
        assertFalse(matches);
    }

    @Test
    @DisplayName("not transform if not matches")
    void notTransformNonMatching() {
        DirectoryPattern pattern = DirectoryPattern.of("a");
        DirectoryReference directory = DirectoryReference.of("b");
        assertThrows(
                IllegalStateException.class,
                () -> pattern.transform(directory)
        );
    }

    private static boolean matches(DirectoryPattern pattern, String directoryToMatch) {
        DirectoryReference directory = DirectoryReference.of(directoryToMatch);
        return pattern.matches(directory);
    }

    private static void assertTransform(DirectoryPattern pattern,
                                        String originReference,
                                        String expectedReference) {
        DirectoryReference origin = DirectoryReference.of(originReference);
        DirectoryReference transformed = pattern.transform(origin);
        assertEquals(expectedReference, transformed.value());
    }
}
