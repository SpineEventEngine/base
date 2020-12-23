/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.tools.protoc;

import io.spine.type.MessageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.testing.Assertions.assertNpe;
import static io.spine.tools.protoc.FilePatterns.filePrefix;
import static io.spine.tools.protoc.FilePatterns.fileRegex;
import static io.spine.tools.protoc.FilePatterns.fileSuffix;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("`FilePatternMatcher` should")
class FilePatternMatcherTest {

    @DisplayName("throw `NullPointerException` if")
    @Nested
    class ThrowNpe {

        @Test
        @DisplayName("is create with `null` `FilePattern`")
        void isCreatedWithNullPattern() {
            assertNpe(() -> new FilePatternMatcher(null));
        }

        @Test
        @DisplayName("`null` `MessageType` is supplied")
        void nullMessageTypeIsSupplied() {
            assertNpe(() -> {
                FilePattern pattern = FilePattern.getDefaultInstance();
                new FilePatternMatcher(pattern).test(null);
            });
        }
    }

    @Nested
    @DisplayName("match")
    class Match {

        @Test
        @DisplayName("suffix pattern")
        void suffix() {
            assertMatches(fileSuffix("file_patterns.proto"));
        }

        @Test
        @DisplayName("prefix pattern")
        void prefix() {
            assertMatches(filePrefix("spine/tools/protoc/test_file"));
        }

        @Test
        @DisplayName("regex pattern")
        void regex() {
            assertMatches(fileRegex(".*tools\\/protoc\\/.*file_patterns.*"));
        }

        private void assertMatches(FilePattern pattern) {
            FilePatternMatcher matcher = new FilePatternMatcher(pattern);
            MessageType type = new MessageType(FPMMessage.getDescriptor());
            assertTrue(matcher.test(type));
        }
    }

    @Nested
    @DisplayName("not match")
    class NotMatch {

        @Test
        @DisplayName("suffix pattern")
        void suffix() {
            assertNotMatches(fileSuffix("test_file.proto"));
        }

        @Test
        @DisplayName("prefix pattern")
        void prefix() {
            assertNotMatches(filePrefix("spine/tools/protoc/test_patterns"));
        }

        @Test
        @DisplayName("regex pattern")
        void regex() {
            assertNotMatches(fileRegex(".*tools\\/protoc\\/.*test_patterns.*"));
        }

        private void assertNotMatches(FilePattern pattern) {
            FilePatternMatcher matcher = new FilePatternMatcher(pattern);
            MessageType type = new MessageType(FPMMessage.getDescriptor());
            assertFalse(matcher.test(type));
        }
    }
}
