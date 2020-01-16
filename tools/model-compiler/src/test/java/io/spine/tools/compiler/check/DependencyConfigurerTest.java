/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.tools.compiler.check;

import com.google.common.testing.NullPointerTester;
import io.spine.testing.SlowTest;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static io.spine.tools.compiler.check.DependencyConfigurer.SPINE_CHECKER_MODULE;
import static io.spine.tools.gradle.Artifact.SPINE_TOOLS_GROUP;
import static io.spine.tools.gradle.ConfigurationName.annotationProcessor;
import static io.spine.tools.gradle.compiler.given.Project.newProject;

/**
 * A test for the {@link DependencyConfigurer} part of the Spine Error Prone Checks plugin.
 *
 * @implNote This test configures the project with real dependencies and repositories which leads
 *         to a slow test execution. In future, it should be removed in favor of proper integration
 *         tests for the `spine-errorprone-checks` plugin.
 */
@SlowTest
@DisplayName("DependencyConfigurer should")
class DependencyConfigurerTest {

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester().testAllPublicStaticMethods(DependencyConfigurer.class);
        new NullPointerTester().testAllPublicInstanceMethods(createFor(newProject().get()));
    }

    @Test
    @DisplayName("add spine check dependency to annotation processor config")
    void addSpineCheckDependencyToAnnotationProcessorConfig() {
        Project project = newProject()
                .withMavenRepositories()
                .get();
        addDependency(project);

        boolean hasDependency = hasErrorProneChecksDependency(project);
        assertThat(hasDependency).isTrue();
    }

    @Test
    @DisplayName("not add spine check dependency if it is not resolvable")
    void notAddSpineCheckDependencyIfItIsNotResolvable() {
        Project project = newProject()
                .get();
        addDependency(project);

        boolean hasDependency = hasErrorProneChecksDependency(project);
        assertThat(hasDependency).isFalse();
    }

    @SuppressWarnings("CheckReturnValue")
        // We ignore boolean "success" flag which is not interesting for us in this test.
    private static void addDependency(Project project) {
        DependencyConfigurer configurer = createFor(project);
        configurer.addErrorProneChecksDependency();
    }

    private static DependencyConfigurer createFor(Project project) {
        Configuration annotationProcessorConfig = annotationProcessorConfig(project);
        DependencyConfigurer configurer =
                DependencyConfigurer.createFor(annotationProcessorConfig);
        return configurer;
    }

    private static boolean hasErrorProneChecksDependency(Project project) {
        Configuration config = annotationProcessorConfig(project);
        DependencySet dependencies = config.getDependencies();
        for (Dependency dependency : dependencies) {
            if (SPINE_TOOLS_GROUP.equals(dependency.getGroup()) &&
                    SPINE_CHECKER_MODULE.equals(dependency.getName())) {
                return true;
            }
        }
        return false;
    }

    private static Configuration annotationProcessorConfig(Project project) {
        ConfigurationContainer configs = project.getConfigurations();
        return configs.getByName(annotationProcessor.value());
    }
}
