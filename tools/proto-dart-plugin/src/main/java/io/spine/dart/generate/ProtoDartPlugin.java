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

package io.spine.dart.generate;

import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import io.spine.code.proto.FileDescriptors;
import io.spine.code.proto.FileSet;
import io.spine.code.proto.TypeSet;
import io.spine.dart.knowntypes.KnownTypesBuilder;
import io.spine.io.Resource;
import io.spine.tools.gradle.SourceScope;
import io.spine.tools.gradle.SpinePlugin;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static io.spine.tools.gradle.ProtocPluginName.dart;
import static io.spine.tools.gradle.SourceScope.main;
import static io.spine.tools.gradle.SourceScope.test;
import static io.spine.tools.gradle.TaskName.assemble;
import static io.spine.tools.gradle.TaskName.generateDartTestTypeRegistry;
import static io.spine.tools.gradle.TaskName.generateDartTypeRegistry;
import static io.spine.tools.gradle.TaskName.testClasses;

/**
 * A Gradle plugin which configures Protobuf Dart code generation.
 *
 * <p>Generates mapping between Protobuf type URLs and Dart types and reflective descriptors
 * (a.k.a. {@code BuilderInfo}s).
 *
 * @see DartProtocConfigurationPlugin
 */
public final class ProtoDartPlugin extends SpinePlugin {

    private static final Resource TEMPLATE = Resource.file("types.template.dart");

    @Override
    public void apply(Project project) {
        Extension extension = new Extension(project);
        extension.registerIn(project);
        newTask(generateDartTypeRegistry, createAction(extension))
                .insertBeforeTask(assemble)
                .withOutputFiles(project.files(extension.getMainTypeRegistry()))
                .applyNowTo(project);
        newTask(generateDartTestTypeRegistry, createTestAction(extension))
                .insertBeforeTask(testClasses)
                .withOutputFiles(project.files(extension.getTestTypeRegistry()))
                .applyNowTo(project);
        Plugin<Project> protocConfig = new DartProtocConfigurationPlugin();
        protocConfig.apply(project);
    }

    private static Action<Task> createAction(Extension extension) {
        return t -> generateTypeRegistry(extension,
                                         extension.mainDescriptorSetFile(),
                                         extension.mainTypeRegistryFile(),
                                         main);
    }

    private static Action<Task> createTestAction(Extension extension) {
        return t -> generateTypeRegistry(extension,
                                         extension.testDescriptorSetFile(),
                                         extension.testTypeRegistryFile(),
                                         test);
    }

    private static void
    generateTypeRegistry(Extension extension,
                         File descriptorsFile,
                         File targetFile,
                         SourceScope scope) {
        List<FileDescriptorProto> fileDescriptors = FileDescriptors.parse(descriptorsFile);
        TypeSet types = TypeSet.from(FileSet.ofFiles(fileDescriptors));
        Path generatedDir = extension.generatedDirPath()
                                     .resolve(scope.name())
                                     .resolve(dart.name());
        Path relativeGeneratedDir = targetFile.toPath()
                                              .relativize(generatedDir);
        String packageName = extension.packageName();
        CodeTemplate template = new CodeTemplate(TEMPLATE);
        GeneratedDartFile file = KnownTypesBuilder
                .newBuilder()
                .setKnownTypes(types)
                .setGeneratedProtoDir(relativeGeneratedDir)
                .setTemplate(template)
                .setPackageName(packageName)
                .buildAsSourceFile();
        file.writeTo(targetFile);
    }
}
