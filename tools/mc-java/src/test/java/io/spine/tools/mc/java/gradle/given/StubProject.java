/*
 * Copyright 2021, TeamDev. All rights reserved.
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

package io.spine.tools.mc.java.gradle.given;

import io.spine.testing.TempDir;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.testfixtures.ProjectBuilder;

import java.io.File;

import static io.spine.tools.gradle.ProtobufTaskName.generateProto;
import static io.spine.tools.gradle.ProtobufTaskName.generateTestProto;
import static org.gradle.internal.impldep.com.google.common.base.Preconditions.checkNotNull;

/**
 * A helper for configuring a Gradle project for the needs of test suites.
 *
 * <p>NOTE: the real dependencies and their resolution in tests will often lead to relatively long
 * execution times, the {@link io.spine.testing.SlowTest} annotation should be used for such
 * test cases.
 */
public final class StubProject {

    private final Project project;

    private StubProject(Project project) {
        this.project = checkNotNull(project);
    }

    /**
     * Creates a new Gradle project for the purposes of the passed test suite class.
     */
    public static StubProject createFor(Class<?> testSuiteClass) {
        File tempDir = TempDir.forClass(testSuiteClass);
        Project project = createAt(tempDir);
        return new StubProject(project);
    }

    /**
     * Creates a project in the given directory.
     *
     * <p>The created project has:
     * <ul>
     *     <li>{@code java} plugin applied;
     *     <li>{@code generateProto} task;
     *     <li>{@code generateTestProto} task.
     * </ul>
     *
     * @param projectDir the {@linkplain Project#getProjectDir() root directory} of the project
     */
    public static Project createAt(File projectDir) {
        Project project = ProjectBuilder.builder()
                .withProjectDir(projectDir)
                .build();
        project.getPluginManager()
               .apply("java");
        project.task(generateProto.name());
        project.task(generateTestProto.name());
        return project;
    }

    /**
     * Configures the project to contain the {@code mavenLocal()} and {@code mavenCentral()}
     * repositories for proper dependency resolution.
     */
    public StubProject withMavenRepositories() {
        RepositoryHandler repositories = project.getRepositories();
        repositories.mavenLocal();
        repositories.mavenCentral();
        return this;
    }

    /**
     * Returns underlying Gradle project.
     */
    public Project get() {
        return project;
    }
}
