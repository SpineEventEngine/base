/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.tools.protojs.knowntypes;

import io.spine.code.proto.FileSet;
import io.spine.tools.protojs.given.Given.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static io.spine.tools.protojs.files.JsFiles.KNOWN_TYPES;
import static io.spine.tools.protojs.given.Given.project;
import static io.spine.tools.protojs.given.Writers.assertNonZeroSize;
import static java.nio.file.Files.exists;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SuppressWarnings("DuplicateStringLiteralInspection") // Common test display names.
@DisplayName("KnownTypesWriter should")
class KnownTypesWriterTest {

    private Path protoJsLocation;
    private KnownTypesWriter writer;

    @BeforeEach
    void setUp() {
        Project project = project();
        protoJsLocation = project.protoJsLocation();
        FileSet fileSet = project.fileSet();
        writer = KnownTypesWriter.createFor(protoJsLocation, fileSet);
    }

    @Test
    @DisplayName("compose file path")
    void composeFilePath() {
        Path filePath = KnownTypesWriter.composeFilePath(protoJsLocation);
        Path expected = Paths.get(protoJsLocation.toString(), KNOWN_TYPES);
        assertEquals(expected, filePath);
    }

    @Test
    @DisplayName("write known types map to JS file")
    void writeKnownTypes() {
        Path filePath = KnownTypesWriter.composeFilePath(protoJsLocation);
        assertFalse(exists(filePath));
        writer.writeFile();
        assertNonZeroSize(filePath);
    }
}
