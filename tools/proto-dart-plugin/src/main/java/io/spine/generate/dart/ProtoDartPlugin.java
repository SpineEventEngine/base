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

package io.spine.generate.dart;

import com.google.common.collect.ImmutableMap;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import io.spine.code.proto.FileDescriptors;
import io.spine.code.proto.FileSet;
import io.spine.code.proto.ProtoBelongsToModule;
import io.spine.code.proto.TypeSet;
import io.spine.io.Resource;
import io.spine.tools.gradle.SpinePlugin;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.Copy;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static io.spine.tools.gradle.ProtocPluginName.dart;
import static io.spine.tools.gradle.SourceScope.main;
import static io.spine.tools.gradle.SourceScope.test;
import static io.spine.tools.gradle.TaskName.classes;
import static io.spine.tools.gradle.TaskName.copyGeneratedDart;
import static io.spine.tools.gradle.TaskName.copyTestGeneratedDart;
import static io.spine.tools.gradle.TaskName.generateDartTestTypeRegistry;
import static io.spine.tools.gradle.TaskName.generateDartTypeRegistry;
import static io.spine.tools.gradle.TaskName.generateProto;
import static io.spine.tools.gradle.TaskName.generateTestProto;
import static io.spine.tools.gradle.TaskName.testClasses;
import static org.gradle.api.Task.TASK_TYPE;

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
    private static final String TYPES_FILE_NAME = "types.dart";

    @Override
    public void apply(Project project) {
        Extension extension = new Extension(project);
        extension.register();

        Plugin<Project> protocConfig = new DartProtocConfigurationPlugin();
        protocConfig.apply(project);

        createMainCopyTask(project, extension);
        createTestCopyTask(project, extension);

        newTask(generateDartTypeRegistry, createAction(extension))
                .insertAfterTask(copyGeneratedDart)
                .insertBeforeTask(classes)
                .withOutputFiles(project.files(extension.getMainGeneratedDir()
                                                        .file(TYPES_FILE_NAME)))
                .applyNowTo(project);
        newTask(generateDartTestTypeRegistry, createTestAction(extension))
                .insertAfterTask(copyTestGeneratedDart)
                .insertBeforeTask(testClasses)
                .withOutputFiles(project.files(extension.getTestGeneratedDir()
                                                        .file(TYPES_FILE_NAME)))
                .applyNowTo(project);
    }

    private static void createMainCopyTask(Project project, Extension extension) {
        Copy task = (Copy) project.task(ImmutableMap.of(TASK_TYPE, Copy.class),
                                        copyGeneratedDart.name());
        task.from(extension.getGeneratedBaseDir()
                           .dir(main.name() + File.separator + dart.name()));
        task.into(extension.getLibDir());
        task.dependsOn(generateProto.name());
    }

    private static void createTestCopyTask(Project project, Extension extension) {
        Copy task = (Copy) project.task(ImmutableMap.of(TASK_TYPE, Copy.class),
                                        copyTestGeneratedDart.name());
        task.from(extension.getGeneratedBaseDir()
                           .dir(test.name() + File.separator + dart.name()));
        task.into(extension.getTestDir());
        task.dependsOn(generateTestProto.name());
    }

    private Action<Task> createAction(Extension extension) {
        return t -> generateTypeRegistry(extension,
                                         extension.mainDescriptorSetFile(),
                                         extension.libDir(),
                                         extension.mainGeneratedDir());
    }

    private Action<Task> createTestAction(Extension extension) {
        return t -> generateTypeRegistry(extension,
                                         extension.testDescriptorSetFile(),
                                         extension.testDir(),
                                         extension.testGeneratedDir());
    }

    private void
    generateTypeRegistry(Extension extension,
                         File descriptorsFile,
                         Path baseSourcePath,
                         Path generatedSourcePath) {
        extension.finalizeAll();
        if (descriptorsFile.exists()) {
            doGenerateRegistry(descriptorsFile,
                               baseSourcePath,
                               generatedSourcePath);
        } else {
            logMissingDescriptorSetFile(descriptorsFile);
        }
    }

    private void doGenerateRegistry(File descriptorsFile,
                                    Path baseSourcePath,
                                    Path generatedSourcePath) {
        _fine().log("Generating Dart known types registry from descriptor `%s`.",
                    descriptorsFile);
        Path targetFile = generatedSourcePath.resolve(TYPES_FILE_NAME);
        List<FileDescriptorProto> fileDescriptors = FileDescriptors.parse(descriptorsFile);
        ProtoBelongsToModule predicate = new CompiledProtoBelongsToModule(generatedSourcePath);
        FileSet fileSet = FileSet.ofFiles(fileDescriptors)
                                 .filter(predicate.forDescriptor());
        TypeSet types = TypeSet.from(fileSet);
        _finest().log("There are %d known types.", types.size());
        CodeTemplate template = new CodeTemplate(TEMPLATE);
        Path relativeSourcePath = baseSourcePath.relativize(generatedSourcePath);
        GeneratedDartFile file = KnownTypesBuilder
                .newBuilder()
                .setKnownTypes(types)
                .setGeneratedFilesPrefix(relativeSourcePath)
                .setTemplate(template)
                .buildAsSourceFile();
        _fine().log("Storing known types registry to `%s`.", targetFile);
        file.writeTo(targetFile.toFile());
    }
}
