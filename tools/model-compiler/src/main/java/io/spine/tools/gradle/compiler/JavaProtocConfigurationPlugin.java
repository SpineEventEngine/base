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

import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import com.google.protobuf.gradle.ExecutableLocator;
import com.google.protobuf.gradle.GenerateProtoTask;
import io.spine.code.fs.java.DefaultJavaProject;
import io.spine.code.fs.java.DefaultJavaProject.GeneratedRoot;
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

import static com.google.common.base.Charsets.UTF_8;
import static io.spine.tools.gradle.BaseTaskName.clean;
import static io.spine.tools.gradle.ModelCompilerTaskName.writeDescriptorReference;
import static io.spine.tools.gradle.ModelCompilerTaskName.writePluginConfiguration;
import static io.spine.tools.gradle.ModelCompilerTaskName.writeTestDescriptorReference;
import static io.spine.tools.gradle.ModelCompilerTaskName.writeTestPluginConfiguration;
import static io.spine.tools.gradle.ProtocPluginName.grpc;
import static io.spine.tools.gradle.ProtocPluginName.spineProtoc;

/**
 * A Gradle plugin that performs additional {@code protoc} configurations relevant for Java
 * projects.
 */
public final class JavaProtocConfigurationPlugin extends ProtocConfigurationPlugin {

    private static final String GRPC_GROUP = "io.grpc";
    private static final String GRPC_PLUGIN_NAME = "protoc-gen-grpc-java";

    @Override
    protected void configureDescriptorSetGeneration(GenerateProtoTask protocTask,
                                                    File descriptor) {
        boolean tests = isTestsTask(protocTask);
        Project project = protocTask.getProject();
        TaskName writeRefName = tests
                                ? writeTestDescriptorReference
                                : writeDescriptorReference;
        JavaPluginConvention javaConvention = project.getConvention()
                                                     .getPlugin(JavaPluginConvention.class);
        SourceScope sourceScope = tests ? SourceScope.test : SourceScope.main;
        Path resourceDirectory = descriptor.toPath()
                                           .getParent();
        javaConvention.getSourceSets()
                      .getByName(sourceScope.name())
                      .getResources()
                      .srcDir(resourceDirectory);
        GradleTask writeRef = newTask(writeRefName, task -> {
            DescriptorReference reference = DescriptorReference.toOneFile(descriptor);
            reference.writeTo(resourceDirectory);
        }).allowNoDependencies()
          .applyNowTo(project);
        protocTask.finalizedBy(writeRef.getTask());
    }

    @Override
    protected void configureTaskPlugins(GenerateProtoTask protocTask, Task dependency) {
        Path spineProtocConfigPath = spineProtocConfigPath(protocTask);
        Task writeConfig = newWriteSpineProtocConfigTask(protocTask, spineProtocConfigPath);
        protocTask.dependsOn(dependency, writeConfig);
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

    @OverridingMethodsMustInvokeSuper
    @Override
    protected final void configureProtocPlugins(NamedDomainObjectContainer<ExecutableLocator> plugins) {
        super.configureProtocPlugins(plugins);
        plugins.create(grpc.name(),
                       locator -> locator.setArtifact(Artifact.newBuilder()
                                                              .setGroup(GRPC_GROUP)
                                                              .setName(GRPC_PLUGIN_NAME)
                                                              .setVersion(VERSIONS.grpc())
                                                              .build()
                                                              .notation()));
    }

    @Override
    protected File getTestDescriptorSet(Project project) {
        return Extension.getTestDescriptorSet(project);
    }

    @Override
    protected Path generatedFilesBaseDir(Project project) {
        DefaultJavaProject javaProject = DefaultJavaProject.at(project.getProjectDir());
        GeneratedRoot result = javaProject.generated();
        return result.path();
    }

    @Override
    protected File getMainDescriptorSet(Project project) {
        return Extension.getMainDescriptorSet(project);
    }

    /**
     * Creates a new {@code writeSpineProtocConfig} task that is expected to run after the
     * {@code clean} task.
     */
    private Task newWriteSpineProtocConfigTask(GenerateProtoTask protocTask, Path configPath) {
        return newTask(spineProtocConfigWriteTaskName(protocTask), task -> {
            ProtocPluginConfiguration configuration = ProtocPluginConfiguration
                    .forProject(protocTask.getProject());
            configuration.writeTo(configPath);
        }).allowNoDependencies()
          .applyNowTo(protocTask.getProject())
          .getTask()
          .mustRunAfter(clean.name());
    }

    private static String base64Encoded(String value) {
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] valueBytes = value.getBytes(UTF_8);
        String result = encoder.encodeToString(valueBytes);
        return result;
    }

    private static TaskName spineProtocConfigWriteTaskName(GenerateProtoTask protoTask) {
        return isTestsTask(protoTask) ?
               writeTestPluginConfiguration :
               writePluginConfiguration;
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
