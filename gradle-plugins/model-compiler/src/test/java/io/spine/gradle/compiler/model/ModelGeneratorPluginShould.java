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

package io.spine.gradle.compiler.model;

import io.spine.gradle.compiler.GradleProject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static io.spine.gradle.TaskName.GENERATE_MODEL;

/**
 * @author Dmytro Dashenkov
 */
public class ModelGeneratorPluginShould {

    private static final String PROJECT_NAME = "model-generator-test";

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();

    @Test
    public void compile() {
        newProjectWithJava("io/spine/tools/model/TestAggregate.java").executeTask(GENERATE_MODEL);
    }

    private GradleProject newProjectWithJava(String fileName) {
        return GradleProject.newBuilder()
                            .setProjectName(PROJECT_NAME)
                            .setProjectFolder(testProjectDir)
                            .addJavaFile(fileName)
                            .build();
    }
}
