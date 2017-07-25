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
import io.spine.gradle.compiler.GradleProject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.util.Arrays;
import java.util.Collection;

import static io.spine.gradle.TaskName.COMPILE_JAVA;
import static io.spine.gradle.compiler.failure.Given.TEST_SOURCE;
import static io.spine.gradle.compiler.failure.Given.getExpectedClassComment;
import static io.spine.gradle.compiler.failure.Given.getExpectedCtorComment;
import static io.spine.gradle.compiler.failure.Given.newProjectWithFailuresJavadoc;
import static org.junit.Assert.assertEquals;

/**
 * @author Dmytro Grankin
 */
public class FailuresGenPluginShould {

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();

    @Test
    public void compile_generated_failures() throws Exception {
        final Collection<String> files = Arrays.asList("test_failures.proto",
                                                       "outer_class_by_file_name_failures.proto",
                                                       "outer_class_set_failures.proto",
                                                       "deps/deps.proto");
        final GradleProject project = GradleProject.newBuilder()
                                                   .setProjectName(Given.PROJECT_NAME)
                                                   .setProjectFolder(testProjectDir)
                                                   .addProtoFiles(files)
                                                   .build();
        project.executeTask(COMPILE_JAVA);
    }

    @Test
    public void generate_failure_javadoc() throws Exception {
        final GradleProject project = newProjectWithFailuresJavadoc(testProjectDir);
        project.executeTask(COMPILE_JAVA);

        final RootDoc root = RootDocReceiver.getRootDoc(testProjectDir, TEST_SOURCE);
        final ClassDoc failureDoc = root.classes()[0];
        final ConstructorDoc failureCtorDoc = failureDoc.constructors()[0];

        assertEquals(getExpectedClassComment(), failureDoc.getRawCommentText());
        assertEquals(getExpectedCtorComment(), failureCtorDoc.getRawCommentText());
    }
}
