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

package io.spine.gradle.compiler.failure;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.RootDoc;
import io.spine.gradle.compiler.ProjectConfigurator;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.ResultHandler;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static io.spine.gradle.TaskName.COMPILE_JAVA;
import static io.spine.gradle.compiler.ProjectConfigurator.newEmptyResultHandler;
import static io.spine.gradle.compiler.failure.FailuresGenPluginShould.FailuresJavadocConfigurator.TEST_SOURCE;
import static io.spine.gradle.compiler.failure.FailuresGenPluginShould.FailuresJavadocConfigurator.getExpectedClassComment;
import static io.spine.gradle.compiler.failure.FailuresGenPluginShould.FailuresJavadocConfigurator.getExpectedCtorComment;
import static io.spine.gradle.compiler.util.JavaCode.toJavaFieldName;
import static io.spine.gradle.compiler.util.JavaSources.getJavaExtension;
import static org.junit.Assert.assertEquals;

/**
 * @author Dmytro Grankin
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")  // It's OK: running a Gradle build inside.
public class FailuresGenPluginShould {

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();
    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    @Test
    public void compile_generated_failures() throws Exception {
        final ProjectConnection connection =
                new FailuresGenerationConfigurator(testProjectDir).configure();
        final BuildLauncher launcher = connection.newBuild();

        launcher.setStandardError(System.out)
                .forTasks(COMPILE_JAVA.getValue());
        try {
            launcher.run(newEmptyResultHandler(countDownLatch));
        } finally {
            connection.close();
        }
        countDownLatch.await(100, TimeUnit.MILLISECONDS);
    }

    @Test
    public void generate_failure_javadoc() throws Exception {
        final ProjectConnection connection
                = new FailuresJavadocConfigurator(testProjectDir).configure();
        final BuildLauncher launcher = connection.newBuild();

        launcher.setStandardError(System.out)
                .forTasks(COMPILE_JAVA.getValue());
        try {
            launcher.run(new ResultHandler<Void>() {
                @Override
                public void onComplete(Void aVoid) {
                    final RootDoc root = RootDocReceiver.getRootDoc(testProjectDir, TEST_SOURCE);
                    final ClassDoc failureDoc = root.classes()[0];
                    final ConstructorDoc failureCtorDoc = failureDoc.constructors()[0];

                    assertEquals(getExpectedClassComment(), failureDoc.getRawCommentText());
                    assertEquals(getExpectedCtorComment(), failureCtorDoc.getRawCommentText());
                    countDownLatch.countDown();
                }

                @Override
                public void onFailure(GradleConnectionException e) {
                    throw e;
                }
            });
        } finally {
            connection.close();
        }
        countDownLatch.await(100, TimeUnit.MILLISECONDS);
    }

    private static class FailuresGenerationConfigurator extends ProjectConfigurator {

        private static final String PROJECT_NAME = "failures-gen-plugin-test/";
        private static final String[] TEST_PROTO_FILES = {
                "test_failures.proto",
                "outer_class_by_file_name_failures.proto",
                "outer_class_set_failures.proto",
                "deps/deps.proto"
        };

        private FailuresGenerationConfigurator(TemporaryFolder projectDirectory) {
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

    static class FailuresJavadocConfigurator extends ProjectConfigurator {

        /** Javadocs received from {@link RootDoc} contain "\n" line separator. */
        @SuppressWarnings("HardcodedLineSeparator")
        private static final String JAVADOC_LINE_SEPARATOR = "\n";

        private static final String JAVA_PACKAGE = "io.spine.sample.failures";
        private static final String CLASS_COMMENT =
                "The failure definition to test Javadoc generation.";
        private static final String FAILURE_NAME = "Failure";
        static final String TEST_SOURCE = "/generated/main/spine/io/spine/sample/failures/"
                + FAILURE_NAME + getJavaExtension();
        private static final String FAILURES_FILE_NAME = "javadoc_failures.proto";
        private static final String FIRST_FIELD_COMMENT = "The failure ID.";
        private static final String FIRST_FIELD_NAME = "id";
        private static final String SECOND_FIELD_COMMENT = "The failure message.";
        private static final String SECOND_FIELD_NAME = "message";

        private FailuresJavadocConfigurator(TemporaryFolder projectDirectory) {
            super(projectDirectory);
        }

        static String getExpectedClassComment() {
            return ' ' + "<pre>" + JAVADOC_LINE_SEPARATOR
                    + ' ' + CLASS_COMMENT + JAVADOC_LINE_SEPARATOR
                    + " </pre>" + JAVADOC_LINE_SEPARATOR + JAVADOC_LINE_SEPARATOR
                    + " Failure based on protobuf type {@code " + JAVA_PACKAGE + '.' + FAILURE_NAME
                    + '}' + JAVADOC_LINE_SEPARATOR;
        }        @Override
        public ProjectConnection configure() throws IOException {
            writeBuildGradle();
            writeFailureProto();
            return createProjectConnection();
        }

        static String getExpectedCtorComment() {
            final String param = " @param ";
            final String firstFieldJavaName = toJavaFieldName(FIRST_FIELD_NAME, false);
            final String secondFieldJavaName = toJavaFieldName(SECOND_FIELD_NAME, false);
            return " Creates a new instance." + JAVADOC_LINE_SEPARATOR + JAVADOC_LINE_SEPARATOR
                    + param + firstFieldJavaName + "      " + FIRST_FIELD_COMMENT
                    + JAVADOC_LINE_SEPARATOR
                    + param + secondFieldJavaName + ' ' + SECOND_FIELD_COMMENT
                    + JAVADOC_LINE_SEPARATOR;
        }

        private void writeFailureProto() throws IOException {
            final Iterable<String> sourceLines = Arrays.asList(
                    "syntax = \"proto3\";",
                    "package spine.sample.failures;",
                    "option java_package = \"" + JAVA_PACKAGE + "\";",
                    "option java_multiple_files = false;",

                    "//" + CLASS_COMMENT,
                    "message " + FAILURE_NAME + " {",

                    "//" + FIRST_FIELD_COMMENT,
                    "int32 " + FIRST_FIELD_NAME + " = 1; // Is not a part of Javadoc.",

                    "//" + SECOND_FIELD_COMMENT,
                    "string " + SECOND_FIELD_NAME + " = 2;",

                    "bool hasNoComment = 3;",
                    "}"

            );

            final Path sourcePath =
                    getProjectDirectory().toPath()
                                         .resolve(BASE_PROTO_LOCATION + FAILURES_FILE_NAME);
            Files.createDirectories(sourcePath.getParent());
            Files.write(sourcePath, sourceLines, Charset.forName("UTF-8"));
        }


    }
}
