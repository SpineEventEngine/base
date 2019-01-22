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

package io.spine.tools.gradle.compiler;

import com.google.common.io.Files;
import com.google.protobuf.gradle.ExecutableLocator;
import com.google.protobuf.gradle.GenerateProtoTask;
import com.google.protobuf.gradle.ProtobufConfigurator;
import com.google.protobuf.gradle.ProtobufConfigurator.GenerateProtoTaskCollection;
import com.google.protobuf.gradle.ProtobufConvention;
import io.spine.code.java.DefaultJavaProject;
import io.spine.tools.gradle.GradleTask;
import io.spine.tools.gradle.SpinePlugin;
import io.spine.tools.groovy.GStrings;
import org.gradle.api.GradleException;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.plugins.JavaPluginConvention;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.code.java.DefaultJavaProject.at;
import static io.spine.tools.gradle.ConfigurationName.FETCH;
import static io.spine.tools.gradle.TaskName.COPY_PLUGIN_JAR;
import static io.spine.tools.gradle.compiler.Extension.getMainDescriptorSetPath;
import static io.spine.tools.gradle.compiler.Extension.getTestDescriptorSetPath;
import static io.spine.tools.groovy.ConsumerClosure.closure;
import static java.lang.String.format;
import static org.gradle.internal.os.OperatingSystem.current;

public class ProtocConfigurationPlugin extends SpinePlugin {

    private static final String PROTOBUF_GRADLE_PLUGIN = "com.google.protobuf";

    private static final String PLUGIN_DEPENDENCY_TEMPLATE =
            "io.spine.tools:spine-protoc-plugin:%s@jar";

    @Override
    public void apply(Project project) {
        project.getPluginManager()
               .withPlugin(PROTOBUF_GRADLE_PLUGIN, plugin -> applyTo(project));
    }

    private void applyTo(Project project) {
        project.getConvention()
               .getPlugin(ProtobufConvention.class)
               .protobuf(closure(
                       ProtobufConfigurator.class,
                       protobuf -> configureProtobuf(project, protobuf)
               ));
    }

    private void configureProtobuf(Project project, ProtobufConfigurator protobuf) {
        DefaultJavaProject defaultProject = at(project.getProjectDir());
        protobuf.setGeneratedFilesBaseDir(defaultProject.generated().toString());
        protobuf.protoc(closure(
                ExecutableLocator.class,
                protocLocator -> protocLocator.setArtifact("com.google.protobuf:protoc:3.6.1")
        ));
        protobuf.plugins(closure(
                NamedDomainObjectContainer.class,
                ProtocConfigurationPlugin::configureProtocPlugins
        ));
        GradleTask copyPluginJar = createCopyPluginJarTask(project);
        protobuf.generateProtoTasks(closure(
                GenerateProtoTaskCollection.class,
                tasks -> configureProtocTasks(tasks, copyPluginJar)
        ));
    }

    private static void configureProtocPlugins(NamedDomainObjectContainer<ExecutableLocator> plugins) {
        plugins.create(ProtocPlugin.GRPC.name,
                       locator -> locator.setArtifact("io.grpc:protoc-gen-grpc-java:1.15.0"));
        plugins.create(ProtocPlugin.SPINE.name, locator -> {
            boolean windows = current().isWindows();
            String scriptExt = windows ? "bat" : "sh";
            locator.setArtifact("io.spine.tools:spine-protoc-plugin:" + "1.0.0-SNAPSHOT:" + "script@" + scriptExt);
        });
    }

    private GradleTask createCopyPluginJarTask(Project project) {
        Configuration fetch = project.getConfigurations()
                                     .maybeCreate(FETCH.getValue());
        // TODO:2019-01-21:dmytro.dashenkov: Provide proper version.
        String dependency = format(PLUGIN_DEPENDENCY_TEMPLATE, "1.0.0-SNAPSHOT");
        Dependency protocPluginDependency = project.getDependencies()
                                                   .add(fetch.getName(), dependency);
        checkNotNull(protocPluginDependency,
                     "Could not create dependency %s %s", fetch.getName(), dependency);
        GradleTask copyPluginJar = newTask(COPY_PLUGIN_JAR,
                                           task -> copyPluginExecutables(project,
                                                                         protocPluginDependency,
                                                                         fetch))
                .allowNoDependencies()
                .applyNowTo(project);
        return copyPluginJar;
    }

    private static void copyPluginExecutables(Project project,
                                              Dependency protobufDependency,
                                              Configuration fetchConfiguration) {
        File executableJar = fetchConfiguration.fileCollection(protobufDependency)
                                               .getSingleFile();
        File spineDir = at(project.getProjectDir()).tempArtifacts();
        File rootSpineDir = at(project.getRootDir()).tempArtifacts();
        copy(executableJar, spineDir);
        copy(executableJar, rootSpineDir);
    }

    private static void copy(File file, File destinationDir) {
        try {
            destinationDir.mkdirs();
            File destination = destinationDir.toPath()
                                             .resolve(file.getName())
                                             .toFile();
            Files.copy(file, destination);
        } catch (IOException e) {
            throw new GradleException("Failed to copy Spine Protoc executable JAR.", e);
        }
    }

    private static void configureProtocTask(GenerateProtoTask protocTask, Task dependency) {
        protocTask.dependsOn(dependency);

        protocTask.getPlugins()
            .create(ProtocPlugin.GRPC.name);
        protocTask.getPlugins()
            .create(ProtocPlugin.SPINE.name,
                    options -> options.setOutputSubDir("java"));
        protocTask.setGenerateDescriptorSet(true);
        boolean tests = protocTask.getSourceSet()
                            .getName()
                            .contains("test");
        Project project = protocTask.getProject();
        String descPath = tests
                          ? getTestDescriptorSetPath(project)
                          : getMainDescriptorSetPath(project);
        GenerateProtoTask.DescriptorSetOptions options = protocTask.getDescriptorSetOptions();
        options.setPath(GStrings.fromPlain(descPath));
        options.setIncludeImports(true);
        options.setIncludeSourceInfo(true);

        JavaPluginConvention javaConvention = project.getConvention()
                                                     .getPlugin(JavaPluginConvention.class);
        String sourceSetName = tests ? "test" : "main";
        javaConvention.getSourceSets()
                      .getByName(sourceSetName)
                      .getResources()
                      .srcDir(Paths.get(descPath)
                                   .getParent());
    }

    private static void configureProtocTasks(GenerateProtoTaskCollection tasks,
                                             GradleTask dependency) {
        tasks.all().forEach(task -> configureProtocTask(task, dependency.getTask()));
    }

    private enum ProtocPlugin {

        GRPC("grpc"),
        SPINE("spineProtoc");

        private final String name;

        ProtocPlugin(String name) {
            this.name = name;
        }
    }
}
