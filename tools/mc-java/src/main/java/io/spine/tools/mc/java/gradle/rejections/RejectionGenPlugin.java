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
package io.spine.tools.mc.java.gradle.rejections;

import io.spine.code.proto.FileSet;
import io.spine.tools.mc.java.gradle.Extension;
import io.spine.tools.gradle.GradleTask;
import io.spine.tools.gradle.ProtoPlugin;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.File;
import java.util.function.Supplier;

import static io.spine.tools.gradle.JavaTaskName.compileJava;
import static io.spine.tools.gradle.JavaTaskName.compileTestJava;
import static io.spine.tools.gradle.ModelCompilerTaskName.generateRejections;
import static io.spine.tools.gradle.ModelCompilerTaskName.generateTestRejections;
import static io.spine.tools.gradle.ModelCompilerTaskName.mergeDescriptorSet;
import static io.spine.tools.gradle.ModelCompilerTaskName.mergeTestDescriptorSet;

/**
 * Plugin which generates Rejections declared in {@code rejections.proto} files.
 *
 * <p>Uses generated proto descriptors.
 *
 * <p>Logs a warning if there are no protobuf descriptors generated.
 */
public class RejectionGenPlugin extends ProtoPlugin {

    /**
     * Applies the plug-in to a project.
     *
     * <p>Adds {@code :generateRejections} and {@code :generateTestRejections} tasks.
     *
     * <p>Tasks depend on corresponding {@code :generateProto} tasks and are executed
     * before corresponding {@code :compileJava} tasks.
     */
    @Override
    public void apply(Project project) {
        Extension extension = Extension.of(project);
        Action<Task> mainScopeAction =
                createAction(project,
                             mainProtoFiles(project),
                             extension::generatedMainRejectionsDir,
                             extension::mainProtoSrcDir);
        ProtoModule module = new ProtoModule(project);
        GradleTask mainTask =
                newTask(generateRejections, mainScopeAction)
                        .insertAfterTask(mergeDescriptorSet)
                        .insertBeforeTask(compileJava)
                        .withInputFiles(module.protoSource())
                        .withOutputFiles(module.compiledRejections())
                        .applyNowTo(project);
        Action<Task> testScopeAction =
                createAction(project,
                             testProtoFiles(project),
                             extension::generatedTestRejectionsDir,
                             () -> extension.testProtoDir());

        GradleTask testTask =
                newTask(generateTestRejections, testScopeAction)
                        .insertAfterTask(mergeTestDescriptorSet)
                        .insertBeforeTask(compileTestJava)
                        .withInputFiles(module.protoSource())
                        .withInputFiles(module.testProtoSource())
                        .withOutputFiles(module.compiledRejections())
                        .withOutputFiles(module.testCompiledRejections())
                        .applyNowTo(project);

        _debug().log("Rejection generation phase initialized with tasks: `%s`, `%s`.",
                     mainTask, testTask);
    }

    private static Action<Task> createAction(Project project,
                                             Supplier<FileSet> files,
                                             Supplier<String> targetDirPath,
                                             Supplier<String> protoSrcDir) {
        return new RejectionGenAction(project, files, targetDirPath, protoSrcDir);
    }

    @Override
    protected Supplier<File> mainDescriptorFile(Project project) {
        return () -> Extension.of(project).mainDescriptorSetFile();
    }

    @Override
    protected Supplier<File> testDescriptorFile(Project project) {
        return () -> Extension.of(project).testDescriptorSetFile();
    }
}
