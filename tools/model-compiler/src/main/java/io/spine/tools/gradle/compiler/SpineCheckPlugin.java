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

import io.spine.tools.gradle.SpinePlugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.initialization.dsl.ScriptHandler;
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.JavaCompile;

import java.util.List;
import java.util.Optional;

import static io.spine.tools.gradle.compiler.SpineCheckExtension.getUseVBuilderSeverity;

public class SpineCheckPlugin extends SpinePlugin {

    private static final String SPINE_CHECKS_MODULE = "spine-custom-checks";
    private static final String EXTENSION_NAME = "spineCheck";

    public static String extensionName() {
        return EXTENSION_NAME;
    }

    @Override
    public void apply(Project project) {
        System.out.println("Applying Spine Check 1");
        project.getExtensions()
               .create(EXTENSION_NAME, SpineCheckExtension.class);
        System.out.println("Created extension");
        Optional<String> versionOptional = acquireModelCompilerVersion(project);
        if (!versionOptional.isPresent()) {
            log().debug("Model compiler plugin dependency version is not found for the project {}",
                        project.getName());
            return;
        }
        String version = versionOptional.get();
        Optional<Configuration> configurationOptional =
                configureAnnotationProcessor(version, project);
        if (!configurationOptional.isPresent()) {
            log().debug("The configuration 'annotationProcessor' could not be created nor " +
                                "acquired for the project {}", project.getName());
            return;
        }
        Configuration annotationProcessor = configurationOptional.get();
        if (!isResolvable(annotationProcessor)) {
            log().debug("The artifacts for the 'annotationProcessor' configuration are not " +
                                "available in the project {}, rolling back changes and quitting",
                        project.getName());
            rollBackChanges(annotationProcessor);
            return;
        }
        System.out.println("Getting vBuilder Severity");
        String useVBuilderSeverity = getUseVBuilderSeverity(project);
        System.out.println("Got vBuilder Severity");
        if (useVBuilderSeverity == null) {
            System.out.println("UseVBuilder severity is null.");
        } else {
            System.out.println("UseVBuilder severity is not null, but " + useVBuilderSeverity);
        }
        addToJavaCompileTasks(annotationProcessor, project);
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
            if ("spine-model-compiler".equals(dependency.getName())) {
                String dependencyVersion = dependency.getVersion();
                version = Optional.ofNullable(dependencyVersion);
            }
        }
        return version;
    }

    private Optional<Configuration> configureAnnotationProcessor(String version, Project project) {
        log().debug("Creating/adjusting 'annotationProcessor' configuration for the project {}",
                    project.getName());
        ConfigurationContainer configurations = project.getConfigurations();
        Configuration config = configurations.maybeCreate("annotationProcessor");
        DependencySet dependencies = config.getDependencies();
        Dependency dependency =
                new DefaultExternalModuleDependency("io.spine.tools", SPINE_CHECKS_MODULE, version);
        dependencies.add(dependency);
        Optional<Configuration> result = Optional.of(config);
        return result;
    }

    private boolean isResolvable(Configuration annotationProcessor) {
        log().debug("Checking that all artifacts for the 'annotationProcessor' are available.");
        Configuration configCopy = annotationProcessor.copy();
        ResolvedConfiguration resolved = configCopy.getResolvedConfiguration();
        boolean isResolvable = !resolved.hasError();
        return isResolvable;
    }

    private void rollBackChanges(Configuration annotationProcessor) {
        log().debug("Rolling back dependency changes for the 'annotationProcessor' configuration.");
        DependencySet dependencies = annotationProcessor.getDependencies();
        dependencies.removeIf(dependency -> SPINE_CHECKS_MODULE.equals(dependency.getName()));
    }

    private void addToJavaCompileTasks(Configuration annotationProcessor, Project project) {
        log().debug("Adding the 'annotationProcessor' configuration to all 'JavaCompile' tasks.");
        TaskContainer tasks = project.getTasks();
        TaskCollection<JavaCompile> javaCompileTasks = tasks.withType(JavaCompile.class);
        for (JavaCompile task : javaCompileTasks) {
            CompileOptions options = task.getOptions();
            List<String> compilerArgs = options.getCompilerArgs();
            compilerArgs.add("-processorpath");
            compilerArgs.add(annotationProcessor.getAsPath());
        }
    }
}
