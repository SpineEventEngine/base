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

import io.spine.testing.SlowTest;
import io.spine.tools.mc.java.gradle.given.StubProject;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.tools.gradle.Artifact.SPINE_TOOLS_GROUP;
import static io.spine.tools.mc.java.gradle.McJavaChecksDependency.SPINE_MC_JAVA_CHECKS_ARTIFACT;

/**
 * A test for the {@link McJavaChecksDependency} part of the Spine Error Prone Checks plugin.
 *
 * @implNote This test configures the project with real dependencies and repositories which leads
 *         to a slow test execution. In the future, it should be removed in favor of proper
 *         integration tests for the {@code mc-java-checks} plugin.
 */
@SlowTest
@DisplayName("`McJavaChecksDependency` should")
class McJavaChecksDependencyTest {

    /** The helper to configure a Gradle project. */
    private StubProject stubProject;

    @BeforeEach
    void createProject() {
        stubProject = StubProject.createFor(getClass());
    }

    @Test
    @DisplayName("add Spine Java Checks dependency to annotation processor config of a project")
    void addToProject() {
        Project project = stubProject.withMavenRepositories().get();

        boolean applied = McJavaChecksDependency.addTo(project);
        assertThat(applied)
                .isTrue();

        assertThat(hasMcJavaChecksDependencyIn(project))
                .isTrue();
    }

    @Test
    @DisplayName("not add Spine Java Checks dependency if it is not resolvable")
    void notAddIfNotResolvable() {
        Project project = stubProject.get();

        boolean applied = McJavaChecksDependency.addTo(project);
        assertThat(applied).isFalse();

        assertThat(hasMcJavaChecksDependencyIn(project))
                .isFalse();
    }

    private static boolean hasMcJavaChecksDependencyIn(Project project) {
        Configuration config = AnnotationProcessorConfiguration.in(project);
        DependencySet dependencies = config.getDependencies();
        for (Dependency d : dependencies) {
            if (SPINE_MC_JAVA_CHECKS_ARTIFACT.equals(d.getName())
                    && SPINE_TOOLS_GROUP.equals(d.getGroup())) {
                return true;
            }
        }
        return false;
    }
}
