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

import io.spine.tools.gradle.GradleProject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.util.Arrays;
import java.util.List;

import static io.spine.tools.gradle.TaskName.COMPILE_JAVA;

public class ValidatingBuilderGenPluginShould {

    private static final String PROJECT_NAME = "validators-gen-plugin-test";
    private static final List<String> PROTO_FILES = Arrays.asList("identifiers.proto",
                                                                  "attributes.proto",
                                                                  "changes.proto",
                                                                  "c/test_commands.proto");

    @Rule
    public TemporaryFolder testProjectDir = new TemporaryFolder();

    @Test
    public void compile_generated_validators() {
        GradleProject project =
                GradleProject.newBuilder()
                             .setProjectName(PROJECT_NAME)
                             .setProjectFolder(testProjectDir.getRoot())
                             .addProtoFiles(PROTO_FILES)
                             .build();
        project.executeTask(COMPILE_JAVA);
    }
}
