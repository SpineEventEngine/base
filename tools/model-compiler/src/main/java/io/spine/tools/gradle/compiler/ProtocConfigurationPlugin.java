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

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.google.protobuf.gradle.ExecutableLocator;
import com.google.protobuf.gradle.GenerateProtoTask;
import com.google.protobuf.gradle.ProtobufConfigurator;
import com.google.protobuf.gradle.ProtobufConfigurator.GenerateProtoTaskCollection;
import com.google.protobuf.gradle.ProtobufConvention;
import io.spine.code.java.DefaultJavaProject;
import io.spine.code.proto.DescriptorReference;
import io.spine.tools.gradle.Artifact;
import io.spine.tools.gradle.GradleTask;
import io.spine.tools.gradle.SpinePlugin;
import io.spine.tools.gradle.TaskName;
import io.spine.tools.groovy.GStrings;
import io.spine.tools.protoc.SpineProtocConfig;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.TaskCollection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Base64;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.code.java.DefaultJavaProject.at;
import static io.spine.tools.gradle.ConfigurationName.FETCH;
import static io.spine.tools.gradle.TaskName.COPY_PLUGIN_JAR;
import static io.spine.tools.gradle.TaskName.WRITE_DESCRIPTOR_REFERENCE;
import static io.spine.tools.gradle.TaskName.WRITE_TEST_DESCRIPTOR_REFERENCE;
import static io.spine.tools.gradle.compiler.Extension.getGeneratedInterfaces;
import static io.spine.tools.gradle.compiler.Extension.getMainDescriptorSet;
import static io.spine.tools.gradle.compiler.Extension.getTestDescriptorSet;
import static io.spine.tools.groovy.ConsumerClosure.closure;
import static org.gradle.internal.os.OperatingSystem.current;

/**
 * The Gradle plugin which configures Protobuf compilation.
 *
 * <p>This plugin requires {@code com.google.protobuf} plugin. If it is not applied, the plugin
 * performs no action.
 */
public class ProtocConfigurationPlugin extends SpinePlugin {

    private static final String PROTOBUF_GROUP = "com.google.protobuf";
    private static final String PROTOBUF_GRADLE_PLUGIN = PROTOBUF_GROUP;
    private static final String PROTOC = "protoc";

    private static final String GRPC_GROUP = "io.grpc";
    private static final String GRPC_PLUGIN_NAME = "protoc-gen-grpc-java";

    private static final String SPINE_PLUGIN_NAME = "spine-protoc-plugin";
    private static final String JAR_EXTENSION = "jar";
    private static final String SH_EXTENSION = "sh";
    private static final String BAT_EXTENSION = "bat";
    private static final String SCRIPT_CLASSIFIER = "script";

    private static final DependencyVersions VERSIONS = DependencyVersions.load();

    @Override
    public void apply(Project project) {
        project.getPluginManager()
               .withPlugin(PROTOBUF_GRADLE_PLUGIN, plugin -> applyTo(project));
    }

    private void applyTo(Project project) {
        project.getConvention()
               .getPlugin(ProtobufConvention.class)
               .protobuf(closure(
                       (ProtobufConfigurator protobuf) -> configureProtobuf(project, protobuf)
               ));
    }

    private void configureProtobuf(Project project, ProtobufConfigurator protobuf) {
        DefaultJavaProject defaultProject = at(project.getProjectDir());
        protobuf.setGeneratedFilesBaseDir(defaultProject.generated()
                                                        .toString());
        protobuf.protoc(closure(
                (ExecutableLocator protocLocator) -> protocLocator.setArtifact(
                        Artifact.newBuilder()
                                .setGroup(PROTOBUF_GROUP)
                                .setName(PROTOC)
                                .setVersion(VERSIONS.protobuf())
                                .build()
                                .notation())
        ));
        protobuf.plugins(closure(ProtocConfigurationPlugin::configureProtocPlugins));
        GradleTask copyPluginJar = createCopyPluginJarTask(project);
        protobuf.generateProtoTasks(closure(
                (GenerateProtoTaskCollection tasks) -> configureProtocTasks(tasks, copyPluginJar)
        ));
    }

    private static void
    configureProtocPlugins(NamedDomainObjectContainer<ExecutableLocator> plugins) {
        plugins.create(ProtocPlugin.GRPC.name,
                       locator -> locator.setArtifact(Artifact.newBuilder()
                                                              .setGroup(GRPC_GROUP)
                                                              .setName(GRPC_PLUGIN_NAME)
                                                              .setVersion(VERSIONS.grpc())
                                                              .build()
                                                              .notation()));
        plugins.create(ProtocPlugin.SPINE.name, locator -> {
            boolean windows = current().isWindows();
            String scriptExt = windows ? BAT_EXTENSION : SH_EXTENSION;
            locator.setArtifact(Artifact.newBuilder()
                                        .useSpineToolsGroup()
                                        .setName(SPINE_PLUGIN_NAME)
                                        .setVersion(VERSIONS.spineBase())
                                        .setClassifier(SCRIPT_CLASSIFIER)
                                        .setExtension(scriptExt)
                                        .build()
                                        .notation());
        });
    }

    private GradleTask createCopyPluginJarTask(Project project) {
        Configuration fetch = project.getConfigurations()
                                     .maybeCreate(FETCH.getValue());
        Artifact protocPluginArtifact = Artifact
                .newBuilder()
                .useSpineToolsGroup()
                .setName(SPINE_PLUGIN_NAME)
                .setVersion(VERSIONS.spineBase())
                .setExtension(JAR_EXTENSION)
                .build();
        Dependency protocPluginDependency = project
                .getDependencies()
                .add(fetch.getName(), protocPluginArtifact.notation());
        checkNotNull(protocPluginDependency,
                     "Could not create dependency %s %s", fetch.getName(), protocPluginArtifact);
        Action<Task> action = task -> copyPluginExecutables(project, protocPluginDependency, fetch);
        GradleTask copyPluginJar = newTask(COPY_PLUGIN_JAR, action)
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

    private void configureProtocTask(GenerateProtoTask protocTask, Task dependency) {
        configureTaskPlugins(protocTask, dependency);
        configureDescriptorSetGeneration(protocTask);
    }

    private void configureDescriptorSetGeneration(GenerateProtoTask protocTask) {
        protocTask.setGenerateDescriptorSet(true);
        boolean tests = protocTask.getSourceSet()
                                  .getName()
                                  .contains("test");
        Project project = protocTask.getProject();
        File descriptor;
        TaskName writeRefName;
        if (tests) {
            descriptor = getTestDescriptorSet(project);
            writeRefName = WRITE_TEST_DESCRIPTOR_REFERENCE;
        } else {
            descriptor = getMainDescriptorSet(project);
            writeRefName = WRITE_DESCRIPTOR_REFERENCE;
        }
        GenerateProtoTask.DescriptorSetOptions options = protocTask.getDescriptorSetOptions();
        options.setPath(GStrings.fromPlain(descriptor.getPath()));
        options.setIncludeImports(true);
        options.setIncludeSourceInfo(true);

        JavaPluginConvention javaConvention = project.getConvention()
                                                     .getPlugin(JavaPluginConvention.class);
        String sourceSetName = tests ? "test" : "main";
        Path resourceDirectory = descriptor.toPath()
                                           .getParent();
        javaConvention.getSourceSets()
                      .getByName(sourceSetName)
                      .getResources()
                      .srcDir(resourceDirectory);
        GradleTask writeRef = newTask(writeRefName, task -> {
            DescriptorReference reference = DescriptorReference.toOneFile(descriptor);
            reference.writeTo(resourceDirectory);
        }).allowNoDependencies()
          .applyNowTo(project);
        protocTask.finalizedBy(writeRef.getTask());
    }

    private static void configureTaskPlugins(GenerateProtoTask protocTask, Task dependency) {
        protocTask.dependsOn(dependency);
        protocTask.getPlugins()
                  .create(ProtocPlugin.GRPC.name);
        protocTask.getPlugins()
                  .create(ProtocPlugin.SPINE.name,
                          options -> {
                              options.setOutputSubDir("java");
                              SpineProtocConfig param = assembleParameter(protocTask.getProject());
                              String option = Base64.getEncoder()
                                                    .encodeToString(param.toByteArray());
                              options.option(option);
                          });
    }

    private void configureProtocTasks(GenerateProtoTaskCollection tasks,
                                      GradleTask dependency) {
        // This is a `live` view of the current Gradle tasks.
        TaskCollection<GenerateProtoTask> tasksProxy = tasks.all();

        /*
         *  Creating a hard-copy of `live` view of matching Gradle tasks.
         *
         *  Otherwise a `ConcurrentModificationException` is thrown upon an attempt to
         *  insert a task into the Gradle task list.
         */
        ImmutableList<GenerateProtoTask> allTasks = ImmutableList.copyOf(tasksProxy);
        for (GenerateProtoTask task : allTasks) {
            configureProtocTask(task, dependency.getTask());
        }
    }

    private static SpineProtocConfig assembleParameter(Project project) {
        GeneratedInterfaces interfaces = getGeneratedInterfaces(project);
        return interfaces.asProtocConfig();
    }

    private enum ProtocPlugin {

        GRPC("grpc"),
        SPINE("spineProtoc");

        @SuppressWarnings("PMD.SingularField") /* Accessed from the outer class. */
        private final String name;

        ProtocPlugin(String name) {
            this.name = name;
        }
    }
}
