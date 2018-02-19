/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

package io.spine.tools.gradle.compiler.rejection;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.RootDoc;
import io.spine.tools.compiler.rejection.RootDocReceiver;
import io.spine.tools.gradle.compiler.rejection.given.Given;
import io.spine.tools.gradle.given.GradleProject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.util.Arrays;
import java.util.Collection;

import static io.spine.tools.gradle.TaskName.COMPILE_JAVA;
import static io.spine.tools.gradle.compiler.rejection.given.Given.getExpectedClassComment;
import static io.spine.tools.gradle.compiler.rejection.given.Given.getExpectedCtorComment;
import static io.spine.tools.gradle.compiler.rejection.given.Given.rejectionsJavadocSourceName;
import static org.junit.Assert.assertEquals;

/**
 * @author Dmytro Grankin
 */
public class RejectionGenPluginShould {

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();

    @Test
    public void compile_generated_rejections() {
        final Collection<String> files = Arrays.asList("test_rejections.proto",
                                                       "outer_class_by_file_name_rejections.proto",
                                                       "outer_class_set_rejections.proto",
                                                       "deps/deps.proto");
        final GradleProject project = GradleProject.newBuilder()
                                                   .setProjectName("rejections-gen-plugin-test")
                                                   .setProjectFolder(testProjectDir)
                                                   .addProtoFiles(files)
                                                   .build();
        project.executeTask(COMPILE_JAVA);
    }

    @Test
    public void generate_rejection_javadoc() {

        final GradleProject project = Given.newProjectWithRejectionsJavadoc(testProjectDir);
        project.executeTask(COMPILE_JAVA);

        final RootDoc root = RootDocReceiver.getRootDoc(testProjectDir,
                                                        rejectionsJavadocSourceName());
        final ClassDoc rejectionDoc = root.classes()[0];
        final ConstructorDoc rejectionCtorDoc = rejectionDoc.constructors()[0];

        assertEquals(getExpectedClassComment(), rejectionDoc.getRawCommentText());
        assertEquals(getExpectedCtorComment(), rejectionCtorDoc.getRawCommentText());
    }
}
