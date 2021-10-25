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

import com.google.common.annotations.VisibleForTesting;
import io.spine.logging.Logging;
import io.spine.tools.gradle.DependencyVersions;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.component.ComponentSelector;
import org.gradle.api.artifacts.result.DependencyResult;
import org.gradle.api.artifacts.result.ResolutionResult;
import org.gradle.api.artifacts.result.UnresolvedDependencyResult;
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.gradle.Artifact.SPINE_TOOLS_GROUP;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Adds a {@code spine-mc-java-checks} dependency to the given project {@link Configuration}.
 */
public final class McJavaChecksDependency implements Logging {

    /**
     * The name of the Maven artifact of the Model Compiler Java Checks.
     */
    @VisibleForTesting
    public static final String SPINE_MC_JAVA_CHECKS_ARTIFACT = "spine-mc-java-checks";

    /** The configuration to be extended. */
    private final Configuration configuration;

    /** The version of the dependency, which is the same as one for {@code spine-base}. */
    private final String version;

    /** The dependency to be added. */
    private final Dependency dependency;

    private McJavaChecksDependency(Configuration cfg) {
        this.configuration = cfg;
        DependencyVersions versions = DependencyVersions.get();
        this.version = versions.spineBase();
        this.dependency = checksDependency();
    }

    /**
     * Adds the dependency of the Spine Model Checks to the given configuration.
     *
     * @param project
     *         the project to which apply the dependency
     * @return true if the configuration was applied
     */
    public static boolean addTo(Project project) {
        checkNotNull(project);
        Configuration cfg = AnnotationProcessorConfiguration.findOrCreateIn(project);
        McJavaChecksDependency dep = new McJavaChecksDependency(cfg);
        boolean result = dep.addDependency();
        return result;
    }

    /**
     * Adds the dependency to the project configuration.
     *
     * @return {@code true} if the operation was successful, {@code false} otherwise
     */
    private boolean addDependency() {
        ResolutionHelper helper = new ResolutionHelper();
        if (helper.wasResolved()) {
            addDependencyTo(configuration);
            return true;
        } else {
            helper.logUnresolved();
            return false;
        }
    }

    /**
     * Adds the dependency to the project configuration.
     */
    private void addDependencyTo(Configuration cfg) {
        _debug().log("Adding a dependency on `%s` to the `%s` configuration.", artifactId(), cfg);
        DependencySet dependencies = cfg.getDependencies();
        dependencies.add(dependency);
    }

    private String artifactId() {
        return format("%s:%s:%s", SPINE_TOOLS_GROUP, SPINE_MC_JAVA_CHECKS_ARTIFACT, version);
    }

    private DefaultExternalModuleDependency checksDependency() {
        return new DefaultExternalModuleDependency(
                SPINE_TOOLS_GROUP, SPINE_MC_JAVA_CHECKS_ARTIFACT, version
        );
    }

    /**
     * Assists with checking if the dependency can be resolved, and if not, helps with
     * logging error diagnostics.
     */
    private class ResolutionHelper {

        private final ResolutionResult resolutionResult;
        private @Nullable UnresolvedDependencyResult unresolved;

        private ResolutionHelper() {
            Configuration configCopy = configuration.copy();
            addDependencyTo(configCopy);
            resolutionResult =
                    configCopy.getIncoming()
                              .getResolutionResult();
        }

        /**
         * Verifies if the {@link #dependency} to be added was resolved, returning {@code true}
         * if so.
         *
         * <p>If the {@link #dependency} was not resolved, the corresponding
         * {@link UnresolvedDependencyResult} is {@linkplain #unresolved stored} for
         * future logging needs.
         */
        private boolean wasResolved() {
            Set<? extends DependencyResult> allDeps = resolutionResult.getAllDependencies();
            String group = requireNonNull(dependency.getGroup());
            String name = dependency.getName();
            for (DependencyResult dep : allDeps) {
                if (dep instanceof UnresolvedDependencyResult) {
                    UnresolvedDependencyResult unresolved = (UnresolvedDependencyResult) dep;
                    ComponentSelector attempted = unresolved.getAttempted();
                    String displayName = attempted.getDisplayName();
                    if (displayName.contains(group) && displayName.contains(name)) {
                        this.unresolved = unresolved;
                        return false;
                    }
                }
            }
            return true;
        }

        private void logUnresolved() {
            String problemReport = toErrorMessage(requireNonNull(unresolved));
            _warn().log(
                    "Unable to add a dependency on `%s` to the configuration `%s` because some " +
                            "dependencies could not be resolved: " +
                            "%s.",
                    artifactId(), configuration.getName(), problemReport
            );
        }

        private String toErrorMessage(UnresolvedDependencyResult entry) {
            String dependency = entry.getAttempted().getDisplayName();
            Throwable throwable = entry.getFailure();
            return format("%nDependency: `%s`%nProblem: `%s`", dependency, throwable);
        }
    }
}
