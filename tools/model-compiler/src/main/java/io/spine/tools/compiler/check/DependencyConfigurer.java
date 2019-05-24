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
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.initialization.dsl.ScriptHandler;
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.gradle.Artifact.SPINE_TOOLS_GROUP;
import static io.spine.tools.gradle.ConfigurationName.annotationProcessor;
import static io.spine.tools.gradle.ConfigurationName.classpath;

/**
 * Class which helps managing dependencies related to the Spine Error Prone Checks module and the
 * {@link io.spine.tools.gradle.compiler.ErrorProneChecksPlugin}.
 *
 * <p>The class manages dependencies of the given {@link Configuration} for the given
 * {@link Project}.
 */
public class DependencyConfigurer {

    private static final String MODEL_COMPILER_PLUGIN_NAME = "spine-model-compiler";

    @VisibleForTesting
    static final String SPINE_CHECKER_MODULE = "spine-errorprone-checks";

    private final Project project;
    private final Configuration configuration;

    private DependencyConfigurer(Project project, Configuration configuration) {
        this.project = project;
        this.configuration = configuration;
    }

    /**
     * Create the {@code DependencyConfigurer} for the given project and configuration.
     *
     * @param project       the project
     * @param configuration the configuration
     * @return the {@code DependencyConfigurer} instance
     */
    public static DependencyConfigurer createFor(Project project, Configuration configuration) {
        checkNotNull(project);
        checkNotNull(configuration);
        return new DependencyConfigurer(project, configuration);
    }

    /**
     * Adds the {@code io.spine.tools.spine-errorprone-checks} dependency to the project
     * configuration.
     *
     * <p>The version of the dependency used is the same as the version of the
     * {@code spine-model-compiler} plugin used by the project.
     *
     * <p>If the {@code spine-model-compiler} version cannot be acquired or the
     * {@code spine-errorprone-checks} version is not resolvable, the method does nothing and
     * returns {@code false}.
     *
     * @return {@code true} if the dependency was resolved successfully and {@code false} otherwise
     */
    public boolean addErrorProneChecksDependency() {
        Optional<String> versionToUse = acquireModelCompilerVersion();
        if (!versionToUse.isPresent()) {
            log().debug("Can't acquire model compiler version for the project {}",
                        project.getName());
            return false;
        }
        String version = versionToUse.get();

        boolean isResolvable = isChecksVersionResolvable(version);
        if (isResolvable) {
            dependOnErrorProneChecks(version, configuration);
        }
        return isResolvable;
    }

    /**
     * Gets the {@code spine-model-compiler} dependency version from the
     * {@code project.buildsript.classpath} configuration.
     *
     * <p>If the dependency version is not found, returns {@link Optional#empty()}.
     */
    @VisibleForTesting
    Optional<String> acquireModelCompilerVersion() {
        ScriptHandler buildscript = project.getRootProject()
                                           .getBuildscript();
        ConfigurationContainer configurations = buildscript.getConfigurations();
        Configuration classpathConfig = configurations.findByName(classpath.value());
        if (classpathConfig == null) {
            return Optional.empty();
        }
        DependencySet classpathDependencies = classpathConfig.getDependencies();
        Optional<String> version = Optional.empty();
        for (Dependency dependency : classpathDependencies) {
            if (MODEL_COMPILER_PLUGIN_NAME.equals(dependency.getName())) {
                String dependencyVersion = dependency.getVersion();
                version = Optional.ofNullable(dependencyVersion);
            }
        }
        return version;
    }

    /**
     * Checks if the given {@code spine-errorprone-checks} dependency is resolvable for the project
     * configuration.
     *
     * <p>Uses the configuration copy to not resolve the given configuration itself.
     */
    @VisibleForTesting
    boolean isChecksVersionResolvable(String version) {
        Configuration configCopy = configuration.copy();
        dependOnErrorProneChecks(version, configCopy);
        ResolvedConfiguration resolved = configCopy.getResolvedConfiguration();
        boolean isResolvable = !resolved.hasError();
        return isResolvable;
    }

    /**
     * Adds the {@code spine-erroprone-checks} dependency to the project configuration.
     */
    private static void dependOnErrorProneChecks(String version, Configuration configuration) {
        log().debug("Adding dependency on {}:{}:{} to the {} configuration",
                    SPINE_TOOLS_GROUP, SPINE_CHECKER_MODULE, version,
                    annotationProcessor.value());
        DependencySet dependencies = configuration.getDependencies();
        Dependency dependency = new DefaultExternalModuleDependency(
                SPINE_TOOLS_GROUP, SPINE_CHECKER_MODULE, version);
        dependencies.add(dependency);
    }

    /**
     * Obtains the SLF4J logger specific to this class.
     *
     * @return the logger for this class
     */
    private static Logger log() {
        return LoggerFactory.getLogger(DependencyConfigurer.class);
    }
}
