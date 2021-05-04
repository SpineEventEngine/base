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

package io.spine.tools.gradle.compiler.given;

import org.gradle.api.artifacts.dsl.RepositoryHandler;

/**
 * A configurable Gradle project.
 *
 * <p>Can be configured to contain real dependencies, repositories, etc. according to the test
 * needs.
 *
 * <p>NOTE: the real dependencies and their resolution in tests will often lead to relatively long
 * execution times, the {@link io.spine.testing.SlowTest} annotation should be used for such
 * test cases.
 */
public final class Project {

    private final org.gradle.api.Project project;

    private Project(org.gradle.api.Project project) {
        this.project = project;
    }

    /**
     * Creates a new instance of the project.
     */
    public static Project newProject() {
        return new Project(ModelCompilerTestEnv.newProject());
    }

    /**
     * Configures the project to contain the {@code mavenLocal()} and {@code mavenCentral()}
     * repositories for proper dependency resolution.
     *
     * @return self for method chaining
     */
    public Project withMavenRepositories() {
        RepositoryHandler repositories = project.getRepositories();
        repositories.mavenLocal();
        repositories.mavenCentral();
        return this;
    }

    /**
     * Returns an actual Gradle project.
     */
    public org.gradle.api.Project get() {
        return project;
    }
}
