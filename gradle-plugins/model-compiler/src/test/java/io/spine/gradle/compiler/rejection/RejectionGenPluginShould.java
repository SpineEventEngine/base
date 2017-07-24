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

package io.spine.gradle.compiler.rejection;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.RootDoc;
import io.spine.gradle.compiler.Given.RejectionsGenerationConfigurator;
import io.spine.gradle.compiler.Given.RejectionsJavadocConfigurator;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.ResultHandler;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static io.spine.gradle.TaskName.COMPILE_JAVA;
import static io.spine.gradle.compiler.Given.RejectionsJavadocConfigurator.TEST_SOURCE;
import static io.spine.gradle.compiler.Given.RejectionsJavadocConfigurator.getExpectedClassComment;
import static io.spine.gradle.compiler.Given.RejectionsJavadocConfigurator.getExpectedCtorComment;
import static org.junit.Assert.assertEquals;

/**
 * @author Dmytro Grankin
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")  // It's OK: running a Gradle build inside.
public class RejectionGenPluginShould {

    @SuppressWarnings("PublicField") // Rules should be public.
    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();

    @Test
    public void compile_generated_rejections() throws Exception {
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        final ProjectConnection connection =
                new RejectionsGenerationConfigurator(testProjectDir).configure();
        final BuildLauncher launcher = connection.newBuild();

        launcher.setStandardError(System.out)
                .forTasks(COMPILE_JAVA.getValue());
        try {
            launcher.run(new ResultHandler<Void>() {
                @Override
                public void onComplete(Void aVoid) {
                    // Test passed.
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

    @Test
    public void generate_rejection_javadoc() throws Exception {
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        final ProjectConnection connection
                = new RejectionsJavadocConfigurator(testProjectDir).configure();
        final BuildLauncher launcher = connection.newBuild();

        launcher.setStandardError(System.out)
                .forTasks(COMPILE_JAVA.getValue());
        try {
            launcher.run(new ResultHandler<Void>() {
                @Override
                public void onComplete(Void aVoid) {
                    final RootDoc root = RootDocReceiver.getRootDoc(testProjectDir, TEST_SOURCE);
                    final ClassDoc rejectionDoc = root.classes()[0];
                    final ConstructorDoc rejectionCtorDoc = rejectionDoc.constructors()[0];

                    assertEquals(getExpectedClassComment(), rejectionDoc.getRawCommentText());
                    assertEquals(getExpectedCtorComment(), rejectionCtorDoc.getRawCommentText());
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
}
