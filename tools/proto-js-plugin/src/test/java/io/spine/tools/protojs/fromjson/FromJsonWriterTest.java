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

package io.spine.tools.protojs.fromjson;

import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.proto.FileName;
import io.spine.code.proto.FileSet;
import io.spine.tools.protojs.given.Given.PreparedProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static io.spine.testing.Verify.assertContains;
import static io.spine.tools.protojs.files.JsFiles.jsFileName;
import static io.spine.tools.protojs.given.Given.preparedProject;
import static java.nio.file.Files.exists;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SuppressWarnings("DuplicateStringLiteralInspection") // Common test display names.
@DisplayName("FromJsonWriter should")
class FromJsonWriterTest {

    private static final String TASK_PROTO = "task.proto";
    private static final String OPTIONS_PROTO = "spine/options.proto";
    private static final String DESCRIPTOR_PROTO = "google/protobuf/descriptor.proto";

    private Path protoJsLocation;
    private FromJsonWriter writer;
    private FileSet fileSet;

    @BeforeEach
    void setUp() {
        PreparedProject project = preparedProject();
        fileSet = project.fileSet();
        protoJsLocation = project.protoJsLocation();
        writer = FromJsonWriter.createFor(protoJsLocation, fileSet);
    }

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester().setDefault(FileSet.class, FileSet.newInstance())
                               .testAllPublicStaticMethods(FromJsonWriter.class);
    }

    @Test
    @DisplayName("compose file path")
    void composeFilePath() {
        FileDescriptor file = getFile(TASK_PROTO);
        Path filePath = writer.composeFilePath(file);
        Path expected = Paths.get(protoJsLocation.toString(), jsFileName(file));
        assertEquals(expected, filePath);
    }

    @Test
    @DisplayName("write fromJson method into generated JS proto definitions")
    void writeFromJsonMethod() throws IOException {
        writer.writeIntoFiles();
        FileDescriptor file = getFile("task.proto");
        Path filePath = writer.composeFilePath(file);
        byte[] bytes = Files.readAllBytes(filePath);
        String fileContent = new String(bytes);
        String fromJsonDeclaration = "proto.spine.sample.protojs.TaskId.fromJson = function";
        assertContains(fromJsonDeclaration, fileContent);
    }

    @Test
    @DisplayName("skip standard types as well as spine/options.proto")
    void skipStandardAndOptions() {
        FileDescriptor options = getFile(OPTIONS_PROTO);
        Path optionsPath = writer.composeFilePath(options);
        assertFalse(exists(optionsPath));

        FileDescriptor descriptor = getFile(DESCRIPTOR_PROTO);
        Path descriptorPath = writer.composeFilePath(descriptor);
        assertFalse(exists(descriptorPath));
    }

    private FileDescriptor getFile(String name) {
        FileName taskProtoName = FileName.of(name);
        Optional<FileDescriptor> fileDescriptor = fileSet.tryFind(taskProtoName);
        return fileDescriptor.get();
    }
}
