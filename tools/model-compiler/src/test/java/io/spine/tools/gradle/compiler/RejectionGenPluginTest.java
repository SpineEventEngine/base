/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.tools.gradle.compiler;

import com.google.common.collect.ImmutableList;
import io.spine.code.java.SimpleClassName;
import io.spine.protobuf.Messages;
import io.spine.tools.gradle.testing.GradleProject;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaDocCapableSource;
import org.jboss.forge.roaster.model.source.JavaDocSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

import static io.spine.tools.gradle.JavaTaskName.compileJava;
import static io.spine.tools.gradle.compiler.given.RejectionTestEnv.getExpectedBuilderClassComment;
import static io.spine.tools.gradle.compiler.given.RejectionTestEnv.getExpectedClassComment;
import static io.spine.tools.gradle.compiler.given.RejectionTestEnv.getExpectedFirstFieldComment;
import static io.spine.tools.gradle.compiler.given.RejectionTestEnv.getExpectedSecondFieldComment;
import static io.spine.tools.gradle.compiler.given.RejectionTestEnv.newProjectWithRejectionsJavadoc;
import static io.spine.tools.gradle.compiler.given.RejectionTestEnv.rejectionsJavadocThrowableSource;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("RejectionGenPlugin should")
class RejectionGenPluginTest {

    private File testProjectDir;

    @BeforeEach
    void setUp(@TempDir Path tempDirPath) {
        testProjectDir = tempDirPath.toFile();
    }

    @Test
    @DisplayName("compile generated rejections")
    void compileGeneratedRejections() {
        Collection<String> files = ImmutableList.of("test_rejections.proto",
                                                    "outer_class_by_file_name_rejections.proto",
                                                    "outer_class_set_rejections.proto",
                                                    "deps/deps.proto");
        GradleProject project = GradleProject.newBuilder()
                                             .setProjectName("rejections-gen-plugin-test")
                                             .setProjectFolder(testProjectDir)
                                             .addProtoFiles(files)
                                             .build();
        project.executeTask(compileJava);
    }

    @Test
    @DisplayName("generate rejection Javadoc")
    void generateRejectionJavadoc() throws IOException {
        GradleProject project = newProjectWithRejectionsJavadoc(testProjectDir);
        project.executeTask(compileJava);
        String projectAbsolutePath = testProjectDir.getAbsolutePath();
        File generatedFile = new File(projectAbsolutePath + rejectionsJavadocThrowableSource());
        JavaClassSource generatedSource = Roaster.parse(JavaClassSource.class, generatedFile);
        assertRejectionJavadoc(generatedSource);
        assertBuilderJavadoc(
                (JavaClassSource) generatedSource.getNestedType(SimpleClassName.ofBuilder()
                                                                               .value())
        );
    }

    private static void assertRejectionJavadoc(JavaClassSource rejection) {
        assertDoc(getExpectedClassComment(), rejection);
        assertMethodDoc("@return a new builder for the rejection", rejection,
                        Messages.METHOD_NEW_BUILDER
        );
    }

    private static void assertBuilderJavadoc(JavaClassSource builder) {
        assertDoc(getExpectedBuilderClassComment(), builder);
        assertMethodDoc("Creates the rejection from the builder and validates it.", builder, "build"
        );
        assertMethodDoc(getExpectedFirstFieldComment(), builder, "setId");
        assertMethodDoc(getExpectedSecondFieldComment(), builder, "setRejectionMessage");
    }

    private static void assertMethodDoc(String expectedComment,
                                        JavaClassSource source,
                                        String methodName) {
        MethodSource<JavaClassSource> method = source.getMethods()
                                                     .stream()
                                                     .filter(m -> m.getName()
                                                                   .equals(methodName))
                                                     .findFirst()
                                                     .orElseThrow(IllegalStateException::new);
        assertDoc(expectedComment, method);
    }

    private static void assertDoc(String expectedText, JavaDocCapableSource source) {
        JavaDocSource javadoc = source.getJavaDoc();
        assertEquals(expectedText, javadoc.getFullText());
    }
}
