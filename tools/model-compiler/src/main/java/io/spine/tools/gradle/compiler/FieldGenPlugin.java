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
import static io.spine.tools.gradle.ModelCompilerTaskName.generateFieldDeclarations;
import static io.spine.tools.gradle.ModelCompilerTaskName.generateTestColumnInterfaces;
import static io.spine.tools.gradle.ModelCompilerTaskName.generateTestFieldDeclarations;
import static io.spine.tools.gradle.compiler.Extension.getMainDescriptorSet;
import static io.spine.tools.gradle.compiler.Extension.getMainProtoSrcDir;
import static io.spine.tools.gradle.compiler.Extension.getTargetGenFieldsRootDir;
import static io.spine.tools.gradle.compiler.Extension.getTargetTestGenFieldsRootDir;
import static io.spine.tools.gradle.compiler.Extension.getTestDescriptorSet;
import static io.spine.tools.gradle.compiler.Extension.getTestProtoSrcDir;

public class FieldGenPlugin extends ProtoPlugin {

    /**
     * Applies the plug-in to a project.
     */
    @Override
    public void apply(Project project) {

        Action<Task> mainScopeAction =
                new FieldGenAction(project,
                                   mainProtoFiles(project),
                                   () -> getTargetGenFieldsRootDir(project),
                                   () -> getMainProtoSrcDir(project));
        ProtoModule module = new ProtoModule(project);
        GradleTask mainTask =
                newTask(generateFieldDeclarations, mainScopeAction)
                        .insertAfterTask(generateColumnInterfaces)
                        .insertBeforeTask(compileJava)
                        .withInputFiles(module.protoSource())
                        .withOutputFiles(module.compiledFields())
                        .applyNowTo(project);
        Action<Task> testScopeAction =
                new FieldGenAction(project,
                                   testProtoFiles(project),
                                   () -> getTargetTestGenFieldsRootDir(project),
                                   () -> getTestProtoSrcDir(project));

        GradleTask testTask =
                newTask(generateTestFieldDeclarations, testScopeAction)
                        .insertAfterTask(generateTestColumnInterfaces)
                        .insertBeforeTask(compileTestJava)
                        .withInputFiles(module.protoSource())
                        .withInputFiles(module.testProtoSource())
                        .withOutputFiles(module.compiledRejections())
                        .withOutputFiles(module.testCompiledFields())
                        .applyNowTo(project);

        _debug().log("Field generation phase initialized with tasks: `%s`, `%s`.",
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
