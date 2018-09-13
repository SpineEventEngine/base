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

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.proto.FileSet;
import io.spine.tools.gradle.GradleProject;
import spine.test.protojs.Fields.FieldContainer;
import spine.test.protojs.Fields.TaskType;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static com.google.common.io.Files.createTempDir;
import static io.spine.tools.gradle.TaskName.COMPILE_PROTO_TO_JS;
import static io.spine.tools.protojs.files.ProjectFiles.mainDescriptorSetFile;
import static io.spine.tools.protojs.files.ProjectFiles.mainProtoJsLocation;
import static io.spine.tools.protojs.given.FieldContainerEntry.ENUM_FIELD;
import static io.spine.tools.protojs.given.FieldContainerEntry.PRIMITIVE_FIELD;
import static io.spine.tools.protojs.given.FieldContainerEntry.MAP_FIELD;
import static io.spine.tools.protojs.given.FieldContainerEntry.MESSAGE_FIELD;
import static io.spine.tools.protojs.given.FieldContainerEntry.REPEATED_FIELD;
import static io.spine.tools.protojs.given.FieldContainerEntry.TIMESTAMP_FIELD;
import static java.util.Collections.singletonList;

/**
 * @author Dmytro Kuzmin
 */
public final class Given {

    public static final String TASK_PROTO = "task.proto";

    private static final String PROJECT_NAME = "proto-js-plugin-test";
    private static final List<String> PROTO_FILES = singletonList(TASK_PROTO);

    private Given() {
    }

    public static FileDescriptor file() {
        FileDescriptor file = message().getFile();
        return file;
    }

    public static Descriptor message() {
        Descriptor message = FieldContainer.getDescriptor();
        return message;
    }

    public static EnumDescriptor enumType() {
        EnumDescriptor enumType = TaskType.getDescriptor();
        return enumType;
    }

    public static FieldDescriptor primitiveField() {
        return field(PRIMITIVE_FIELD);
    }

    public static FieldDescriptor enumField() {
        return field(ENUM_FIELD);
    }

    public static FieldDescriptor messageField() {
        return field(MESSAGE_FIELD);
    }

    public static FieldDescriptor timestampField() {
        return field(TIMESTAMP_FIELD);
    }

    public static FieldDescriptor singularField() {
        return field(MESSAGE_FIELD);
    }

    public static FieldDescriptor repeatedField() {
        return field(REPEATED_FIELD);
    }

    public static FieldDescriptor mapField() {
        return field(MAP_FIELD);
    }

    private static FieldDescriptor field(FieldContainerEntry entry) {
        String fieldName = entry.protoName();
        FieldDescriptor field = message().findFieldByName(fieldName);
        return field;
    }

    public static Project project() {
        File projectDir = createTempDir();
        compileProject(projectDir);
        Project project = Project.at(projectDir);
        return project;
    }

    private static void compileProject(File projectDir) {
        GradleProject gradleProject = GradleProject
                .newBuilder()
                .setProjectName(PROJECT_NAME)
                .setProjectFolder(projectDir)
                .addProtoFiles(PROTO_FILES)
                .build();
        gradleProject.executeTask(COMPILE_PROTO_TO_JS);
    }

    public static class Project {

        private final Path protoJsLocation;
        private final File descriptorSetFile;
        private final FileSet fileSet;

        private Project(Path protoJsLocation, File descriptorSetFile, FileSet fileSet) {
            this.protoJsLocation = protoJsLocation;
            this.descriptorSetFile = descriptorSetFile;
            this.fileSet = fileSet;
        }

        private static Project at(File projectDir) {
            Path protoJsLocation = mainProtoJsLocation(projectDir);
            File descriptorSetFile = mainDescriptorSetFile(projectDir);
            FileSet fileSet = FileSet.parse(descriptorSetFile);
            return new Project(protoJsLocation, descriptorSetFile, fileSet);
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
