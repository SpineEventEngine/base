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

import static io.spine.tools.gradle.ConfigurationName.implementation;

/**
 * Manages the dependencies of a Gradle project.
 *
 * <p>Typically, represented by a {@link org.gradle.api.artifacts.dsl.DependencyHandler} of
 * the project.
 */
public interface Dependant {

    /**
     * Adds a new dependency within a given configuration.
     *
     * @param configurationName
     *         the name of the Gradle configuration
     * @param notation
     *         the dependency string, e.g. {@code "io.spine:spine-base:1.0.0"}
     */
    void depend(ConfigurationName configurationName, String notation);

    /**
     * Excludes the given dependency from the project.
     *
     * @param dependency
     *         the dependency to exclude, may refer to multiple artifacts with different versions,
     *         classifiers, etc.
     */
    void exclude(Dependency dependency);

    /**
     * Forces all project configurations to fetch the particular dependency version.
     *
     * @param artifact
     *         the artifact which represents a dependency resolved to the required version
     */
    void force(Artifact artifact);

    /**
     * Removes a forced dependency from resolution strategies of all project configurations.
     *
     * @param dependency
     *         the dependency to remove from the list of forced dependencies
     */
    void removeForcedDependency(Dependency dependency);

    /**
     * Adds a new dependency within the {@code compile} configuration.
     *
     * @see #compile(String)
     */
    default void compile(Artifact artifact) {
        compile(artifact.notation());
    }

    /**
     * Adds a new dependency within the {@code compile} configuration.
     *
     * <p>Though {@code compile} configuration is deprecated in Gradle, it is still used in order to
     * define Protobuf dependencies without re-generating the Java/JS sources from the upstream
     * Protobuf definitions.
     *
     * @see #depend(ConfigurationName, String)
     */
    @SuppressWarnings("deprecation") // See the doc.
    default void compile(String notation) {
        depend(ConfigurationName.compile, notation);
    }

    /**
     * Adds a new dependency within the {@code implementation} configuration.
     *
     * @see #depend(ConfigurationName, String)
     */
    default void implementation(String notation) {
        depend(implementation, notation);
    }
}
