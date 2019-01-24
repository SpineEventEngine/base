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

package io.spine.js.generate.resolve;

import com.google.common.testing.NullPointerTester;
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
    @DisplayName("obtain name for the non-nested format")
    void nameForNonNested() {
        String name = "original";
        DirectoryPattern pattern = DirectoryPattern.of(name);
        assertEquals(name, pattern.directoryName());
    }

    @Test
    @DisplayName("obtain name for the nested format")
    void nameForNested() {
        DirectoryPattern pattern = DirectoryPattern.of("work/*");
        assertEquals("work", pattern.directoryName());
    }

    @Test
    @DisplayName("not match nested directories by default")
    void notMatchNested() {
        DirectoryPattern pattern = DirectoryPattern.of("first");
        String directoryName = "first/second";
        boolean matches = pattern.matches(directoryName);
        assertFalse(matches);
    }

    @Test
    @DisplayName("match same directories")
    void matchSame() {
        String name = "protos";
        DirectoryPattern pattern = DirectoryPattern.of(name);
        boolean matches = pattern.matches(name);
        assertTrue(matches);
    }

    @Test
    @DisplayName("match nested directories if specified")
    void matchNested() {
        DirectoryPattern pattern = DirectoryPattern.of("foo/*");
        String directoryName = "foo/bar";
        boolean matches = pattern.matches(directoryName);
        assertTrue(matches);
    }

    @Test
    @DisplayName("match directories if structure is similar")
    void matchSimilarDirectories() {
        DirectoryPattern pattern = DirectoryPattern.of("base/nested");
        boolean matches = pattern.matches("nested");
        assertTrue(matches);
    }
}
