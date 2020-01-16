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

import io.spine.tools.gradle.GradleTask;
import io.spine.tools.gradle.ProtoPlugin;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.File;
import java.util.function.Supplier;

import static io.spine.tools.gradle.JavaTaskName.compileJava;
import static io.spine.tools.gradle.JavaTaskName.compileTestJava;
import static io.spine.tools.gradle.ModelCompilerTaskName.generateColumnInterfaces;
import static io.spine.tools.gradle.ModelCompilerTaskName.generateTestColumnInterfaces;
import static io.spine.tools.gradle.ModelCompilerTaskName.mergeDescriptorSet;
import static io.spine.tools.gradle.ModelCompilerTaskName.mergeTestDescriptorSet;
import static io.spine.tools.gradle.compiler.Extension.getMainDescriptorSet;
import static io.spine.tools.gradle.compiler.Extension.getMainProtoSrcDir;
import static io.spine.tools.gradle.compiler.Extension.getTargetGenColumnsRootDir;
import static io.spine.tools.gradle.compiler.Extension.getTargetTestGenColumnsRootDir;
import static io.spine.tools.gradle.compiler.Extension.getTestDescriptorSet;
import static io.spine.tools.gradle.compiler.Extension.getTestProtoSrcDir;

/**
 * A plugin that generates helper interfaces for declaring entity columns.
 *
 * @see io.spine.base.EntityWithColumns
 */
public class ColumnGenPlugin extends ProtoPlugin {

    /**
     * Applies the plug-in to a project.
     */
    @Override
    public void apply(Project project) {

        Action<Task> mainScopeAction =
                new ColumnGenAction(project,
                                    mainProtoFiles(project),
                                    () -> getTargetGenColumnsRootDir(project),
                                    () -> getMainProtoSrcDir(project));
        ProtoModule module = new ProtoModule(project);
        GradleTask mainTask =
                newTask(generateColumnInterfaces, mainScopeAction)
                        .insertAfterTask(mergeDescriptorSet)
                        .insertBeforeTask(compileJava)
                        .withInputFiles(module.protoSource())
                        .withOutputFiles(module.compiledColumns())
                        .applyNowTo(project);
        Action<Task> testScopeAction =
                new ColumnGenAction(project,
                                    testProtoFiles(project),
                                    () -> getTargetTestGenColumnsRootDir(project),
                                    () -> getTestProtoSrcDir(project));

        GradleTask testTask =
                newTask(generateTestColumnInterfaces, testScopeAction)
                        .insertAfterTask(mergeTestDescriptorSet)
                        .insertBeforeTask(compileTestJava)
                        .withInputFiles(module.protoSource())
                        .withInputFiles(module.testProtoSource())
                        .withOutputFiles(module.compiledRejections())
                        .withOutputFiles(module.testCompiledColumns())
                        .applyNowTo(project);

        _debug().log("Column generation phase initialized with tasks: `%s`, `%s`.",
                     mainTask, testTask);
    }

    @Override
    protected Supplier<File> mainDescriptorFile(Project project) {
        return () -> getMainDescriptorSet(project);
    }

    @Override
    protected Supplier<File> testDescriptorFile(Project project) {
        return () -> getTestDescriptorSet(project);
    }
}
