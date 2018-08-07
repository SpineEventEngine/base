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

import io.spine.tools.gradle.compiler.given.SpineCheckerPluginTestEnv.NonResolvingSpineCheckerPlugin;
import io.spine.tools.gradle.compiler.given.SpineCheckerPluginTestEnv.ResolvingSpineCheckerPlugin;
import io.spine.tools.gradle.compiler.given.SpineCheckerPluginTestEnv.SpineCheckerPluginWithoutErrorProne;
import org.gradle.BuildListener;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.initialization.dsl.ScriptHandler;
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.invocation.DefaultGradle;
import org.junit.Test;

import java.util.List;

import static com.google.common.collect.testing.Helpers.assertEmpty;
import static io.spine.testing.Verify.assertContains;
import static io.spine.tools.gradle.compiler.Severity.ERROR;
import static io.spine.tools.gradle.compiler.SpineCheckerPlugin.MODEL_COMPILER_PLUGIN_NAME;
import static io.spine.tools.gradle.compiler.SpineCheckerPlugin.PREPROCESSOR_CONFIG_NAME;
import static io.spine.tools.gradle.compiler.SpineCheckerPlugin.SPINE_CHECKER_MODULE;
import static io.spine.tools.gradle.compiler.SpineCheckerPlugin.SPINE_TOOLS_GROUP;
import static io.spine.tools.gradle.compiler.given.ModelCompilerTestEnv.newProject;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class SpineCheckerPluginShould {

    @Test
    public void create_spine_check_extension() {
        Project project = newProject();
        project.getPluginManager()
               .apply(SpineCheckerPlugin.class);
        ExtensionContainer extensions = project.getExtensions();
        Object found = extensions.findByName(SpineCheckerPlugin.extensionName());
        assertNotNull(found);
    }

    @Test
    public void create_annotation_processor_config_if_it_does_not_exist() {
        Project project = newProject();
        ConfigurationContainer configs = project.getConfigurations();
        Configuration config = configs.getByName(PREPROCESSOR_CONFIG_NAME);
        configs.remove(config);

        assertNull(configs.findByName(PREPROCESSOR_CONFIG_NAME));

        project.getPluginManager()
               .apply(SpineCheckerPlugin.class);
        assertNotNull(configs.findByName(PREPROCESSOR_CONFIG_NAME));
    }

    @Test
    public void add_spine_check_dependency_to_annotation_processor_config() {
        Project project = projectWithModelCompilerDependency();
        project.getPluginManager()
               .apply(ResolvingSpineCheckerPlugin.class);
        boolean hasSpineCheckerDependency = hasSpineCheckerDependency(project);
        assertTrue(hasSpineCheckerDependency);
    }

    @Test
    public void not_add_spine_check_dependency_if_it_is_not_resolvable() {
        Project project = projectWithModelCompilerDependency();
        project.getPluginManager()
               .apply(NonResolvingSpineCheckerPlugin.class);
        boolean hasSpineCheckerDependency = hasSpineCheckerDependency(project);
        assertFalse(hasSpineCheckerDependency);
    }

    @Test
    public void not_add_spine_check_dependency_if_model_compiler_dependency_not_available() {
        Project project = newProject();
        boolean hasSpineCheckerDependency = hasSpineCheckerDependency(project);
        assertFalse(hasSpineCheckerDependency);
    }

    @Test
    public void configure_check_severity() {
        Project project = projectWithModelCompilerDependency();
        configureWithSpineCheckerExtension(project, ResolvingSpineCheckerPlugin.class);
        checkSeverityConfigured(project);
    }

    @Test
    public void configure_check_severity_for_all_checks() {
        Project project = projectWithModelCompilerDependency();
        configureWithModelCompilerExtension(project, ResolvingSpineCheckerPlugin.class);
        checkSeverityConfigured(project);
    }

    @Test
    public void not_add_severity_args_if_error_prone_plugin_not_applied() {
        Project project = projectWithModelCompilerDependency();
        configureWithModelCompilerExtension(project, SpineCheckerPluginWithoutErrorProne.class);
        checkSeverityUntouched(project);
    }

    private static Project projectWithModelCompilerDependency() {
        Project project = newProject();
        ScriptHandler buildscript = project.getRootProject()
                                           .getBuildscript();
        ConfigurationContainer configurations = buildscript.getConfigurations();
        Configuration classpath = configurations.findByName("classpath");
        DependencySet classpathDependencies = classpath.getDependencies();

        String versionStub = "versionStub";
        Dependency modelCompilerDependency =
                new DefaultExternalModuleDependency(SPINE_TOOLS_GROUP,
                                                    MODEL_COMPILER_PLUGIN_NAME,
                                                    versionStub);
        classpathDependencies.add(modelCompilerDependency);
        return project;
    }

    private static boolean hasSpineCheckerDependency(Project project) {
        ConfigurationContainer configs = project.getConfigurations();
        Configuration config = configs.getByName(PREPROCESSOR_CONFIG_NAME);
        DependencySet dependencies = config.getDependencies();
        for (Dependency dependency : dependencies) {
            if (SPINE_TOOLS_GROUP.equals(dependency.getGroup()) &&
                    SPINE_CHECKER_MODULE.equals(dependency.getName())) {
                return true;
            }
        }
        return false;
    }

    private static void
    configureWithSpineCheckerExtension(Project project,
                                       Class<? extends SpineCheckerPlugin> pluginToApply) {
        ExtensionContainer extensions = project.getExtensions();
        extensions.create(ModelCompilerPlugin.extensionName(), Extension.class);
        project.getPluginManager()
               .apply(pluginToApply);
        SpineCheckerExtension extension =
                (SpineCheckerExtension) extensions.getByName(SpineCheckerPlugin.extensionName());
        extension.useVBuilder = ERROR;
    }

    private static void
    configureWithModelCompilerExtension(Project project,
                                        Class<? extends SpineCheckerPlugin> pluginToApply) {
        ExtensionContainer extensions = project.getExtensions();
        Extension modelCompilerExtension =
                extensions.create(ModelCompilerPlugin.extensionName(), Extension.class);
        project.getPluginManager()
               .apply(pluginToApply);
        modelCompilerExtension.spineCheckerSeverity = ERROR;
    }

    private static void checkSeverityConfigured(Project project) {
        TaskCollection<JavaCompile> javaCompileTasks = acquireJavaCompileTasks(project);
        for (JavaCompile task : javaCompileTasks) {
            List<String> compilerArgs = obtainCompilerArgs(task);
            assertContains("-Xep:UseVBuilder:ERROR", compilerArgs);
        }
    }

    private static void checkSeverityUntouched(Project project) {
        TaskCollection<JavaCompile> javaCompileTasks = acquireJavaCompileTasks(project);
        for (JavaCompile task : javaCompileTasks) {
            List<String> compilerArgs = obtainCompilerArgs(task);
            assertEmpty(compilerArgs);
        }
    }

    private static TaskCollection<JavaCompile> acquireJavaCompileTasks(Project project) {
        DefaultGradle gradle = (DefaultGradle) project.getGradle();
        BuildListener buildListenerBroadcaster = gradle.getBuildListenerBroadcaster();
        buildListenerBroadcaster.projectsEvaluated(project.getGradle());
        TaskContainer tasks = project.getTasks();
        return tasks.withType(JavaCompile.class);
    }

    private static List<String> obtainCompilerArgs(JavaCompile task) {
        CompileOptions options = task.getOptions();
        return options.getCompilerArgs();
    }
}
