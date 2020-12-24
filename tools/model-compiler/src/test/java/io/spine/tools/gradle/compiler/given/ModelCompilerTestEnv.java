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

package io.spine.tools.gradle.compiler.given;

import io.spine.testing.TempDir;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;

import java.io.File;
import java.util.UUID;

import static io.spine.tools.gradle.ProtobufTaskName.generateProto;
import static io.spine.tools.gradle.ProtobufTaskName.generateTestProto;

/**
 * A helper class for the test data generation.
 */
@SuppressWarnings("UtilityClass")
public class ModelCompilerTestEnv {

    public static final String SPINE_PROTOBUF_PLUGIN_ID = "io.spine.tools.spine-model-compiler";

    /** Prevent instantiation of this utility class. */
    private ModelCompilerTestEnv() {
    }

    /** Creates a project with all required tasks. */
    public static Project newProject() {
        return newProject(TempDir.forClass(ModelCompilerTestEnv.class));
    }

    /**
     * Creates a project with all required tasks.
     *
     * <p>The project will be placed into the given directory.
     *
     * @param projectDir {@link Project#getProjectDir() Project.getProjectDir()} of the project
     */
    public static Project newProject(File projectDir) {
        Project project = ProjectBuilder.builder()
                                        .withProjectDir(projectDir)
                                        .build();
        project.getPluginManager()
               .apply("java");
        project.task(generateProto.name());
        project.task(generateTestProto.name());
        return project;
    }

    public static String newUuid() {
        String result = UUID.randomUUID()
                            .toString();
        return result;
    }
}
