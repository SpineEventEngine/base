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
import com.google.common.flogger.FluentLogger;
import io.spine.logging.Logging;
import io.spine.tools.gradle.DependencyVersions;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.LenientConfiguration;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.artifacts.UnresolvedDependency;
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.gradle.Artifact.SPINE_TOOLS_GROUP;
import static io.spine.tools.gradle.ConfigurationName.annotationProcessor;

/**
 * Adds a {@code spine-mc-java-checks} dependency to the given project {@link Configuration}.
 */
public final class ConfigDependency implements Logging {

    /**
     * The name of the Maven artifact of the Model Compiler Java Checks.
     */
    @VisibleForTesting
    public static final String SPINE_MC_JAVA_CHECKS_ARTIFACT = "spine-mc-java-checks";

    /** The configuration to be extended. */
    private final Configuration configuration;

    /** If true, the extended configuration will be checked for errors by downloading its files. */
    private final boolean forceDownload;

    private ConfigDependency(Configuration cfg, boolean forceDownload) {
        this.configuration = cfg;
        this.forceDownload = forceDownload;
    }

    /**
     * Adds the dependency of the Spine Model Checks to the given configuration.
     *
     * @param project
     *         the project to which apply the dependency
     * @param forceDownload
     *          forces the download of the files which make up the given configuration before
     *          this method finishes
     * @return true if the configuration was applied
     */
    public static boolean applyTo(Project project, boolean forceDownload) {
        checkNotNull(project);
        Configuration preprocessorConfig = PreprocessorConfig.applyTo(project);
        ConfigDependency dep = new ConfigDependency(preprocessorConfig, forceDownload);
        boolean result = dep.addDependency();
        return result;
    }

    /**
     * Adds the dependency to the project configuration.
     *
     * @return {@code true} if the operation was successful, {@code false} otherwise
     * @see #forceDownload
     */
    private boolean addDependency() {
        DependencyVersions versions = DependencyVersions.get();
        String version = versions.spineBase();

        Configuration configCopy = addDependency(version);

        if (forceDownload) {
            boolean isResolvable = isResolvable(configCopy);
            return isResolvable;
        }
        return true;
    }

    private Configuration addDependency(String version) {
        Configuration configCopy = configuration.copy();
        addDependency(configCopy, version);
        return configCopy;
    }

    /**
     * Checks if the given configuration is resolvable.
     *
     * <p>Uses the configuration copy because the configuration resolution is the irreversible
     * action that can be done only once for any given {@link Configuration}.
     */
    private boolean isResolvable(Configuration configCopy) {
        if (forceDownload) {
            ResolvedConfiguration resolved = configCopy.getResolvedConfiguration();
            boolean hasErrors = !resolved.hasError();
            if (!hasErrors) {
                logUnresolvedFor(resolved);
            }
            return hasErrors;
        }
        return true;
    }

    private void logUnresolvedFor(ResolvedConfiguration resolved) {
        LenientConfiguration lenient = resolved.getLenientConfiguration();
        FluentLogger.Api error = _error();
        error.log("The configuration `%s` was not fully resolved.", resolved);
        Set<UnresolvedDependency> unresolved = lenient.getUnresolvedModuleDependencies();
        error.log("Unresolved dependencies: `%s`.", unresolved);
    }

    /**
     * Adds the dependency to the project configuration.
     */
    private void addDependency(Configuration cfg, String version) {
        _debug().log("Adding dependency on %s:%s:%s to the %s configuration.",
                     SPINE_TOOLS_GROUP, SPINE_MC_JAVA_CHECKS_ARTIFACT, version,
                     annotationProcessor);
        DependencySet dependencies = cfg.getDependencies();
        Dependency dependency = new DefaultExternalModuleDependency(
                SPINE_TOOLS_GROUP, SPINE_MC_JAVA_CHECKS_ARTIFACT, version);
        dependencies.add(dependency);
    }
}
