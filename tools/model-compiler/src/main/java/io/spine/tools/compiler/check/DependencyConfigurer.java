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

package io.spine.tools.compiler.check;

import com.google.common.annotations.VisibleForTesting;
import io.spine.logging.Logging;
import io.spine.tools.gradle.DependencyVersions;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.gradle.Artifact.SPINE_TOOLS_GROUP;
import static io.spine.tools.gradle.ConfigurationName.annotationProcessor;

/**
 * Adds a {@code spine-errorprone-checks} dependency to the given project {@link Configuration}.
 */
public final class DependencyConfigurer implements Logging {

    @VisibleForTesting
    static final String SPINE_CHECKER_MODULE = "spine-errorprone-checks";

    private final Configuration configuration;

    private DependencyConfigurer(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Create the {@code DependencyConfigurer} for the given project {@link Configuration}.
     *
     * @param configuration
     *         the configuration
     * @return the {@code DependencyConfigurer} instance
     */
    public static DependencyConfigurer createFor(Configuration configuration) {
        checkNotNull(configuration);
        return new DependencyConfigurer(configuration);
    }

    /**
     * Adds the {@code io.spine.tools.spine-errorprone-checks} dependency to the project
     * configuration.
     *
     * <p>If the dependency cannot be resolved, the method does nothing and returns {@code false}.
     *
     * @return {@code true} if the dependency was resolved successfully and {@code false} otherwise
     */
    public boolean addErrorProneChecksDependency() {
        DependencyVersions dependencyVersions = DependencyVersions.get();
        String version = dependencyVersions.spineBase();

        boolean isResolvable = isDependencyResolvable(version);
        if (isResolvable) {
            dependOnErrorProneChecks(version, configuration);
        }
        return isResolvable;
    }

    /**
     * Checks if the given {@code spine-errorprone-checks} dependency version is resolvable.
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
                    SPINE_TOOLS_GROUP, SPINE_CHECKER_MODULE, version,
                    annotationProcessor.value());
        DependencySet dependencies = configuration.getDependencies();
        Dependency dependency = new DefaultExternalModuleDependency(
                SPINE_TOOLS_GROUP, SPINE_CHECKER_MODULE, version);
        dependencies.add(dependency);
    }
}
