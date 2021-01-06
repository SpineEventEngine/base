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

package io.spine.code.proto;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.testing.Assertions.assertIllegalArgument;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("FileName should")
class FileNameTest {

    /** Concatenation is used for avoiding duplicated string warning. */
    private static final String REJECTIONS_FILE_SUFFIX = "Rejection" + 's';

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void nullCheck() {
        new NullPointerTester().testStaticMethods(FileName.class,
                                                  NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    @DisplayName("require standard extension")
    void requireStandardExtension() {
        assertIllegalArgument(() -> FileName.of("some_thing"));
    }

    @Test
    @DisplayName("return words")
    void returnWords() {
        List<String> words = FileName.of("some_file_name.proto").words();

        assertEquals(ImmutableList.of("some", "file", "name"), words);
    }

    @Test
    @DisplayName("calculate outer class name")
    void calculateOuterClassName() {
        assertEquals(REJECTIONS_FILE_SUFFIX, FileName.of("rejections.proto")
                                                     .nameOnlyCamelCase());
        assertEquals("ManyRejections", FileName.of("many_rejections.proto")
                                               .nameOnlyCamelCase());
        assertEquals("ManyMoreRejections", FileName.of("many_more_rejections.proto")
                                                   .nameOnlyCamelCase());
    }

    @Nested
    @DisplayName("Calculate outer class name")
    class OuterClassName {

        @Test
        @DisplayName("one word name")
        void oneWord() {
            assertConversion(REJECTIONS_FILE_SUFFIX, "rejections.proto");
        }

        @Test
        @DisplayName("two words")
        void twoWords() {
            assertConversion("ManyRejections", "many_rejections.proto");
        }

        @Test
        @DisplayName("three words")
        void threeWords() {
            assertConversion("ManyMoreRejections", "many_more_rejections.proto");
        }

        private void assertConversion(String expected, String fileName) {
            String calculated = FileName.of(fileName)
                                        .nameOnlyCamelCase();
            assertEquals(expected, calculated);
        }
    }

    @Test
    @DisplayName("return file name without extension")
    void nameNoExtension() {
        assertEquals("package/commands", FileName.of("package/commands.proto")
                                                 .nameWithoutExtension());
    }

    @Test
    @DisplayName("tell commands file kind")
    void commandsFile() {
        FileName commandsFile = FileName.of("my_commands.proto");

        assertTrue(commandsFile.isCommands());
        assertFalse(commandsFile.isEvents());
        assertFalse(commandsFile.isRejections());
    }

    @Test
    @DisplayName("tell events file kind")
    void eventsFile() {
        FileName eventsFile = FileName.of("project_events.proto");

        assertTrue(eventsFile.isEvents());
        assertFalse(eventsFile.isCommands());
        assertFalse(eventsFile.isRejections());
    }

    @Test
    @DisplayName("tell rejection file kind")
    void rejectionsFile() {
        FileName rejectionsFile = FileName.of("rejections.proto");

        assertTrue(rejectionsFile.isRejections());
        assertFalse(rejectionsFile.isCommands());
        assertFalse(rejectionsFile.isEvents());
    }

    @Test
    @DisplayName("return file name with extension")
    void returnFileNameWithExtension(){
        FileName fileName = FileName.of("io/spine/test/test_protos.proto");

        assertEquals("test_protos.proto", fileName.nameWithExtension());
    }
}
