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

import static io.spine.tools.gradle.compiler.SpineCheckerExtension.getUseVBuilder;

public class SpineCheckerPlugin extends SpinePlugin {

    private static final String ERROR_PRONE_PLUGIN_ID = "net.ltgt.errorprone";
    private static final String EXTENSION_NAME = "spineChecker";

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

    @Override
    public void apply(Project project) {
        project.getExtensions()
               .create(extensionName(), SpineCheckerExtension.class);
        Configuration preprocessorConfig = setupPreprocessorConfig(project);
        boolean addedSuccessfully = addSpineCheckerDependency(preprocessorConfig, project);
        if (addedSuccessfully) {
            addConfigureSeverityAction(project);
        }
    }

    private Configuration setupPreprocessorConfig(Project project) {
        ConfigurationContainer configurations = project.getConfigurations();
        Configuration preprocessorConfig = configurations.findByName(PREPROCESSOR_CONFIG_NAME);
        if (preprocessorConfig == null) {
            preprocessorConfig = configurations.create(PREPROCESSOR_CONFIG_NAME);
            addConfigurePreprocessorAction(preprocessorConfig, project);
        }
        return preprocessorConfig;
    }

    private boolean addSpineCheckerDependency(Configuration configuration, Project project) {
        Optional<String> versionToUse = acquireModelCompilerVersion(project);
        if (!versionToUse.isPresent()) {
            log().debug("Can't determine which dependency version to use for the project {}",
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

    private void addConfigureSeverityAction(Project project) {
        Action<Gradle> configureCheckSeverity = configureSeverityAction(project);
        Gradle gradle = project.getGradle();
        gradle.projectsEvaluated(configureCheckSeverity);
    }

    private void addConfigurePreprocessorAction(Configuration preprocessorConfig, Project project) {
        Action<Gradle> configurePreprocessor =
                configurePreprocessorAction(preprocessorConfig, project);
        Gradle gradle = project.getGradle();
        gradle.projectsEvaluated(configurePreprocessor);
    }

    private Optional<String> acquireModelCompilerVersion(Project project) {
        log().debug("Acquiring 'spine-model-compiler' dependency version for the project {}",
                    project.getName());

        ScriptHandler buildscript = project.getRootProject()
                                           .getBuildscript();
        ConfigurationContainer configurations = buildscript.getConfigurations();
        Configuration classpath = configurations.findByName("classpath");

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

    @VisibleForTesting
    protected boolean isSpineCheckerVersionResolvable(String version, Configuration configuration) {
        Configuration configCopy = configuration.copy();
        dependOnSpineChecker(version, configCopy);
        ResolvedConfiguration resolved = configCopy.getResolvedConfiguration();
        boolean isResolvable = !resolved.hasError();
        return isResolvable;
    }

    private void dependOnSpineChecker(String dependencyVersion, Configuration configuration) {
        log().debug("Adding dependency on {}:{}:{} to the {} configuration",
                    SPINE_TOOLS_GROUP, SPINE_CHECKER_MODULE, dependencyVersion,
                    PREPROCESSOR_CONFIG_NAME);
        DependencySet dependencies = configuration.getDependencies();
        Dependency dependency = new DefaultExternalModuleDependency(
                SPINE_TOOLS_GROUP, SPINE_CHECKER_MODULE, dependencyVersion);
        dependencies.add(dependency);
    }

    private Action<Gradle>
    configurePreprocessorAction(Configuration preprocessorConfig, Project project) {
        return gradle -> configurePreprocessor(preprocessorConfig, project);
    }

    private void configurePreprocessor(Configuration preprocessorConfig, Project project) {
        log().debug("Adding the {} configuration to all 'JavaCompile' tasks.",
                    PREPROCESSOR_CONFIG_NAME);
        addArgsToJavaCompile(project, "-processorpath", preprocessorConfig.getAsPath());
    }

    private Action<Gradle> configureSeverityAction(Project project) {
        return gradle -> configureCheckSeverity(project);
    }

    private void configureCheckSeverity(Project project) {
        if (!hasErrorPronePlugin(project)) {
            log().debug("Cannot configure Spine checks severity as Error Prone plugin is not " +
                                "applied to the project {}.", project.getName());
            return;
        }
        Severity defaultSeverity = Extension.getSpineCheckSeverity(project);
        setUseVBuilderSeverity(project, defaultSeverity);
    }

    @SuppressWarnings("ConstantConditions") // The condition is not really constant.
    private void setUseVBuilderSeverity(Project project, @Nullable Severity defaultSeverity) {
        Severity severity = getUseVBuilder(project);
        if (severity == null) {
            if (defaultSeverity == null) {
                return;
            } else {
                severity = defaultSeverity;
            }
        }
        log().debug("Setting UseVBuilder check severity to {} for the project {}",
                    severity.name(), project.getName());
        String severityArg = "-Xep:UseVBuilder:" + severity.name();
        addArgsToJavaCompile(project, severityArg);
    }

    private static void addArgsToJavaCompile(Project project, String... args) {
        TaskContainer tasks = project.getTasks();
        TaskCollection<JavaCompile> javaCompileTasks = tasks.withType(JavaCompile.class);
        for (JavaCompile task : javaCompileTasks) {
            CompileOptions taskOptions = task.getOptions();
            List<String> compilerArgs = taskOptions.getCompilerArgs();
            compilerArgs.addAll(Arrays.asList(args));
        }
    }

    @VisibleForTesting
    protected boolean hasErrorPronePlugin(Project project) {
        PluginContainer appliedPlugins = project.getPlugins();
        boolean hasPlugin = appliedPlugins.hasPlugin(ERROR_PRONE_PLUGIN_ID);
        return hasPlugin;
    }
}
