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

package io.spine.tools.gradle.project;

import com.google.common.collect.ImmutableMap;
import io.spine.tools.gradle.ConfigurationName;
import io.spine.tools.gradle.Dependency;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.gradle.ConfigurationName.runtimeClasspath;
import static io.spine.tools.gradle.ConfigurationName.testRuntimeClasspath;
import static org.gradle.api.artifacts.ExcludeRule.GROUP_KEY;
import static org.gradle.api.artifacts.ExcludeRule.MODULE_KEY;

/**
 * A {@link Dependant} implemented on top of a {@link DependencyHandler} of a project.
 */
public final class DependantProject implements Dependant {

    private final DependencyHandler dependencies;
    private final ConfigurationContainer configurations;

    private DependantProject(DependencyHandler dependencies,
                             ConfigurationContainer configurations) {
        this.dependencies = dependencies;
        this.configurations = configurations;
    }

    /**
     * Creates a new instance of {@code DependantProject} for the given project.
     */
    public static DependantProject from(Project project) {
        checkNotNull(project);
        return new DependantProject(project.getDependencies(),
                                    project.getConfigurations());
    }

    @Override
    public void depend(ConfigurationName configurationName, String notation) {
        dependencies.add(configurationName.value(), notation);
    }

    @Override
    public void exclude(Dependency dependency) {
        Configuration mainConfig = configurations.getByName(runtimeClasspath.value());
        exclude(mainConfig, dependency);

        Configuration testConfig = configurations.getByName(testRuntimeClasspath.value());
        exclude(testConfig, dependency);
    }

    /**
     * Excludes the given dependency from the given configuration.
     *
     * @param configuration
     *         the configuration to exclude from
     * @param dependency
     *         the dependency to exclude
     */
    private static void exclude(Configuration configuration, Dependency dependency) {
        configuration.exclude(ImmutableMap.of(
                GROUP_KEY, dependency.groupId(),
                MODULE_KEY, dependency.name()
        ));
    }
}
