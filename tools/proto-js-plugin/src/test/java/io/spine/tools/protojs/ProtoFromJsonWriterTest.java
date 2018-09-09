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

import com.google.common.io.Files;
import com.google.common.testing.NullPointerTester;
import io.spine.code.DefaultProject;
import io.spine.tools.gradle.GradleProject;
import io.spine.tools.protojs.files.JsFiles;
import io.spine.tools.protojs.knowntypes.KnownTypesWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static io.spine.tools.gradle.TaskName.COMPILE_PROTO_TO_JS;
import static io.spine.tools.protojs.ProtoFromJsonWriter.createFor;
import static io.spine.tools.protojs.files.ProjectFiles.mainProtoJsLocation;
import static java.nio.file.Files.exists;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ProtoFromJsonWriter should")
class ProtoFromJsonWriterTest {

    private static final List<String> PROTO_FILES = Arrays.asList("commands.proto", "task.proto");

    private File projectDir;
    private DefaultProject project;

    @BeforeEach
    void setUp() {
        projectDir = Files.createTempDir();
        GradleProject gradleProject = GradleProject
                .newBuilder()
                .setProjectName("proto-js-plugin-test")
                .setProjectFolder(projectDir)
                .addProtoFiles(PROTO_FILES)
                .build();
        gradleProject.executeTask(COMPILE_PROTO_TO_JS);
        project = DefaultProject.at(projectDir);
    }

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester().testAllPublicStaticMethods(ProtoFromJsonWriter.class);
    }

    @Test
    @DisplayName("write known types map to JS file")
    void writeKnownTypes() {
        Path protoJsLocation = mainProtoJsLocation(projectDir);
        File descriptorSetFile = project.mainDescriptors();
        assertTrue(exists(protoJsLocation));
        assertTrue(exists(descriptorSetFile.toPath()));

        ProtoFromJsonWriter writer = createFor(protoJsLocation, descriptorSetFile);
        assertTrue(writer.hasFilesToProcess());

        writer.writeFromJsonForProtos();
        Path knownTypes = Paths.get(protoJsLocation.toString(), JsFiles.KNOWN_TYPES);
        assertTrue(exists(knownTypes));
    }
}
