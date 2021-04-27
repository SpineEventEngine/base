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

package io.spine.tools.mc.java.gradle.check;

import com.google.common.annotations.VisibleForTesting;
import io.spine.logging.Logging;
import io.spine.tools.gradle.DependencyVersions;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.gradle.Artifact.TOOLS_GROUP;
import static io.spine.tools.gradle.ConfigurationName.annotationProcessor;

/**
 * Adds a {@code spine-mc-java-checks} dependency to the given project {@link Configuration}.
 */
final class DependencyConfig implements Logging {

    @VisibleForTesting
    static final String MODULE_CHECK_ARTIFACT_ID = "spine-mc-java-check";

    private final Configuration configuration;

    private DependencyConfig(Configuration cfg) {
        this.configuration = cfg;
    }

    /**
     * Create the {@code DependencyConfigurer} for the given project {@link Configuration}.
     *
     * @param cfg
     *         the configuration
     * @return the {@code DependencyConfigurer} instance
     */
    static DependencyConfig createFor(Configuration cfg) {
        checkNotNull(cfg);
        return new DependencyConfig(cfg);
    }

    /**
     * Adds the {@code io.spine.tools.spine-mc-java-checks} dependency to the project
     * configuration.
     *
     * <p>If the dependency cannot be resolved, the method does nothing and returns {@code false}.
     *
     * @return {@code true} if the dependency was resolved successfully and {@code false} otherwise
     */
    boolean addErrorProneChecksDependency() {
        DependencyVersions dependencyVersions = DependencyVersions.get();
        String version = dependencyVersions.spineBase();

        boolean isResolvable = isDependencyResolvable(version);
        if (isResolvable) {
            dependOnErrorProneChecks(version, configuration);
        }
        return isResolvable;
    }

    /**
     * Checks if the given {@code spine-mc-java-checks} dependency version is resolvable.
     *
     * <p>Uses the configuration copy because the configuration resolution is the irreversible
     * action that can be done only once for any given {@link Configuration}.
     */
    private boolean isDependencyResolvable(String version) {
        Configuration configCopy = configuration.copy();
        dependOnErrorProneChecks(version, configCopy);
        ResolvedConfiguration resolved = configCopy.getResolvedConfiguration();
        boolean isResolvable = !resolved.hasError();
        return isResolvable;
    }

    /**
     * Adds the {@code spine-erroprone-checks} dependency to the project configuration.
     */
    private void dependOnErrorProneChecks(String version, Configuration configuration) {
        _debug().log("Adding dependency on %s:%s:%s to the %s configuration.",
                     TOOLS_GROUP, MODULE_CHECK_ARTIFACT_ID, version,
                     annotationProcessor.value());
        DependencySet dependencies = configuration.getDependencies();
        Dependency dependency = new DefaultExternalModuleDependency(
                TOOLS_GROUP, MODULE_CHECK_ARTIFACT_ID, version);
        dependencies.add(dependency);
    }
}
