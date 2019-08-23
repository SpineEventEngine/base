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

import com.google.common.base.Function;
import com.google.common.truth.Correspondence;
import io.spine.tools.gradle.Artifact;
import io.spine.tools.gradle.ConfigurationName;
import io.spine.tools.gradle.Dependency;
import io.spine.tools.gradle.ThirdPartyDependency;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ExcludeRule;
import org.gradle.api.artifacts.ModuleVersionSelector;
import org.gradle.api.internal.artifacts.DefaultExcludeRule;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static java.lang.String.format;

@SuppressWarnings("DuplicateStringLiteralInspection") // Test display names duplication.
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
        Dependency unwanted = dependency();
        container.exclude(unwanted);

        checkExcluded(runtimeClasspath, unwanted);
        checkExcluded(testRuntimeClasspath, unwanted);
    }

    @Test
    @DisplayName("force the dependency to resolve to a particular version")
    void forceDependency() {
        }

    @Nested
    @DisplayName("force the dependency to resolve to a particular version")
    class ForceDependency {

        @Test
        @DisplayName("when the dependency is represented as `Artifact`")
        void asArtifact() {
            DependantProject container = DependantProject.from(project);
            Artifact artifact = artifact();
            container.force(artifact);

            checkForced(artifact);
        }

        @Test
        @DisplayName("when the dependency is represented as `String` notation")
        void asString() {
            DependantProject container = DependantProject.from(project);
            Artifact artifact = artifact();
            String notation = artifact.notation();
            container.force(notation);

            checkForced(artifact);
        }

        private void checkForced(Artifact artifact) {
            ConfigurationContainer configurations = project.getConfigurations();
            configurations.forEach(config -> checkForced(config, artifact));
        }

        private void checkForced(Configuration config, Artifact artifact) {
            String notation = artifact.notation();
            Set<ModuleVersionSelector> forcedModules = config.getResolutionStrategy()
                                                             .getForcedModules();
            String description = "in string form equals to";
            Correspondence<ModuleVersionSelector, String> correspondence =
                    Correspondence.transforming(new ModuleVersionSelectorToNotation(), description);
            assertThat(forcedModules)
                    .comparingElementsUsing(correspondence)
                    .contains(notation);
        }
    }

    @Nested
    @DisplayName("remove a dependency from the forced dependencies list")
    class RemoveForcedDependency {

        @Test
        @DisplayName("when the dependency is represented as `Dependency`")
        void asDependency() {
            Dependency dependency = dependency();
            String version = version();
            forceDependency(dependency, version);

            DependantProject container = DependantProject.from(project);
            container.removeForcedDependency(dependency);

            checkNotForced();
        }

        @Test
        @DisplayName("when the dependency is represented as `String` notation")
        void asString() {
            Dependency dependency = dependency();
            String version = version();
            forceDependency(dependency, version);

            DependantProject container = DependantProject.from(project);
            String notation = dependency.ofVersion(version)
                                        .notation();
            container.removeForcedDependency(notation);

            checkNotForced();
        }

        private void forceDependency(Dependency dependency, String version) {
            String dependencyNotation = dependency.ofVersion(version)
                                                  .notation();
            project.getConfigurations()
                   .forEach(config -> config.getResolutionStrategy()
                                            .setForcedModules(dependencyNotation));
        }

        private void checkNotForced() {
            project.getConfigurations()
                   .forEach(ProjectDependantTest::checkForcedModulesEmpty);
        }
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

    private static void checkForcedModulesEmpty(Configuration config) {
        Set<ModuleVersionSelector> forcedModules = config.getResolutionStrategy()
                                                         .getForcedModules();
        assertThat(forcedModules).isEmpty();
    }

    private static Dependency dependency() {
        Dependency dependency = new ThirdPartyDependency("org.example.system", "system-core");
        return dependency;
    }

    private static String version() {
        String version = "1.15.12";
        return version;
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

    private static class ModuleVersionSelectorToNotation
            implements Function<ModuleVersionSelector, String> {

        @Override
        public String apply(ModuleVersionSelector selector) {
            String notation = format(
                    "%s:%s:%s", selector.getGroup(), selector.getName(), selector.getVersion()
            );
            return notation;
        }
    }
}
