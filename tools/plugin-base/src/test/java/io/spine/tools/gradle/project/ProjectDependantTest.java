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

import io.spine.tools.gradle.Artifact;
import io.spine.tools.gradle.ConfigurationName;
import io.spine.tools.gradle.Dependency;
import io.spine.tools.gradle.ThirdPartyDependency;
import org.gradle.api.Project;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ExcludeRule;
import org.gradle.api.internal.artifacts.DefaultExcludeRule;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;

import java.nio.file.Path;
import java.util.Set;

import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.truth.Truth.assertThat;
import static io.spine.tools.gradle.ConfigurationName.compile;
import static io.spine.tools.gradle.ConfigurationName.implementation;
import static io.spine.tools.gradle.ConfigurationName.runtimeClasspath;
import static io.spine.tools.gradle.ConfigurationName.testRuntimeClasspath;

@ExtendWith(TempDirectory.class)
@DisplayName("DependantProject should")
class ProjectDependantTest {

    private Project project;

    @BeforeEach
    void setUp(@TempDir Path projectPath) {
        project = ProjectBuilder
                .builder()
                .withProjectDir(projectPath.toFile())
                .build();
        project.getPluginManager()
               .apply(JavaPlugin.class);
    }

    @Test
    @DisplayName("add a given dependency")
    void addDependency() {
        DependantProject container = DependantProject.from(project);
        Artifact dependency = artifact();
        container.depend(implementation, dependency.notation());

        checkDependency(implementation, dependency);
    }

    @Test
    @DisplayName("add an implementation dependency")
    void implementation() {
        DependantProject container = DependantProject.from(project);
        Artifact dependency = artifact();
        container.implementation(dependency.notation());

        checkDependency(implementation, dependency);
    }

    @SuppressWarnings("deprecation") // `compile` configuration.
    @Test
    @DisplayName("add a compile dependency")
    void compile() {
        DependantProject container = DependantProject.from(project);
        Artifact dependency = artifact();
        container.compile(dependency);

        checkDependency(compile, dependency);
    }

    @Test
    @DisplayName("exclude dependencies")
    void excludeDependencies() {
        DependantProject container = DependantProject.from(project);
        Dependency unwanted = new ThirdPartyDependency("org.example.system", "system-core");
        container.exclude(unwanted);

        checkExcluded(runtimeClasspath, unwanted);
        checkExcluded(testRuntimeClasspath, unwanted);
    }

    private void checkDependency(ConfigurationName configuration, Artifact dependency) {
        DependencySet dependencies = project.getConfigurations()
                                            .getByName(configuration.value())
                                            .getDependencies();
        assertThat(dependencies).hasSize(1);
        Artifact actualDependency = Artifact.from(getOnlyElement(dependencies));
        assertThat(actualDependency).isEqualTo(dependency);
    }

    private void checkExcluded(ConfigurationName fromConfiguration, Dependency unwanted) {
        Set<ExcludeRule> runtimeExclusionRules = project.getConfigurations()
                                                        .getByName(fromConfiguration.value())
                                                        .getExcludeRules();
        ExcludeRule excludeRule = new DefaultExcludeRule(unwanted.groupId(), unwanted.name());
        assertThat(runtimeExclusionRules).containsExactly(excludeRule);
    }

    private static Artifact artifact() {
        Artifact artifact = Artifact
                .newBuilder()
                .useSpineToolsGroup()
                .setName("test-artifact")
                .setVersion("42.0")
                .build();
        return artifact;
    }
}
