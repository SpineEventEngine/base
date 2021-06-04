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

import com.google.common.base.Charsets;
import com.google.protobuf.gradle.ExecutableLocator;
import com.google.protobuf.gradle.GenerateProtoTask;
import io.spine.tools.java.fs.DefaultJavaPaths;
import io.spine.tools.java.fs.DefaultJavaPaths.GeneratedRoot;
import io.spine.code.proto.DescriptorReference;
import io.spine.tools.gradle.Artifact;
import io.spine.tools.gradle.GradleTask;
import io.spine.tools.gradle.ProtocConfigurationPlugin;
import io.spine.tools.gradle.SourceScope;
import io.spine.tools.gradle.TaskName;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPluginConvention;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import static io.spine.tools.java.fs.DefaultJavaPaths.at;
import static io.spine.tools.gradle.BaseTaskName.clean;
import static io.spine.tools.gradle.JavaTaskName.processResources;
import static io.spine.tools.gradle.JavaTaskName.processTestResources;
import static io.spine.tools.gradle.ModelCompilerTaskName.writeDescriptorReference;
import static io.spine.tools.gradle.ModelCompilerTaskName.writePluginConfiguration;
import static io.spine.tools.gradle.ModelCompilerTaskName.writeTestDescriptorReference;
import static io.spine.tools.gradle.ModelCompilerTaskName.writeTestPluginConfiguration;
import static io.spine.tools.gradle.ProtocPluginName.grpc;
import static io.spine.tools.gradle.ProtocPluginName.spineProtoc;
import static io.spine.tools.mc.java.gradle.ProtocPluginConfiguration.forProject;

/**
 * A Gradle plugin that performs additional {@code protoc} configurations relevant for Java
 * projects.
 */
public final class JavaProtocConfigurationPlugin extends ProtocConfigurationPlugin {

    private static final String JAR_EXTENSION = "jar";
    private static final String GRPC_GROUP = "io.grpc";
    private static final String GRPC_PLUGIN_NAME = "protoc-gen-grpc-java";
    private static final String SPINE_PLUGIN_NAME = "spine-mc-java-protoc";
    private static final String EXECUTABLE_CLASSIFIER = "exe";

    @Override
    protected void
    configureProtocPlugins(NamedDomainObjectContainer<ExecutableLocator> plugins, Project project) {
        Artifact gRpcPlugin = Artifact
                .newBuilder()
                .setGroup(GRPC_GROUP)
                .setName(GRPC_PLUGIN_NAME)
                .setVersion(VERSIONS.grpc())
                .build();
        Artifact spinePlugin = Artifact
                .newBuilder()
                .useSpineToolsGroup()
                .setName(SPINE_PLUGIN_NAME)
                .setVersion(VERSIONS.spineBase())
                .setClassifier(EXECUTABLE_CLASSIFIER)
                .setExtension(JAR_EXTENSION)
                .build();
        plugins.create(grpc.name(), locator -> locator.setArtifact(gRpcPlugin.notation()));
        plugins.create(spineProtoc.name(), locator -> locator.setArtifact(spinePlugin.notation()));

    }

    @Override
    protected void customizeTask(GenerateProtoTask protocTask) {
        customizeDescriptorSetGeneration(protocTask);
        Path spineProtocConfigPath = spineProtocConfigPath(protocTask);
        Task writeConfig = newWriteSpineProtocConfigTask(protocTask, spineProtocConfigPath);
        protocTask.dependsOn(writeConfig);
        protocTask.getPlugins()
                  .create(grpc.name());
        protocTask.getPlugins()
                  .create(spineProtoc.name(),
                          options -> {
                              options.setOutputSubDir("java");
                              String option = spineProtocConfigPath.toString();
                              String encodedOption = base64Encoded(option);
                              options.option(encodedOption);
                          });
    }

    private void customizeDescriptorSetGeneration(GenerateProtoTask protocTask) {
        boolean tests = isTestsTask(protocTask);
        Project project = protocTask.getProject();
        TaskName writeRefName = writeRefNameTask(tests);
        JavaPluginConvention javaConvention = project.getConvention()
                                                     .getPlugin(JavaPluginConvention.class);
        SourceScope sourceScope = tests ? SourceScope.test : SourceScope.main;
        File descriptorFile = new File(protocTask.getDescriptorPath());
        Path resourceDirectory = descriptorFile.toPath()
                                               .getParent();
        javaConvention.getSourceSets()
                      .getByName(sourceScope.name())
                      .getResources()
                      .srcDir(resourceDirectory);
        GradleTask writeRef = newTask(writeRefName,
                                      task -> writeRefFile(descriptorFile, resourceDirectory))
                .insertBeforeTask(processResourceTaskName(tests))
                .applyNowTo(project);
        protocTask.finalizedBy(writeRef.getTask());
    }

    private static void writeRefFile(File descriptorFile, Path resourceDirectory) {
        DescriptorReference reference = DescriptorReference.toOneFile(descriptorFile);
        reference.writeTo(resourceDirectory);
    }

    private static TaskName writeRefNameTask(boolean tests) {
        return tests ? writeTestDescriptorReference : writeDescriptorReference;
    }

    private static TaskName processResourceTaskName(boolean tests) {
        return tests ? processTestResources : processResources;
    }

    @Override
    protected File getTestDescriptorSet(Project project) {
        return Extension.getTestDescriptorSetFile(project);
    }

    @Override
    protected Path generatedFilesBaseDir(Project project) {
        DefaultJavaPaths javaProject = at(project.getProjectDir());
        GeneratedRoot result = javaProject.generated();
        return result.path();
    }

    @Override
    protected File getMainDescriptorSet(Project project) {
        return Extension.getMainDescriptorSetFile(project);
    }

    /**
     * Creates a new {@code writeSpineProtocConfig} task that is expected to run after the
     * {@code clean} task.
     */
    private Task newWriteSpineProtocConfigTask(GenerateProtoTask protocTask, Path configPath) {
        return newTask(spineProtocConfigWriteTaskName(protocTask),
                       task -> writePluginConfig(protocTask, configPath))
                .allowNoDependencies()
                .applyNowTo(protocTask.getProject())
                .getTask()
                .mustRunAfter(clean.name());
    }

    private static void writePluginConfig(Task protocTask, Path configPath) {
        ProtocPluginConfiguration configuration = forProject(protocTask.getProject());
        configuration.writeTo(configPath);
    }

    private static String base64Encoded(String value) {
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] valueBytes = value.getBytes(Charsets.UTF_8);
        String result = encoder.encodeToString(valueBytes);
        return result;
    }

    private static TaskName spineProtocConfigWriteTaskName(GenerateProtoTask protoTask) {
        return isTestsTask(protoTask)
               ? writeTestPluginConfiguration
               : writePluginConfiguration;
    }

    private static Path spineProtocConfigPath(GenerateProtoTask protocTask) {
        Project project = protocTask.getProject();
        File buildDir = project.getBuildDir();
        Path spinePluginTmpDir = Paths.get(buildDir.getAbsolutePath(),
                                           "tmp",
                                           SPINE_PLUGIN_NAME);
        Path protocConfigPath = isTestsTask(protocTask) ?
                                spinePluginTmpDir.resolve("test-config.pb") :
                                spinePluginTmpDir.resolve("config.pb");
        return protocConfigPath;
    }
}
