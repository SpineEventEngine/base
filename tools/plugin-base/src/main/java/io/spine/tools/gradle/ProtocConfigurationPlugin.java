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

package io.spine.tools.gradle;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import com.google.protobuf.gradle.ExecutableLocator;
import com.google.protobuf.gradle.GenerateProtoTask;
import com.google.protobuf.gradle.ProtobufConfigurator;
import com.google.protobuf.gradle.ProtobufConfigurator.GenerateProtoTaskCollection;
import com.google.protobuf.gradle.ProtobufConvention;
import io.spine.tools.groovy.GStrings;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;

import static io.spine.tools.gradle.ProtobufDependencies.gradlePlugin;
import static io.spine.tools.gradle.ProtobufDependencies.protobufCompiler;
import static io.spine.tools.gradle.ProtocPluginName.spineProtoc;
import static io.spine.tools.groovy.ConsumerClosure.closure;
import static org.gradle.internal.os.OperatingSystem.current;

/**
 * An abstract base for Gradle plugins that configure Protobuf compilation.
 *
 * <p>Any extending plugin requires {@code com.google.protobuf} plugin. If it is not applied,
 * no action is performed.
 */
public abstract class ProtocConfigurationPlugin extends SpinePlugin {

    protected static final String SPINE_PLUGIN_NAME = "spine-protoc-plugin";
    private static final String SH_EXTENSION = "sh";
    private static final String BAT_EXTENSION = "bat";
    private static final String SCRIPT_CLASSIFIER = "script";

    protected static final DependencyVersions VERSIONS = DependencyVersions.get();

    @Override
    public void apply(Project project) {
        project.getPluginManager()
               .withPlugin(gradlePlugin().value(), plugin -> applyTo(project));
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
        protobuf.plugins(closure(this::configureProtocPlugins));
        protobuf.generateProtoTasks(closure(this::configureProtocTasks));
    }

    private void configureProtocTasks(GenerateProtoTaskCollection tasks) {
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
            configureProtocTask(task);
        }
    }

    /**
     * Adds plugins related to the {@code protoc}.
     *
     * @param plugins
     *         container of all plugins
     * @apiNote overriding methods must invoke super to add the {@code spineProtoc} plugin,
     *         which
     *         is a required plugin
     */
    @OverridingMethodsMustInvokeSuper
    protected void configureProtocPlugins(NamedDomainObjectContainer<ExecutableLocator> plugins) {
        if (!spineProtocIsPresent(plugins)) {
            plugins.create(spineProtoc.name(), locator -> {
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
    }

    private static boolean
    spineProtocIsPresent(NamedDomainObjectContainer<ExecutableLocator> plugins) {
        return plugins.findByName(spineProtoc.name()) != null;
    }

    private void configureProtocTask(GenerateProtoTask protocTask) {
        configureDescriptorSetGeneration(protocTask);
        customizeTask(protocTask);
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
    }

    /**
     * Allows subclasses to specify additional generation task settings.
     *
     * @param protocTask
     *         code generation task
     */
    @SuppressWarnings("NoopMethodInAbstractClass")
    protected void customizeTask(GenerateProtoTask protocTask) {
        // NO-OP by default.
    }

    /** Obtains the location of the {@code generated} directory of the specified project. */
    protected abstract Path generatedFilesBaseDir(Project project);

    /** Obtains the merged descriptor set file of the {@code main} module. */
    protected abstract File getMainDescriptorSet(Project project);

    /** Obtains the merged descriptor set file of the {@code test} module. */
    protected abstract File getTestDescriptorSet(Project project);

    protected static boolean isTestsTask(GenerateProtoTask protocTask) {
        return protocTask.getSourceSet()
                         .getName()
                         .contains(SourceScope.test.name());
    }
}
