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

import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("FilePatterns should")
final class FilePatternsTest extends UtilityClassTest<FilePatterns> {

    FilePatternsTest() {
        super(FilePatterns.class);
    }

    @DisplayName("not allow null values for")
    @Nested
    final class NotAllowNulls {

        @DisplayName("fileSuffix pattern")
        @Test
        void fileSuffix() {
            Assertions.assertThrows(NullPointerException.class, () -> {
                //noinspection ConstantConditions,ResultOfMethodCallIgnored
                FilePatterns.fileSuffix(null);
            });
        }

        @SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantConditions"})
        @DisplayName("filePrefix pattern")
        @Test
        void filePrefix() {
            Assertions.assertThrows(NullPointerException.class, () -> {
                FilePatterns.filePrefix(null);
            });
        }

        @SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantConditions"})
        @DisplayName("regex pattern")
        @Test
        void regex() {
            Assertions.assertThrows(NullPointerException.class, () -> {
                FilePatterns.fileRegex(null);
            });
        }
    }

    @DisplayName("create a valid")
    @Nested
    final class CreateValid {

        @DisplayName("suffix pattern")
        @Nested
        class Suffix {

            @DisplayName("with file name")
            @Test
            void withFileName() {
                String suffix = "documents.proto";
                FilePattern filter = FilePatterns.fileSuffix(suffix);
                assertEquals(suffix, filter.getSuffix());
            }

            @DisplayName("with path parts")
            @Test
            void withPathParts() {
                String suffix = "tools/protoc/documents.proto";
                FilePattern filter = FilePatterns.fileSuffix(suffix);
                assertEquals(suffix, filter.getSuffix());
            }

            @DisplayName("with absolute file path")
            @Test
            void withAbsolutePath() {
                String suffix = "/home/user/development/petproject/src/main/proto/documents.proto";
                FilePattern filter = FilePatterns.fileSuffix(suffix);
                assertEquals(suffix, filter.getSuffix());
            }
        }

        @DisplayName("prefix pattern")
        @Nested
        class Prefix {

            @DisplayName("with file name")
            @Test
            void withFileName() {
                String prefix = "documents_";
                FilePattern pattern = FilePatterns.filePrefix(prefix);
                assertEquals(prefix, pattern.getPrefix());
            }

            @DisplayName("with path parts")
            @Test
            void withPathParts() {
                String prefix = "io/spine/tools/documents_";
                FilePattern pattern = FilePatterns.filePrefix(prefix);
                assertEquals(prefix, pattern.getPrefix());
            }

            @DisplayName("with absolute file path")
            @Test
            void withAbsolutePath() {
                String prefix = "/home/user/development/petproject/src/main/proto/test_";
                FilePattern filter = FilePatterns.filePrefix(prefix);
                assertEquals(prefix, filter.getPrefix());
            }
        }

        @DisplayName("regex pattern")
        @Nested
        class Regex {

            @DisplayName("with prefix and suffix wildcards")
            @Test
            void withBothWildcards() {
                String regex = ".*documents.*";
                FilePattern pattern = FilePatterns.fileRegex(regex);
                assertEquals(regex, pattern.getRegex());
            }

            @DisplayName("with path parts")
            @Test
            void withPathParts() {
                String regex = "io/spine/.*/documents/.*\\.proto";
                FilePattern pattern = FilePatterns.fileRegex(regex);
                assertEquals(regex, pattern.getRegex());
            }

            @DisplayName("with absolute file path")
            @Test
            void withAbsolutePath() {
                String regex = "/home/user/development/petproject/.*/proto/test_.*\\.proto";
                FilePattern filter = FilePatterns.fileRegex(regex);
                assertEquals(regex, filter.getRegex());
            }
        }
    }
}
