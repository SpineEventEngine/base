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

package io.spine.tools.gradle;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.gradle.ExecutableLocator;
import com.google.protobuf.gradle.GenerateProtoTask;
import com.google.protobuf.gradle.ProtobufConfigurator;
import com.google.protobuf.gradle.ProtobufConfigurator.GenerateProtoTaskCollection;
import com.google.protobuf.gradle.ProtobufConvention;
import io.spine.tools.groovy.GStrings;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.code.fs.java.DefaultJavaProject.at;
import static io.spine.tools.gradle.ConfigurationName.FETCH;
import static io.spine.tools.gradle.ProtobufDependencies.protobufCompiler;
import static io.spine.tools.groovy.ConsumerClosure.closure;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.gradle.internal.os.OperatingSystem.current;

/**
 * An abstract base for Gradle plugins that configure Protobuf compilation.
 *
 * <p>Any extending plugin requires {@code com.google.protobuf} plugin. If it is not applied,
 * no action is performed.
 */
public abstract class ProtocConfigurationPlugin extends SpinePlugin {

    private static final String PLUGIN_ARTIFACT_PROPERTY = "Protoc plugin artifact";

    private static final String GRPC_GROUP = "io.grpc";
    private static final String GRPC_PLUGIN_NAME = "protoc-gen-grpc-java";

    protected static final String SPINE_PLUGIN_NAME = "spine-protoc-plugin";
    private static final String JAR_EXTENSION = "jar";
    private static final String SH_EXTENSION = "sh";
    private static final String BAT_EXTENSION = "bat";
    private static final String SCRIPT_CLASSIFIER = "script";

    private static final DependencyVersions VERSIONS = DependencyVersions.load();

    @Override
    public void apply(Project project) {
        project.getPluginManager()
               .withPlugin(ProtobufDependencies.gradlePlugin()
                                               .value(), plugin -> applyTo(project));
    }

    private void applyTo(Project project) {
        project.getConvention()
               .getPlugin(ProtobufConvention.class)
               .protobuf(closure(
                       (ProtobufConfigurator protobuf) -> configureProtobuf(project, protobuf)
               ));
    }

    private void configureProtobuf(Project project, ProtobufConfigurator protobuf) {
        Path generatedFilesBaseDir = generatedFilesBaseDir(project);
        protobuf.setGeneratedFilesBaseDir(generatedFilesBaseDir.toString());
        String version = VERSIONS.protobuf();
        protobuf.protoc(closure((ExecutableLocator protocLocator) ->
                                        protocLocator.setArtifact(protobufCompiler()
                                                                          .ofVersion(version)
                                                                          .notation())));
        protobuf.plugins(closure(ProtocConfigurationPlugin::configureProtocPlugins));
        GradleTask copyPluginJar = createCopyPluginJarTask(project);
        protobuf.generateProtoTasks(closure(
                (GenerateProtoTaskCollection tasks) -> configureProtocTasks(tasks, copyPluginJar)
        ));
    }

    private void configureProtocTasks(GenerateProtoTaskCollection tasks, GradleTask dependency) {
        // This is a "live" view of the current Gradle tasks.
        Collection<GenerateProtoTask> tasksProxy = tasks.all();

        /*
         *  Creating a hard-copy of "live" view of matching Gradle tasks.
         *
         *  Otherwise a `ConcurrentModificationException` is thrown upon an attempt to
         *  insert a task into the Gradle lifecycle.
         */
        ImmutableList<GenerateProtoTask> allTasks = ImmutableList.copyOf(tasksProxy);
        for (GenerateProtoTask task : allTasks) {
            configureProtocTask(task, dependency.getTask());
        }
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
                                     .maybeCreate(FETCH.value());
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
        Action<Task> action = new CopyPluginJar(project, protocPluginDependency, fetch);
        GradleTask copyPluginJar = newTask(TaskName.copyPluginJar, action)
                .allowNoDependencies()
                .withInputProperty(PLUGIN_ARTIFACT_PROPERTY, protocPluginArtifact.notation())
                .withOutputFiles(project.fileTree(spineDirectory(project)))
                .withOutputFiles(project.fileTree(rootSpineDirectory(project)))
                .applyNowTo(project);
        return copyPluginJar;
    }

    private static File spineDirectory(Project project) {
        return at(project.getProjectDir()).tempArtifacts();
    }

    private static File rootSpineDirectory(Project project) {
        return at(project.getRootDir()).tempArtifacts();
    }

    private void configureProtocTask(GenerateProtoTask protocTask, Task dependency) {
        configureTaskPlugins(protocTask, dependency);
        configureDescriptorSetGeneration(protocTask);
    }

    private void configureDescriptorSetGeneration(GenerateProtoTask protocTask) {
        protocTask.setGenerateDescriptorSet(true);
        boolean tests = isTestsTask(protocTask);
        Project project = protocTask.getProject();
        File descriptor;
        descriptor = tests
                     ? getTestDescriptorSet(project)
                     : getMainDescriptorSet(project);
        GenerateProtoTask.DescriptorSetOptions options = protocTask.getDescriptorSetOptions();
        options.setPath(GStrings.fromPlain(descriptor.getPath()));
        options.setIncludeImports(true);
        options.setIncludeSourceInfo(true);

        configureDescriptorSetGeneration(protocTask, descriptor);
    }

    /** Obtains the location of the {@code generated} directory of the specified project. */
    protected abstract Path generatedFilesBaseDir(Project project);

    /** Obtains the merged descriptor set file of the {@code main} module. */
    protected abstract File getMainDescriptorSet(Project project);

    /** Obtains the merged descriptor set file of the {@code test} module. */
    protected abstract File getTestDescriptorSet(Project project);

    /** Allows subclasses to specify additional descriptor set generations. */
    protected abstract void configureDescriptorSetGeneration(GenerateProtoTask task,
                                                             File descriptor);

    protected abstract void configureTaskPlugins(GenerateProtoTask protocTask, Task dependency);

    protected static boolean isTestsTask(GenerateProtoTask protocTask) {
        return protocTask.getSourceSet()
                         .getName()
                         .contains(SourceScope.test.name());
    }

    protected enum ProtocPlugin {

        GRPC("grpc"),
        SPINE("spineProtoc");

        @SuppressWarnings("PMD.SingularField") /* Accessed from the outer class. */
        private final String name;

        ProtocPlugin(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * Downloads and lays out the {@code protoc} plugin executable JAR.
     */
    private static final class CopyPluginJar implements Action<Task> {

        private final Project project;
        private final Dependency protocPluginDependency;
        private final Configuration fetch;

        private CopyPluginJar(Project project,
                              Dependency protocPlugin,
                              Configuration fetch) {
            this.project = project;
            this.protocPluginDependency = protocPlugin;
            this.fetch = fetch;
        }

        @Override
        public void execute(Task task) {
            File executableJar = fetch.fileCollection(protocPluginDependency)
                                      .getSingleFile();
            File spineDir = spineDirectory(project);
            File rootSpineDir = rootSpineDirectory(project);
            copy(executableJar, spineDir);
            copy(executableJar, rootSpineDir);
        }

        private static void copy(File file, File destinationDir) {
            try {
                destinationDir.mkdirs();
                Path destination = destinationDir.toPath()
                                                 .resolve(file.getName());
                Files.copy(file.toPath(), destination, REPLACE_EXISTING);
            } catch (IOException e) {
                throw new GradleException("Failed to copy Spine Protoc executable JAR.", e);
            }
        }
    }
}
