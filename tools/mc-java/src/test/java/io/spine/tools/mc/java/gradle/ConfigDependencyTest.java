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

package io.spine.tools.mc.java.gradle;

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
import static io.spine.tools.mc.java.gradle.ConfigDependency.SPINE_MC_CHECKS_ARTIFACT;
import static io.spine.tools.gradle.Artifact.SPINE_TOOLS_GROUP;
import static io.spine.tools.gradle.ConfigurationName.annotationProcessor;
import static io.spine.tools.mc.java.gradle.given.Project.newProject;

/**
 * A test for the {@link ConfigDependency} part of the Spine Error Prone Checks plugin.
 *
 * @implNote This test configures the project with real dependencies and repositories which leads
 *         to a slow test execution. In future, it should be removed in favor of proper integration
 *         tests for the {@code mc-java-checks} plugin.
 */
@SlowTest
@DisplayName("`ConfigDependency` should")
class ConfigDependencyTest {

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester().testAllPublicStaticMethods(ConfigDependency.class);
    }

    @Test
    @DisplayName("add spine check dependency to annotation processor config")
    void addSpineCheckDependencyToAnnotationProcessorConfig() {
        Project project = newProject().withMavenRepositories().get();
        addDependency(project);

        boolean hasDependency = hasErrorProneChecksDependency(project);
        assertThat(hasDependency).isTrue();
    }

    @Test
    @DisplayName("not add spine check dependency if it is not resolvable")
    void notAddSpineCheckDependencyIfItIsNotResolvable() {
        Project project = newProject().get();
        addDependency(project);

        boolean hasDependency = hasErrorProneChecksDependency(project);
        assertThat(hasDependency).isFalse();
    }

    @SuppressWarnings("CheckReturnValue")
        // We ignore boolean "success" flag which is not interesting for us in this test.
    private static void addDependency(Project project) {
        Configuration annotationProcessorConfig = annotationProcessorConfig(project);
        ConfigDependency.applyTo(annotationProcessorConfig);
    }

    private static boolean hasErrorProneChecksDependency(Project project) {
        Configuration config = annotationProcessorConfig(project);
        DependencySet dependencies = config.getDependencies();
        for (Dependency dependency : dependencies) {
            if (SPINE_TOOLS_GROUP.equals(dependency.getGroup()) &&
                    SPINE_MC_CHECKS_ARTIFACT.equals(dependency.getName())) {
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
