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

package io.spine.tools.protojs;

import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.proto.FileSet;
import io.spine.tools.protojs.given.Given.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static io.spine.tools.protojs.ProtoFromJsonWriter.createFor;
import static io.spine.tools.protojs.files.JsFiles.KNOWN_TYPES;
import static io.spine.tools.protojs.files.JsFiles.KNOWN_TYPE_PARSERS;
import static io.spine.tools.protojs.files.JsFiles.jsFileName;
import static io.spine.tools.protojs.given.Given.project;
import static io.spine.tools.protojs.given.Writers.assertFileContains;
import static io.spine.tools.protojs.message.MessageHandler.FROM_JSON;
import static io.spine.tools.protojs.types.Types.isStandardOrSpineOptions;
import static java.nio.file.Files.exists;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Dmytro Kuzmin
 */
@SuppressWarnings("DuplicateStringLiteralInspection") // Common test display names.
@DisplayName("ProtoFromJsonWriter should")
class ProtoFromJsonWriterTest {

    private Path protoJsLocation;
    private ProtoFromJsonWriter writer;

    @BeforeEach
    void setUp() {
        Project project = project();
        File descriptorSetFile = project.descriptorSetFile();
        protoJsLocation = project.protoJsLocation();
        writer = createFor(protoJsLocation, descriptorSetFile);
    }

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester().testAllPublicStaticMethods(ProtoFromJsonWriter.class);
    }

    @Test
    @DisplayName("write known types map to JS file")
    void writeKnownTypes() {
        writer.writeKnownTypes();
        Path knownTypes = Paths.get(protoJsLocation.toString(), KNOWN_TYPES);
        assertTrue(exists(knownTypes));
    }

    @Test
    @DisplayName("write known type parsers map to JS file")
    void writeKnownTypeParsers() {
        writer.writeKnownTypeParsers();
        Path knownTypeParsers = Paths.get(protoJsLocation.toString(), KNOWN_TYPE_PARSERS);
        assertTrue(exists(knownTypeParsers));
    }

    @Test
    @DisplayName("write `fromJson` method into generated JS proto definitions")
    void writeFromJsonMethod() throws IOException {
        writer.writeFromJsonMethod();
        FileSet fileSet = writer.fileSet();
        checkProcessedFiles(fileSet);
    }

    private void checkProcessedFiles(FileSet fileSet) throws IOException {
        Collection<FileDescriptor> fileDescriptors = fileSet.files();
        for (FileDescriptor file : fileDescriptors) {
            if (!isStandardOrSpineOptions(file)) {
                checkFromJsonDeclared(file);
            }
        }
    }

    private void checkFromJsonDeclared(FileDescriptor file) throws IOException {
        String jsFileName = jsFileName(file);
        Path jsFilePath = Paths.get(protoJsLocation.toString(), jsFileName);
        String fromJsonDeclaration = '.' + FROM_JSON + " = function";
        assertFileContains(jsFilePath, fromJsonDeclaration);
    }
}
