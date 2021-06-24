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

package io.spine.tools.mc.java.codegen;

import io.spine.testing.UtilityClassTest;
import io.spine.tools.protoc.FilePattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.testing.Assertions.assertNpe;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("FilePatterns should")
final class FilePatternsTest extends UtilityClassTest<FilePatterns> {

    FilePatternsTest() {
        super(FilePatterns.class);
    }

    @Nested
    @DisplayName("not allow `null` values for")
    @SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantConditions"})
    final class NotAllowNulls {

        @Test
        @DisplayName("fileSuffix pattern")
        void fileSuffix() {
            assertNpe(() -> FilePatterns.fileSuffix(null));
        }

        @Test
        @DisplayName("filePrefix pattern")
        void filePrefix() {
            assertNpe(() -> FilePatterns.filePrefix(null));
        }

        @Test
        @DisplayName("regex pattern")
        void regex() {
            assertNpe(() -> FilePatterns.fileRegex(null));
        }
    }

    @Nested
    @DisplayName("create a valid")
    final class CreateValid {

        @Nested
        @DisplayName("suffix pattern")
        class Suffix {

            @Test
            @DisplayName("with file name")
            void withFileName() {
                String suffix = "documents.proto";
                FilePattern filter = FilePatterns.fileSuffix(suffix);
                assertEquals(suffix, filter.getSuffix());
            }

            @Test
            @DisplayName("with path parts")
            void withPathParts() {
                String suffix = "tools/protoc/documents.proto";
                FilePattern filter = FilePatterns.fileSuffix(suffix);
                assertEquals(suffix, filter.getSuffix());
            }

            @Test
            @DisplayName("with absolute file path")
            void withAbsolutePath() {
                String suffix = "/home/user/development/petproject/src/main/proto/documents.proto";
                FilePattern filter = FilePatterns.fileSuffix(suffix);
                assertEquals(suffix, filter.getSuffix());
            }
        }

        @Nested
        @DisplayName("prefix pattern")
        class Prefix {

            @Test
            @DisplayName("with file name")
            void withFileName() {
                String prefix = "documents_";
                FilePattern pattern = FilePatterns.filePrefix(prefix);
                assertEquals(prefix, pattern.getPrefix());
            }

            @Test
            @DisplayName("with path parts")
            void withPathParts() {
                String prefix = "io/spine/tools/documents_";
                FilePattern pattern = FilePatterns.filePrefix(prefix);
                assertEquals(prefix, pattern.getPrefix());
            }

            @Test
            @DisplayName("with absolute file path")
            void withAbsolutePath() {
                String prefix = "/home/user/development/petproject/src/main/proto/test_";
                FilePattern filter = FilePatterns.filePrefix(prefix);
                assertEquals(prefix, filter.getPrefix());
            }
        }

        @Nested
        @DisplayName("regex pattern")
        class Regex {

            @Test
            @DisplayName("with prefix and suffix wildcards")
            void withBothWildcards() {
                String regex = ".*documents.*";
                FilePattern pattern = FilePatterns.fileRegex(regex);
                assertEquals(regex, pattern.getRegex());
            }

            @Test
            @DisplayName("with path parts")
            void withPathParts() {
                String regex = "io/spine/.*/documents/.*\\.proto";
                FilePattern pattern = FilePatterns.fileRegex(regex);
                assertEquals(regex, pattern.getRegex());
            }

            @Test
            @DisplayName("with absolute file path")
            void withAbsolutePath() {
                String regex = "/home/user/development/petproject/.*/proto/test_.*\\.proto";
                FilePattern filter = FilePatterns.fileRegex(regex);
                assertEquals(regex, filter.getRegex());
            }
        }
    }
}
