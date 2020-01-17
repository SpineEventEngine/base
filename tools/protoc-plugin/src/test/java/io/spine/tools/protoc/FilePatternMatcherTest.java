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

package io.spine.tools.protoc;

import io.spine.type.MessageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("FilePatternMatcher should")
class FilePatternMatcherTest {

    @DisplayName("throw NullPointerException if")
    @Nested
    class ThrowNpe {

        @DisplayName("is create with `null` FilePattern")
        @Test
        void isCreatedWithNullPattern() {
            assertThrows(NullPointerException.class, () -> new FilePatternMatcher(null));
        }

        @DisplayName("`null` MessageType is supplied")
        @Test
        void nullMessageTypeIsSupplied() {
            assertThrows(NullPointerException.class, () ->
                    new FilePatternMatcher(FilePattern.getDefaultInstance()).test(null)
            );
        }
    }

    @DisplayName("match")
    @Nested
    class Match {

        @DisplayName("suffix pattern")
        @Test
        void suffix() {
            assertMatches(FilePatterns.fileSuffix("file_patterns.proto"));
        }

        @DisplayName("prefix pattern")
        @Test
        void prefix() {
            assertMatches(FilePatterns.filePrefix("spine/tools/protoc/test_file"));
        }

        @DisplayName("regex pattern")
        @Test
        void regex() {
            assertMatches(FilePatterns.fileRegex(".*tools\\/protoc\\/.*file_patterns.*"));
        }

        private void assertMatches(FilePattern pattern) {
            FilePatternMatcher matcher = new FilePatternMatcher(pattern);
            MessageType type = new MessageType(FPMMessage.getDescriptor());
            assertTrue(matcher.test(type));
        }
    }

    @DisplayName("not match")
    @Nested
    class NotMatch {

        @DisplayName("suffix pattern")
        @Test
        void suffix() {
            assertNotMatches(FilePatterns.fileSuffix("test_file.proto"));
        }

        @DisplayName("prefix pattern")
        @Test
        void prefix() {
            assertNotMatches(FilePatterns.filePrefix("spine/tools/protoc/test_patterns"));
        }

        @DisplayName("regex pattern")
        @Test
        void regex() {
            assertNotMatches(FilePatterns.fileRegex(".*tools\\/protoc\\/.*test_patterns.*"));
        }

        private void assertNotMatches(FilePattern pattern) {
            FilePatternMatcher matcher = new FilePatternMatcher(pattern);
            MessageType type = new MessageType(FPMMessage.getDescriptor());
            assertFalse(matcher.test(type));
        }
    }
}
