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

package io.spine.tools.mc.js.gradle;

import com.google.common.collect.ImmutableList;
import io.spine.code.proto.FileSet;
import io.spine.tools.fs.ExternalModules;
import io.spine.tools.gradle.BaseTaskName;
import io.spine.tools.gradle.GradleTask;
import io.spine.tools.gradle.ProtoPlugin;
import io.spine.tools.js.fs.DefaultJsProject;
import io.spine.tools.js.fs.Directory;
import io.spine.tools.mc.js.code.index.CreateParsers;
import io.spine.tools.mc.js.code.index.GenerateIndexFile;
import io.spine.tools.mc.js.code.task.AppendTypeUrlGetter;
import io.spine.tools.mc.js.code.task.GenerationTask;
import io.spine.tools.mc.js.code.task.ResolveImports;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.File;
import java.util.List;
import java.util.function.Supplier;

import static io.spine.tools.gradle.BaseTaskName.build;
import static io.spine.tools.gradle.ProtoJsTaskName.generateJsonParsers;
import static io.spine.tools.mc.js.gradle.Extension.extension;

/**
 * The Gradle plugin which performs additional code generation for Protobuf types.
 *
 * <p>To run the plugin, add the {@code "io.spine.tools:spine-mc-js:$spineBaseVersion"}
 * to the {@code classpath} configuration and add
 * {@code apply plugin: 'io.spine.mc-js'} to the module generating JS messages.
 *
 * <p>In particular, the plugin:
 * <ul>
 *     <li>Generates a {@linkplain AppendTypeUrlGetter getter}
 *         to obtain a {@code TypeUrl} for each type.
 *     <li>Generates {@linkplain CreateParsers parsers} for types
 *         with standard JSON mapping.
 *     <li>{@linkplain GenerateIndexFile Exposes} all the messages and enums
 *         as well as generated parsers (to be used by the Spine Web).
 * </ul>
 *
 * <p>The main plugin action may be retrieved and configured as necessary via the
 * {@linkplain Extension "protoJs" extension}. By default, the action is a dependency of the
 * {@linkplain BaseTaskName#build build} task.
 *
 * <p>This plugin currently relies on the set of the hard-coded Gradle settings which have to be
 * set to the required values in a project willing to use the plugin. These settings are:
 * <ol>
 *     <li>CommonJS import style for all generated code:
 *         {@code js {option "import_style=commonjs"}};
 * </ol>
 *
 * <p>The {@code build.gradle} file located under the {@code test/resources} folder of this module
 * can be used as an example of the required project configuration.
 */
public class McJsPlugin extends ProtoPlugin {

    private static final String EXTENSION_NAME = "protoJs";

    @Override
    public void apply(Project project) {
        ProtocConfig configPlugin = new ProtocConfig();
        configPlugin.apply(project);
        Extension extension = project.getExtensions()
                                     .create(EXTENSION_NAME, Extension.class);
        Action<Task> action = newAction(project);
        GradleTask newTask = newTask(generateJsonParsers, action)
                .insertBeforeTask(build)
                .applyNowTo(project);

        Task task = newTask.getTask();
        extension.setGenerateParsersTask(task);
    }

    /**
     * Creates an {@code Action} to perform the additional generation of code
     * for working with Protobuf types.
     *
     * <p>The action handles both main and test scopes.
     *
     * <p>The paths to the generated JS messages location, as well as to the descriptor set file,
     * are currently hard-coded.
     *
     * <p>See {@link DefaultJsProject} for the expected configuration.
     */
    private Action<Task> newAction(Project project) {
        return task -> generateJsonParsers(project);
    }

    private void generateJsonParsers(Project project) {
        generateForMain(project);
        generateForTest(project);
    }

    @Override
    protected Supplier<File> mainDescriptorFile(Project project) {
        return () -> Extension.getMainDescriptorSet(project);
    }

    @Override
    protected Supplier<File> testDescriptorFile(Project project) {
        return () -> Extension.getTestDescriptorSet(project);
    }

    private void generateForMain(Project project) {
        Directory generatedRoot = Extension.getMainGenProto(project);
        Supplier<FileSet> files = mainProtoFiles(project);
        ExternalModules modules = extension(project).modules();
        generateCode(generatedRoot, files, modules);
    }

    private void generateForTest(Project project) {
        Directory generatedRoot = Extension.getTestGenProtoDir(project);
        Supplier<FileSet> files = testProtoFiles(project);
        ExternalModules modules = extension(project).modules();
        generateCode(generatedRoot, files, modules);
    }

    private static void generateCode(Directory generatedRoot,
                                     Supplier<FileSet> files,
                                     ExternalModules modules) {
        List<GenerationTask> tasks = ImmutableList.of(
                new CreateParsers(generatedRoot),
                new AppendTypeUrlGetter(generatedRoot),
                new GenerateIndexFile(generatedRoot),
                new ResolveImports(generatedRoot, modules)
        );
        FileSet suppliedFiles = files.get();
        for (GenerationTask task : tasks) {
            task.performFor(suppliedFiles);
        }
    }

    /**
     * Obtains the extension name of the plugin.
     */
    static String extensionName() {
        return EXTENSION_NAME;
    }
}
