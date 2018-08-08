/*
 * Copyright 2018, TeamDev. All rights reserved.
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
package io.spine.tools.gradle.compiler;

import com.google.common.annotations.VisibleForTesting;
import io.spine.tools.gradle.SpinePlugin;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.initialization.dsl.ScriptHandler;
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.JavaCompile;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static io.spine.tools.gradle.ConfigurationName.CLASSPATH;
import static io.spine.tools.gradle.compiler.SpineCheckExtension.getUseValidatingBuilder;

/**
 * A Gradle plugin which configures the project to run Spine checks during compilation stage.
 *
 * <p>To work, this plugin requires <a href="https://github.com/tbroyer/gradle-errorprone-plugin">
 * the Error Prone plugin</a> applied to the project, as the Spine checks are based on the Error
 * Prone custom checks.
 *
 * <p>The plugin adds a {@code spine-checker} dependency to the project's
 * {@code annotationProcessor} configuration. For the older Gradle versions (pre {@code 4.6}),
 * where there is no such configuration, the plugin creates it.
 *
 * <p>Dependency has the same version as the project's {@code spine-model-compiler} plugin
 * dependency.
 *
 * <p>Checks severity may be configured for all checks:
 *
 * <pre>
 * {@code
 *
 *   modelCompiler {
 *      spineCheckSeverity = "OFF"
 *   }
 * }
 * </pre>
 * or for the specific ones:
 *
 *  <pre>
 * {@code
 *
 *   spineChecker {
 *      useValidatingBuilder = "ERROR"
 *   }
 * }
 * </pre>
 *
 * <p>The latter overrides the former.
 *
 * @author Dmytro Kuzmin
 * @see io.spine.validate.ValidatingBuilder
 */
public class SpineCheckerPlugin extends SpinePlugin {

    private static final String EXTENSION_NAME = "spineCheck";
    private static final String ERROR_PRONE_PLUGIN_ID = "net.ltgt.errorprone";

    @VisibleForTesting
    static final String PREPROCESSOR_CONFIG_NAME = "annotationProcessor";

    @VisibleForTesting
    static final String SPINE_TOOLS_GROUP = "io.spine.tools";

    @VisibleForTesting
    static final String SPINE_CHECKER_MODULE = "spine-checker";

    @VisibleForTesting
    static final String MODEL_COMPILER_PLUGIN_NAME = "spine-model-compiler";

    static String extensionName() {
        return EXTENSION_NAME;
    }

    /**
     * Applies the plugin to the given {@code Project}.
     *
     * @param project the project to apply the plugin to
     */
    @Override
    public void apply(Project project) {
        project.getExtensions()
               .create(extensionName(), SpineCheckExtension.class);
        Configuration preprocessorConfig = setupPreprocessorConfig(project);
        boolean dependencyResolved = addSpineCheckerDependency(preprocessorConfig, project);
        if (!dependencyResolved) {
            return;
        }
        addConfigurePreprocessorAction(preprocessorConfig, project);
        addConfigureSeverityAction(project);
    }

    /**
     * Creates the {@code annotationProcessor} config for the project if it does not exist.
     *
     * <p>In the newer Gradle versions ({@code 4.6} and above) the config most probably already
     * exists.
     *
     * @param project the project
     * @return the {@code annotationProcessor} configuration of the project
     */
    private Configuration setupPreprocessorConfig(Project project) {
        ConfigurationContainer configurations = project.getConfigurations();
        Configuration preprocessorConfig = configurations.findByName(PREPROCESSOR_CONFIG_NAME);
        if (preprocessorConfig == null) {
            log().debug("Creating preprocessor configuration for the project {}",
                        project.getName());
            preprocessorConfig = configurations.create(PREPROCESSOR_CONFIG_NAME);
        }
        return preprocessorConfig;
    }

    /**
     * Adds the {@code io.spine.tools.spine-checker} dependency to the specified configuration of
     * the specified project.
     *
     * <p>The version of the dependency used is the same as the version of the
     * {@code spine-model-compiler} plugin used by the project.
     *
     * <p>If the {@code spine-model-compiler} version cannot be acquired or the
     * {@code spine-checker} version is not resolvable, the method does nothing and returns
     * {@code false}.
     *
     * @param configuration the configuration to add the dependency to
     * @param project       the project to which this configuration belongs
     * @return {@code true} if the dependency was resolved successfully and {@code false} otherwise
     */
    private boolean addSpineCheckerDependency(Configuration configuration, Project project) {
        Optional<String> versionToUse = acquireModelCompilerVersion(project);
        if (!versionToUse.isPresent()) {
            log().debug("Can't acquire model compiler version for the project {}",
                        project.getName());
            return false;
        }
        String version = versionToUse.get();

        boolean isResolvable = isSpineCheckerVersionResolvable(version, configuration);
        if (isResolvable) {
            dependOnSpineChecker(version, configuration);
        }
        return isResolvable;
    }

    /**
     * Makes sure the preprocessor is configured for all the {@code JavaCompile} tasks of the
     * project.
     *
     * <p>The action is executed on the {@code projectEvaluated} stage.
     */
    @VisibleForTesting
    protected void addConfigurePreprocessorAction(Configuration preprocessorConfig, Project project) {
        Action<Gradle> configurePreprocessor =
                configurePreprocessorAction(preprocessorConfig, project);
        Gradle gradle = project.getGradle();
        gradle.projectsEvaluated(configurePreprocessor);
    }

    /**
     * Adds the action configuring Spine checks severity to the {@code projectEvaluated} stage of
     * the project.
     *
     * @param project the project for which to configure the severity
     */
    private void addConfigureSeverityAction(Project project) {
        Action<Gradle> configureCheckSeverity = configureSeverityAction(project);
        Gradle gradle = project.getGradle();
        gradle.projectsEvaluated(configureCheckSeverity);
    }

    /**
     * Gets the {@code spine-model-compiler} dependency version from the
     * {@code project.buildsript.classpath} configuration.
     *
     * <p>If the dependency version is not found, returns {@link Optional#EMPTY}.
     */
    private Optional<String> acquireModelCompilerVersion(Project project) {
        log().debug("Acquiring 'spine-model-compiler' dependency version for the project {}",
                    project.getName());

        ScriptHandler buildscript = project.getRootProject()
                                           .getBuildscript();
        ConfigurationContainer configurations = buildscript.getConfigurations();
        Configuration classpath = configurations.findByName(CLASSPATH.getValue());

        if (classpath == null) {
            return Optional.empty();
        }

        DependencySet classpathDependencies = classpath.getDependencies();
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
     * Checks if the given {@code spine-checker} dependency is resolvable for the given
     * configuration.
     *
     * <p>Uses the configuration copy to not resolve the given configuration itself.
     */
    @VisibleForTesting
    protected boolean isSpineCheckerVersionResolvable(String version, Configuration configuration) {
        Configuration configCopy = configuration.copy();
        dependOnSpineChecker(version, configCopy);
        ResolvedConfiguration resolved = configCopy.getResolvedConfiguration();
        boolean isResolvable = !resolved.hasError();
        return isResolvable;
    }

    /**
     * Adds the {@code spine-checker} dependency to the given configuration.
     */
    private void dependOnSpineChecker(String dependencyVersion, Configuration configuration) {
        log().debug("Adding dependency on {}:{}:{} to the {} configuration",
                    SPINE_TOOLS_GROUP, SPINE_CHECKER_MODULE, dependencyVersion,
                    PREPROCESSOR_CONFIG_NAME);
        DependencySet dependencies = configuration.getDependencies();
        Dependency dependency = new DefaultExternalModuleDependency(
                SPINE_TOOLS_GROUP, SPINE_CHECKER_MODULE, dependencyVersion);
        dependencies.add(dependency);
    }

    /**
     * Converts the {@link #configurePreprocessor(Configuration, Project)} method to the Gradle
     * {@link Action}.
     */
    private Action<Gradle>
    configurePreprocessorAction(Configuration preprocessorConfig, Project project) {
        return gradle -> configurePreprocessor(preprocessorConfig, project);
    }

    /**
     * Adds the given preprocessor configuration as the preprocessor path to all {@code JavaCompile}
     * tasks of the project.
     */
    @SuppressWarnings("TypeMayBeWeakened") // More specific type expresses the method intent better.
    private void configurePreprocessor(Configuration preprocessorConfig, Project project) {
        log().debug("Adding the {} configuration to all 'JavaCompile' tasks.",
                    PREPROCESSOR_CONFIG_NAME);
        addArgsToJavaCompile(project, "-processorpath", preprocessorConfig.getAsPath());
    }

    /**
     * Converts the {@link #configureCheckSeverity(Project)} method to the Gradle {@link Action}.
     */
    private Action<Gradle> configureSeverityAction(Project project) {
        return gradle -> configureCheckSeverity(project);
    }

    /**
     * Adds command line flags necessary to configure Spine check severities to all
     * {@code JavaCompile} tasks of the project.
     */
    private void configureCheckSeverity(Project project) {
        if (!hasErrorPronePlugin(project)) {
            log().debug("Cannot configure Spine checks severity as Error Prone plugin is not " +
                                "applied to the project {}.", project.getName());
            return;
        }
        Severity defaultSeverity = Extension.getSpineCheckSeverity(project);
        configureUseValidatingBuilder(project, defaultSeverity);
    }

    /**
     * Configures the "UseValidatingBuilder" check severity for all {@code JavaCompile} tasks of
     * the project.
     *
     * <p>Uses default severity set in the {@code modelCompiler} extension if set and not
     * overridden by the more specific {@code spineChecker} extension.
     */
    @SuppressWarnings("ConstantConditions") // Checking nullable argument for null.
    private void
    configureUseValidatingBuilder(Project project, @Nullable Severity defaultSeverity) {
        Severity severity = getUseValidatingBuilder(project);
        if (severity == null) {
            if (defaultSeverity == null) {
                return;
            } else {
                severity = defaultSeverity;
            }
        }
        log().debug("Setting UseValidatingBuilder checker severity to {} for the project {}",
                    severity.name(), project.getName());
        String severityArg = "-Xep:UseValidatingBuilder:" + severity.name();
        addArgsToJavaCompile(project, severityArg);
    }

    /**
     * Adds specified arguments to all {@code JavaCompile} tasks of the project.
     */
    private static void addArgsToJavaCompile(Project project, String... args) {
        TaskContainer tasks = project.getTasks();
        TaskCollection<JavaCompile> javaCompileTasks = tasks.withType(JavaCompile.class);
        for (JavaCompile task : javaCompileTasks) {
            CompileOptions taskOptions = task.getOptions();
            List<String> compilerArgs = taskOptions.getCompilerArgs();
            compilerArgs.addAll(Arrays.asList(args));
        }
    }

    /**
     * Checks if the project has Error Prone plugin applied.
     */
    @VisibleForTesting
    protected boolean hasErrorPronePlugin(Project project) {
        PluginContainer appliedPlugins = project.getPlugins();
        boolean hasPlugin = appliedPlugins.hasPlugin(ERROR_PRONE_PLUGIN_ID);
        return hasPlugin;
    }
}
