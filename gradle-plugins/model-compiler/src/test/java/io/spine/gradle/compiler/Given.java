/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

package io.spine.gradle.compiler;

import com.google.common.base.Charsets;
import com.sun.javadoc.RootDoc;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.tooling.ProjectConnection;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;

import static io.spine.gradle.TaskName.CLEAN;
import static io.spine.gradle.TaskName.COMPILE_JAVA;
import static io.spine.gradle.TaskName.COMPILE_TEST_JAVA;
import static io.spine.gradle.TaskName.GENERATE_PROTO;
import static io.spine.gradle.TaskName.GENERATE_TEST_PROTO;
import static io.spine.gradle.TaskName.PROCESS_RESOURCES;
import static io.spine.gradle.TaskName.PROCESS_TEST_RESOURCES;
import static io.spine.gradle.compiler.util.JavaCode.toJavaFieldName;
import static io.spine.gradle.compiler.util.JavaSources.getJavaExtension;

/**
 * A helper class for the test data generation.
 *
 * @author Dmytro Grankin
 */
@SuppressWarnings("UtilityClass")
public class Given {

    static final String SPINE_PROTOBUF_PLUGIN_ID = "io.spine.tools.spine-model-compiler";

    /** Prevents instantiation of this utility class. */
    private Given() {}

    /** Creates a project with all required tasks. */
    static Project newProject() {
        final Project project = ProjectBuilder.builder()
                                              .build();
        project.task(CLEAN.getValue());
        project.task(GENERATE_PROTO.getValue());
        project.task(GENERATE_TEST_PROTO.getValue());
        project.task(COMPILE_JAVA.getValue());
        project.task(COMPILE_TEST_JAVA.getValue());
        project.task(PROCESS_RESOURCES.getValue());
        project.task(PROCESS_TEST_RESOURCES.getValue());
        return project;
    }

    static String newUuid() {
        final String result = UUID.randomUUID()
                                  .toString();
        return result;
    }

    public static class RejectionsGenerationConfigurator extends ProjectConfigurator {

        private static final String PROJECT_NAME = "rejections-gen-plugin-test/";
        private static final String[] TEST_PROTO_FILES = {
                "test_rejections.proto",
                "outer_class_by_file_name_rejections.proto",
                "outer_class_set_rejections.proto",
                "deps/deps.proto"
        };

        public RejectionsGenerationConfigurator(TemporaryFolder projectDirectory) {
            super(projectDirectory);
        }

        @Override
        public ProjectConnection configure() throws IOException {
            writeBuildGradle();
            for (String protoFile : TEST_PROTO_FILES) {
                writeProto(PROJECT_NAME, protoFile);
            }

            return createProjectConnection();
        }
    }

    public static class ValidatorsGenerationConfigurator extends ProjectConfigurator {

        private static final String PROJECT_NAME = "validators-gen-plugin-test/";
        private static final String[] TEST_PROTO_FILES = {
                "identifiers.proto",
                "attributes.proto",
                "changes.proto",
                "c/test_commands.proto"
        };

        public ValidatorsGenerationConfigurator(TemporaryFolder projectDirectory) {
            super(projectDirectory);
        }

        @Override
        public ProjectConnection configure() throws IOException {
            writeBuildGradle();
            for (String protoFile : TEST_PROTO_FILES) {
                writeProto(PROJECT_NAME, protoFile);
            }

            return createProjectConnection();
        }
    }

    public static class RejectionsJavadocConfigurator extends ProjectConfigurator {

        /** Javadocs received from {@link RootDoc} contain "\n" line separator. */
        @SuppressWarnings("HardcodedLineSeparator")
        private static final String JAVADOC_LINE_SEPARATOR = "\n";

        private static final String JAVA_PACKAGE = "io.spine.sample.rejections";
        private static final String CLASS_COMMENT =
                "The rejection definition to test Javadoc generation.";
        private static final String REJECTION_OUTER_CLASS_NAME = "Rejections";
        private static final String REJECTIONS_FILE_NAME = "javadoc_rejections.proto";
        private static final String FIRST_FIELD_COMMENT = "The rejection ID.";
        private static final String FIRST_FIELD_NAME = "id";
        private static final String SECOND_FIELD_COMMENT = "The rejection message.";
        private static final String SECOND_FIELD_NAME = "message";
        public static final String TEST_SOURCE = "/generated/main/spine/io/spine/sample/rejections/"
                + REJECTION_OUTER_CLASS_NAME + getJavaExtension();

        public RejectionsJavadocConfigurator(TemporaryFolder projectDirectory) {
            super(projectDirectory);
        }

        @Override
        public ProjectConnection configure() throws IOException {
            writeBuildGradle();
            writeRejectionProto();
            return createProjectConnection();
        }

        private void writeRejectionProto() throws IOException {
            final Iterable<String> sourceLines = Arrays.asList(
                    "syntax = \"proto3\";",
                    "package spine.sample.rejections;",
                    "option java_package = \"" + JAVA_PACKAGE + "\";",
                    "option java_multiple_files = false;",

                    "//" + CLASS_COMMENT,
                    "message " + REJECTION_OUTER_CLASS_NAME + " {",

                    "//" + FIRST_FIELD_COMMENT,
                    "int32 " + FIRST_FIELD_NAME + " = 1; // Is not a part of Javadoc.",

                    "//" + SECOND_FIELD_COMMENT,
                    "string " + SECOND_FIELD_NAME + " = 2;",

                    "bool hasNoComment = 3;",
                    "}"

            );

            final Path sourcePath =
                    getProjectDirectory().toPath()
                                         .resolve(BASE_PROTO_LOCATION + REJECTIONS_FILE_NAME);
            Files.createDirectories(sourcePath.getParent());
            Files.write(sourcePath, sourceLines, Charsets.UTF_8);
        }

        public static String getExpectedClassComment() {
            return ' ' + "<pre>" + JAVADOC_LINE_SEPARATOR
                    + ' ' + CLASS_COMMENT + JAVADOC_LINE_SEPARATOR
                    + " </pre>" + JAVADOC_LINE_SEPARATOR + JAVADOC_LINE_SEPARATOR
                    + " Rejection based on proto type {@code " + JAVA_PACKAGE + '.' +
                    REJECTION_OUTER_CLASS_NAME
                    + '}' + JAVADOC_LINE_SEPARATOR;
        }

        public static String getExpectedCtorComment() {
            final String param = " @param ";
            final String firstFieldJavaName = toJavaFieldName(FIRST_FIELD_NAME, false);
            final String secondFieldJavaName = toJavaFieldName(SECOND_FIELD_NAME, false);
            return " Creates a new instance." + JAVADOC_LINE_SEPARATOR + JAVADOC_LINE_SEPARATOR
                    + param + firstFieldJavaName + "      " + FIRST_FIELD_COMMENT
                    + JAVADOC_LINE_SEPARATOR
                    + param + secondFieldJavaName + ' ' + SECOND_FIELD_COMMENT
                    + JAVADOC_LINE_SEPARATOR;
        }
    }
}
