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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.spine.logging.Logging;
import io.spine.tools.gradle.DependencyVersions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ModuleIdentifier;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.result.DependencyResult;
import org.gradle.api.artifacts.result.ResolutionResult;
import org.gradle.api.artifacts.result.ResolvedComponentResult;
import org.gradle.api.artifacts.result.ResolvedDependencyResult;
import org.gradle.api.artifacts.result.UnresolvedDependencyResult;
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency;

import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static io.spine.tools.gradle.Artifact.SPINE_TOOLS_GROUP;
import static java.lang.String.format;

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

    /**
     * The result of the attempt to add the dependency to the configuration.
     *
     * @see #isResolvable()
     */
    private @Nullable ResolutionResult resolutionResult;

    private McJavaChecksDependency(Configuration cfg) {
        this.configuration = cfg;
        DependencyVersions versions = DependencyVersions.get();
        this.version = versions.spineBase();
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
        if (isResolvable()) {
            addDependencyTo(configuration);
            return true;
        } else {
            logUnresolvedDependencies();
            return false;
        }
    }

    /**
     * Checks if the given configuration is resolvable.
     *
     * <p>Uses the copy of the passed configuration because the configuration resolution
     * is the irreversible action that can be done only once for any given {@link Configuration}.
     */
    private boolean isResolvable() {
        Configuration configCopy = configuration.copy();
        addDependencyTo(configCopy);
        resolutionResult = configCopy.getIncoming()
                                     .getResolutionResult();
        ModuleIdentifier checksModule = checksDependency().getModule();
        boolean wasResolved =
                resolutionResult.getAllDependencies()
                                .stream()
                                .filter(resolutionResult -> resolutionResult instanceof ResolvedDependencyResult)
                                .map(DependencyResult::getFrom)
                                .map(ResolvedComponentResult::getModuleVersion)
                                .filter(Objects::nonNull)
                                .map(ModuleVersionIdentifier::getModule)
                                .anyMatch(checksModule::equals);
        return wasResolved;
    }

    private String artifactId() {
        return format("%s:%s:%s", SPINE_TOOLS_GROUP, SPINE_MC_JAVA_CHECKS_ARTIFACT, version);
    }

    /**
     * Adds the dependency to the project configuration.
     */
    private void addDependencyTo(Configuration cfg) {
        _debug().log("Adding dependency on `%s` to the `%s` configuration.", artifactId(), cfg);
        DependencySet dependencies = cfg.getDependencies();
        Dependency dependency = checksDependency();
        dependencies.add(dependency);
    }

    @NonNull
    private DefaultExternalModuleDependency checksDependency() {
        return new DefaultExternalModuleDependency(
                SPINE_TOOLS_GROUP, SPINE_MC_JAVA_CHECKS_ARTIFACT, version);
    }

    private void logUnresolvedDependencies() {
        checkState(resolutionResult != null);

        ImmutableList<UnresolvedDependencyResult> unresolvedDeps =
                resolutionResult.getAllDependencies()
                                .stream()
                                .filter(UnresolvedDependencyResult.class::isInstance)
                                .map(UnresolvedDependencyResult.class::cast)
                                .collect(toImmutableList());

        ImmutableMap.Builder<String, Throwable> builder = ImmutableMap.builder();
        for (UnresolvedDependencyResult res : unresolvedDeps) {
            String component = res.getAttempted().getDisplayName();
            Throwable failure = res.getFailure();
            builder.put(component, failure);
        }
        ImmutableMap<String, Throwable> diags = builder.build();

        ImmutableList<String> problemReport =
                diags.entrySet()
                     .stream()
                     .map(McJavaChecksDependency::toErrorMessage)
                     .sorted()
                     .collect(toImmutableList());
        _warn().log(
                "Unable to add dependency on `%s` to the configuration `%s` because some " +
                        "dependencies could not be resolved: " +
                        "%s.",
                artifactId(), configuration.getName(), problemReport
        );
    }

    private static String toErrorMessage(Map.Entry<String, Throwable> entry) {
        String dependency = entry.getKey();
        Throwable throwable = entry.getValue();
        return format("%nDependency: `%s`%nProblem: `%s`", dependency, throwable);
    }
}
