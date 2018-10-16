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

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.RootDoc;
import io.spine.tools.compiler.rejection.RootDocReceiver;
import io.spine.tools.gradle.GradleProject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.util.Arrays;
import java.util.Collection;

import static io.spine.tools.gradle.TaskName.COMPILE_JAVA;
import static io.spine.tools.gradle.compiler.given.RejectionTestEnv.getExpectedBuilderClassComment;
import static io.spine.tools.gradle.compiler.given.RejectionTestEnv.getExpectedClassComment;
import static io.spine.tools.gradle.compiler.given.RejectionTestEnv.getExpectedFirstFieldComment;
import static io.spine.tools.gradle.compiler.given.RejectionTestEnv.getExpectedSecondFieldComment;
import static io.spine.tools.gradle.compiler.given.RejectionTestEnv.newProjectWithRejectionsJavadoc;
import static io.spine.tools.gradle.compiler.given.RejectionTestEnv.rejectionsJavadocProtoSource;
import static io.spine.tools.gradle.compiler.given.RejectionTestEnv.rejectionsJavadocThrowableSource;
import static io.spine.util.Exceptions.newIllegalStateException;
import static org.junit.Assert.assertEquals;

public class RejectionGenPluginShould {

    @Rule
    public TemporaryFolder testProjectDir = new TemporaryFolder();

    @Test
    public void compile_generated_rejections() {
        Collection<String> files = Arrays.asList("test_rejections.proto",
                                                 "outer_class_by_file_name_rejections.proto",
                                                 "outer_class_set_rejections.proto",
                                                 "deps/deps.proto");
        GradleProject project = GradleProject.newBuilder()
                                             .setProjectName("rejections-gen-plugin-test")
                                             .setProjectFolder(testProjectDir.getRoot())
                                             .addProtoFiles(files)
                                             .build();
        project.executeTask(COMPILE_JAVA);
    }

    @Test
    public void generate_rejection_javadoc() {
        GradleProject project = newProjectWithRejectionsJavadoc(testProjectDir);
        project.executeTask(COMPILE_JAVA);
        Collection<String> sources = Arrays.asList(rejectionsJavadocThrowableSource(),
                                                   rejectionsJavadocProtoSource());
        RootDoc root = RootDocReceiver.getRootDoc(testProjectDir, sources);
        assertRejectionJavadoc(root);
        assertBuilderJavadoc(root);
    }

    private static void assertRejectionJavadoc(RootDoc root) {
        ClassDoc rejection = find(root.classes(), "Rejection");
        assertDoc(getExpectedClassComment(), rejection);
        assertDoc(rejection.methods(), "newBuilder", " @return a new builder for the rejection ");
    }

    private static void assertBuilderJavadoc(RootDoc root) {
        ClassDoc builder = find(root.classes(), "Rejection.Builder");
        MethodDoc[] methods = builder.methods();
        assertDoc(getExpectedBuilderClassComment(), builder);
        assertDoc(methods, "build", " Creates the rejection from the builder and validates it. ");
        assertDoc(methods, "setId", getExpectedFirstFieldComment());
        assertDoc(methods, "setRejectionMessage", getExpectedSecondFieldComment());
    }

    private static <T extends ProgramElementDoc>
    void assertDoc(T[] docs, String docName, String expectedText) {
        T element = find(docs, docName);
        assertEquals(expectedText, element.getRawCommentText());
    }

    private static void assertDoc(String expectedText, ProgramElementDoc element) {
        assertEquals(expectedText, element.getRawCommentText());
    }

    private static <T extends ProgramElementDoc> T find(T[] docs, String name) {
        return Arrays
                .stream(docs)
                .filter(doc -> doc.name()
                                  .equals(name))
                .findFirst()
                .orElseThrow(() -> newIllegalStateException(
                        "Unable to find Javadocs for the element %s.", name));
    }
}
