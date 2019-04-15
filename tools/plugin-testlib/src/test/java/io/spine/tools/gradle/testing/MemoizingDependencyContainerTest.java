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

package io.spine.tools.gradle.testing;

import com.google.common.collect.ImmutableSet;
import io.spine.tools.gradle.Artifact;
import io.spine.tools.gradle.Dependency;
import io.spine.tools.gradle.ThirdPartyDependency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

@DisplayName("MemoizingDependencyContainer should")
class MemoizingDependencyContainerTest {

    private MemoizingDependencyContainer container;

    @BeforeEach
    void setUp() {
        container = new MemoizingDependencyContainer();
    }

    @Test
    @DisplayName("memoize a given dependency")
    void addDependency() {
        Artifact dependency = artifact();
        container.implementation(dependency.notation());

        checkDependency(dependency);
    }

    @Test
    @DisplayName("memoize a given exclusion rule")
    void addExclusion() {
        Dependency unwanted = dependency();
        container.exclude(unwanted);

        checkExcluded(unwanted);
    }

    private void checkDependency(Artifact dependency) {
        ImmutableSet<String> dependencies = container.dependencies();
        assertThat(dependencies).contains(dependency.notation());
    }

    private void checkExcluded(Dependency unwanted) {
        ImmutableSet<Dependency> exclusions = container.exclusions();
        assertThat(exclusions).contains(unwanted);
    }

    private static Dependency dependency() {
        return new ThirdPartyDependency("test.dependency", "test-dependency");
    }

    private static Artifact artifact() {
        return dependency().ofVersion("3.14");
    }
}
