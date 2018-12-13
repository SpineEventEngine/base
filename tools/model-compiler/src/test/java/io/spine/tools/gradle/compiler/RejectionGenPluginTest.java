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

package io.spine.tools.gradle.compiler;

import io.spine.protobuf.Messages;
import io.spine.tools.gradle.GradleProject;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaDocCapableSource;
import org.jboss.forge.roaster.model.source.JavaDocSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;

import static io.spine.tools.gradle.TaskName.COMPILE_JAVA;
import static io.spine.tools.gradle.compiler.given.RejectionTestEnv.getExpectedBuilderClassComment;
import static io.spine.tools.gradle.compiler.given.RejectionTestEnv.getExpectedClassComment;
import static io.spine.tools.gradle.compiler.given.RejectionTestEnv.getExpectedFirstFieldComment;
import static io.spine.tools.gradle.compiler.given.RejectionTestEnv.getExpectedSecondFieldComment;
import static io.spine.tools.gradle.compiler.given.RejectionTestEnv.newProjectWithRejectionsJavadoc;
import static io.spine.tools.gradle.compiler.given.RejectionTestEnv.rejectionsJavadocThrowableSource;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(TempDirectory.class)
@DisplayName("RejectionGenPlugin should")
class RejectionGenPluginTest {

    private File testProjectDir;

    @BeforeEach
    void setUp(@TempDir Path tempDirPath) {
        testProjectDir = tempDirPath.toFile();
    }

    @Test
    @DisplayName("compile generated rejections")
    void compile_generated_rejections() {
        Collection<String> files = Arrays.asList("test_rejections.proto",
                                                 "outer_class_by_file_name_rejections.proto",
                                                 "outer_class_set_rejections.proto",
                                                 "deps/deps.proto");
        GradleProject project = GradleProject.newBuilder()
                                             .setProjectName("rejections-gen-plugin-test")
                                             .setProjectFolder(testProjectDir)
                                             .addProtoFiles(files)
                                             .build();
        project.executeTask(COMPILE_JAVA);
    }

    @Test
    @DisplayName("generate rejection Javadoc")
    void generate_rejection_javadoc() throws FileNotFoundException {
        GradleProject project = newProjectWithRejectionsJavadoc(testProjectDir);
        project.executeTask(COMPILE_JAVA);
        String projectAbsolutePath = testProjectDir.getAbsolutePath();
        File generatedFile = new File(projectAbsolutePath + rejectionsJavadocThrowableSource());
        JavaClassSource generatedSource = Roaster.parse(JavaClassSource.class, generatedFile);
        assertRejectionJavadoc(generatedSource);
        assertBuilderJavadoc((JavaClassSource) generatedSource.getNestedType("Builder"));
    }

    private static void assertRejectionJavadoc(JavaClassSource rejection) {
        assertDoc(rejection, getExpectedClassComment());
        assertMethodDoc(rejection,
                        Messages.METHOD_NEW_BUILDER,
                        "@return a new builder for the rejection");
    }

    private static void assertBuilderJavadoc(JavaClassSource builder) {
        assertDoc(builder, getExpectedBuilderClassComment());
        assertMethodDoc(builder, "build",
                        "Creates the rejection from the builder and validates it.");
        assertMethodDoc(builder, "setId", getExpectedFirstFieldComment());
        assertMethodDoc(builder, "setRejectionMessage", getExpectedSecondFieldComment());
    }

    private static void assertMethodDoc(JavaClassSource source,
                                        String methodName,
                                        String expectedComment) {
        MethodSource<JavaClassSource> method = source.getMethods()
                                                     .stream()
                                                     .filter(m -> m.getName()
                                                                   .equals(methodName))
                                                     .findFirst()
                                                     .orElseThrow(IllegalStateException::new);
        assertDoc(method, expectedComment);
    }

    private static void assertDoc(JavaDocCapableSource source, String expectedText) {
        JavaDocSource javadoc = source.getJavaDoc();
        assertEquals(expectedText, javadoc.getFullText());
    }
}
