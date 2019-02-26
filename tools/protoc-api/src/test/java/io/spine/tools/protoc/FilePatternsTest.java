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

package io.spine.tools.protoc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("FilePatterns should")
final class FilePatternsTest {

    @DisplayName("not allow null values for")
    @Nested
    final class NotAllowNulls {

        @DisplayName("filePostfix pattern")
        @Test
        void filePostfix() {
            Assertions.assertThrows(NullPointerException.class, () -> {
                //noinspection ConstantConditions,ResultOfMethodCallIgnored
                FilePatterns.filePostfix(null);
            });
        }

        @DisplayName("filePrefix pattern")
        @Test
        void filePrefix() {
            Assertions.assertThrows(NullPointerException.class, () -> {
                //noinspection ConstantConditions,ResultOfMethodCallIgnored
                FilePatterns.filePrefix(null);
            });
        }
    }

    @DisplayName("create a valid")
    @Nested
    final class CreateValid {

        @DisplayName("file_postfix pattern")
        @Test
        void filePostfix() {
            String postfix = "documents.proto";
            FilePattern filter = FilePatterns.filePostfix(postfix);
            assertEquals(postfix, filter.getFilePostfix());
        }

        @DisplayName("file_prefix pattern")
        @Test
        void filePrefix() {
            String prefix = "documents_";
            FilePattern pattern = FilePatterns.filePrefix(prefix);
            assertEquals(prefix, pattern.getFilePrefix());
        }
    }
}
