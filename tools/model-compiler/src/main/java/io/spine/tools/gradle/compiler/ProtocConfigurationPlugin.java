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
import com.google.protobuf.gradle.ProtobufConfigurator;
import com.google.protobuf.gradle.ProtobufConvention;
import groovy.lang.Closure;
import io.spine.tools.gradle.SpinePlugin;
import org.gradle.api.GradleException;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;

import java.io.File;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.code.java.DefaultJavaProject.at;
import static io.spine.tools.gradle.ConfigurationName.FETCH;
import static io.spine.tools.gradle.TaskName.COPY_PLUGIN_JAR;
import static io.spine.tools.gradle.TaskName.GENERATE_PROTO;
import static java.lang.String.format;
import static org.gradle.internal.os.OperatingSystem.current;

public class ProtocConfigurationPlugin extends SpinePlugin {

    private static final String PLUGIN_DEPENDENCY_TEMPLATE =
            "io.spine.tools:spine-protoc-plugin:$%s@jar";

    @Override
    public void apply(Project project) {
        Configuration fetch = project.getConfigurations()
                                     .maybeCreate(FETCH.name());
        // TODO:2019-01-21:dmytro.dashenkov: Provide proper version.
        String dependency = format(PLUGIN_DEPENDENCY_TEMPLATE, "1.0.0-SNAPSHOT");
        Dependency protocPluginDependency = project.getDependencies()
                                                   .add(fetch.getName(), dependency);
        checkNotNull(protocPluginDependency,
                     "Could not create dependency %s %s", fetch.getName(), dependency);
        newTask(COPY_PLUGIN_JAR, task -> copyPluginExecutables(project, protocPluginDependency, fetch))
                .insertBeforeTask(GENERATE_PROTO)
                .applyNowTo(project);

        ProtobufConfigurator protobuf = project.getConvention()
                                               .getPlugin(ProtobufConvention.class)
                                               .getProtobuf();
        protobuf.setGeneratedFilesBaseDir(at(project.getProjectDir()).generated()
                                                                     .toString());
        protobuf.protoc(new ProtocConfiguration());
        protobuf.plugins(new PluginConfiguration());

        protobuf.generateProtoTasks(new ProtoTaskConfiguration());
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
            Files.copy(file, destinationDir);
        } catch (IOException e) {
            throw new GradleException("Failed to copy Spine Protoc executable JAR.", e);
        }
    }

    private static final class ProtocConfiguration extends Closure {

        private static final long serialVersionUID = 0L;

        private ProtocConfiguration() {
            super(null);
        }

        private void doCall(ExecutableLocator protocLocator) {
            protocLocator.setArtifact("com.google.protobuf:protoc:3.6.1");
        }
    }

    private static final class PluginConfiguration extends Closure {

        private static final long serialVersionUID = 0L;

        private PluginConfiguration() {
            super(null);
        }

        private void doCall(NamedDomainObjectContainer<ExecutableLocator> plugins) {
            // TODO:2019-01-21:dmytro.dashenkov: Version.
            plugins.create("grpc", locator -> locator.setArtifact("io.grpc:protoc-gen-grpc-java:1.15.0"));
            plugins.create("spineProtoc", locator -> {
                boolean windows = current().isWindows();
                String scriptExt = windows ? "bat" : "sh";
                locator.setArtifact("io.spine.tools:spine-protoc-plugin:" + "1.0.0-SNAPSHOT:" + "script@" + scriptExt);
            });
        }
    }

    private static final class ProtoTaskConfiguration extends Closure {

        private ProtoTaskConfiguration() {
            super(null);
        }

        private void doCall() {

        }
    }
}
