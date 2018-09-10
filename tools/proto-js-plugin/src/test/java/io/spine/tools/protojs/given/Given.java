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

package io.spine.tools.protojs.given;

import io.spine.code.proto.FileSet;
import io.spine.tools.gradle.GradleProject;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static com.google.common.io.Files.createTempDir;
import static io.spine.tools.gradle.TaskName.COMPILE_PROTO_TO_JS;
import static io.spine.tools.protojs.files.ProjectFiles.mainDescriptorSetFile;
import static io.spine.tools.protojs.files.ProjectFiles.mainProtoJsLocation;

public final class Given {

    public static final String COMMANDS_PROTO = "commands.proto";
    public static final String TASK_PROTO = "task.proto";

    private static final String PROJECT_NAME = "proto-js-plugin-test";
    private static final List<String> PROTO_FILES = Arrays.asList(COMMANDS_PROTO, TASK_PROTO);

    private Given() {
    }

    public static PreparedProject preparedProject() {
        File projectDir = createTempDir();
        prepareProject(projectDir);
        Path protoJsLocation = mainProtoJsLocation(projectDir);
        File descriptorSetFile = mainDescriptorSetFile(projectDir);
        FileSet fileSet = FileSet.parse(descriptorSetFile);
        PreparedProject project = new PreparedProject(protoJsLocation, descriptorSetFile, fileSet);
        return project;
    }

    private static void prepareProject(File projectDir) {
        GradleProject gradleProject = GradleProject
                .newBuilder()
                .setProjectName(PROJECT_NAME)
                .setProjectFolder(projectDir)
                .addProtoFiles(PROTO_FILES)
                .build();
        gradleProject.executeTask(COMPILE_PROTO_TO_JS);
    }

    public static class PreparedProject {

        private final Path protoJsLocation;
        private final File descriptorSetFile;
        private final FileSet fileSet;

        private PreparedProject(Path protoJsLocation, File descriptorSetFile, FileSet fileSet) {
            this.protoJsLocation = protoJsLocation;
            this.descriptorSetFile = descriptorSetFile;
            this.fileSet = fileSet;
        }

        public Path protoJsLocation() {
            return protoJsLocation;
        }

        public File descriptorSetFile() {
            return descriptorSetFile;
        }

        public FileSet fileSet() {
            return fileSet;
        }
    }
}
