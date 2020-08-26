/*
 * Copyright 2020, TeamDev. All rights reserved.
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

import com.google.common.base.Charsets;
import com.google.common.io.CharSink;
import com.google.protobuf.gradle.ExecutableLocator;
import com.google.protobuf.gradle.GenerateProtoTask;
import io.spine.code.fs.java.DefaultJavaProject;
import io.spine.code.fs.java.DefaultJavaProject.GeneratedRoot;
import io.spine.code.proto.DescriptorReference;
import io.spine.io.Resource;
import io.spine.tools.gradle.Artifact;
import io.spine.tools.gradle.ConfigurationName;
import io.spine.tools.gradle.GradleTask;
import io.spine.tools.gradle.ProtocConfigurationPlugin;
import io.spine.tools.gradle.SourceScope;
import io.spine.tools.gradle.TaskName;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.plugins.JavaPluginConvention;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.io.Files.asCharSink;
import static io.spine.code.fs.java.DefaultJavaProject.at;
import static io.spine.tools.gradle.BaseTaskName.clean;
import static io.spine.tools.gradle.ConfigurationName.fetch;
import static io.spine.tools.gradle.ModelCompilerTaskName.writeDescriptorReference;
import static io.spine.tools.gradle.ModelCompilerTaskName.writePluginConfiguration;
import static io.spine.tools.gradle.ModelCompilerTaskName.writeTestDescriptorReference;
import static io.spine.tools.gradle.ModelCompilerTaskName.writeTestPluginConfiguration;
import static io.spine.tools.gradle.ProtocPluginName.grpc;
import static io.spine.tools.gradle.ProtocPluginName.spineProtoc;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.createTempFile;
import static org.gradle.internal.os.OperatingSystem.current;

/**
 * A Gradle plugin that performs additional {@code protoc} configurations relevant for Java
 * projects.
 */
public final class JavaProtocConfigurationPlugin extends ProtocConfigurationPlugin {

    private static final String JAR_EXTENSION = "jar";
    private static final String GRPC_GROUP = "io.grpc";
    private static final String GRPC_PLUGIN_NAME = "protoc-gen-grpc-java";
    private static final String DOT_SH = ".sh";
    private static final String DOT_BAT = ".bat";
    private static final String SPINE_PLUGIN_NAME = "spine-protoc-plugin";
    private static final Pattern JAR_FILE_INSERTION_POINT = Pattern.compile("\\{jar-file}");

    @Override
    protected void
    configureProtocPlugins(NamedDomainObjectContainer<ExecutableLocator> plugins, Project project) {
        plugins.create(grpc.name(),
                       locator -> locator.setArtifact(Artifact.newBuilder()
                                                              .setGroup(GRPC_GROUP)
                                                              .setName(GRPC_PLUGIN_NAME)
                                                              .setVersion(VERSIONS.grpc())
                                                              .build()
                                                              .notation()));
        project.afterEvaluate(p -> {
            Dependency dependency = pluginJarDependency(project);
            Path jarFile = project.getConfigurations()
                                  .getByName(fetch.name())
                                  .fileCollection(dependency)
                                  .getSingleFile()
                                  .toPath()
                                  .toAbsolutePath();
            plugins.create(spineProtoc.name(),
                           locator -> locator.setPath(tryWriteLaunchScript(jarFile).toString()));
        });
    }

    private static Dependency pluginJarDependency(Project project) {
        Configuration fetch = project.getConfigurations()
                                     .maybeCreate(ConfigurationName.fetch.name());
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
        return protocPluginDependency;
    }

    private static Path tryWriteLaunchScript(Path jarFile) {
        try {
            return writeLaunchScript(jarFile);
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    private static Path writeLaunchScript(Path jarFile) throws IOException {
        boolean windows = current().isWindows();
        String scriptExt = windows ? DOT_BAT : DOT_SH;
        Resource launcherTemplate = Resource.file("plugin_runner" + scriptExt);
        String template = launcherTemplate.read();
        Matcher matcher = JAR_FILE_INSERTION_POINT.matcher(template);
        String script = matcher.replaceAll(jarFile.toString());
        Path path = createTempFile(JavaProtocConfigurationPlugin.class.getSimpleName(), scriptExt);
        File file = path.toFile();
        CharSink sink = asCharSink(file, UTF_8);
        sink.write(script);
        boolean canBeExecuted = file.setExecutable(true, false);
        checkState(canBeExecuted, "Failed to make file `%s` executable.", file);
        return path;
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
        TaskName writeRefName = tests
                                ? writeTestDescriptorReference
                                : writeDescriptorReference;
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
        GradleTask writeRef = newTask(writeRefName, task -> {
            DescriptorReference reference = DescriptorReference.toOneFile(descriptorFile);
            reference.writeTo(resourceDirectory);
        }).allowNoDependencies()
          .applyNowTo(project);
        protocTask.finalizedBy(writeRef.getTask());
    }

    @Override
    protected File getTestDescriptorSet(Project project) {
        return Extension.getTestDescriptorSet(project);
    }

    @Override
    protected Path generatedFilesBaseDir(Project project) {
        DefaultJavaProject javaProject = at(project.getProjectDir());
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
        byte[] valueBytes = value.getBytes(Charsets.UTF_8);
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
