/*
 * Copyright 2019, TeamDev. All rights reserved.
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

import io.spine.tools.gradle.ConfigurationName;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.initialization.dsl.ScriptHandler;

import static com.google.common.truth.Truth.assertThat;

/**
 * A configurable Gradle project.
 *
 * <p>Can be configured to contain real dependencies, repositories, etc. according to the test
 * needs.
 *
 * <p>NOTE: the real dependencies and their resolution in unit tests will often lead to relatively
 * long execution times, the {@link io.spine.testing.SlowTest} annotation should be used for such
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
     * Configures the project to contain a specified buildscript dependency within the specified
     * configuration.
     *
     * @return self for method chaining
     */
    public Project
    withBuildscriptDependency(Dependency dependency, ConfigurationName configuration) {
        Configuration config = buildscriptConfiguration(configuration);
        assertThat(config).isNotNull();
        config.getDependencies()
              .add(dependency);
        return this;
    }

    /**
     * Configures the project to contain the specified Maven repository.
     *
     * <p>Also adds {@code mavenCentral()} repository by default for convenience.
     *
     * @return self for method chaining
     */
    public Project withMavenRepository(String repositoryUrl) {
        RepositoryHandler repositories = project.getRepositories();
        repositories.mavenCentral();
        repositories.maven(repository -> repository.setUrl(repositoryUrl));
        return this;
    }

    /**
     * Returns an actual Gradle project.
     */
    public org.gradle.api.Project get() {
        return project;
    }

    private Configuration buildscriptConfiguration(ConfigurationName configuration) {
        ScriptHandler buildscript = project.getRootProject()
                                           .getBuildscript();
        ConfigurationContainer configurations = buildscript.getConfigurations();
        Configuration config = configurations.findByName(configuration.value());
        return config;
    }
}
