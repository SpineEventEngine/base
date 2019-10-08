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

import com.google.common.collect.ImmutableCollection;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.gen.Indent;
import io.spine.code.proto.FileSet;
import io.spine.code.proto.SourceProtoBelongsToModule;
import io.spine.tools.compiler.gen.GeneratedTypeSpec;
import io.spine.tools.compiler.gen.column.EntityStateWithColumns;
import io.spine.tools.gradle.CodeGenerationAction;
import io.spine.tools.gradle.GradleTask;
import io.spine.tools.gradle.ProtoPlugin;
import io.spine.type.MessageType;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.File;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static io.spine.code.proto.ColumnOption.hasColumns;
import static io.spine.code.proto.TypeSet.topLevelMessages;
import static io.spine.tools.gradle.JavaTaskName.compileJava;
import static io.spine.tools.gradle.JavaTaskName.compileTestJava;
import static io.spine.tools.gradle.ModelCompilerTaskName.generateColumns;
import static io.spine.tools.gradle.ModelCompilerTaskName.generateTestColumns;
import static io.spine.tools.gradle.ModelCompilerTaskName.mergeDescriptorSet;
import static io.spine.tools.gradle.ModelCompilerTaskName.mergeTestDescriptorSet;
import static io.spine.tools.gradle.compiler.Extension.getMainDescriptorSet;
import static io.spine.tools.gradle.compiler.Extension.getMainProtoSrcDir;
import static io.spine.tools.gradle.compiler.Extension.getTargetGenColumnsRootDir;
import static io.spine.tools.gradle.compiler.Extension.getTargetTestGenColumnsRootDir;
import static io.spine.tools.gradle.compiler.Extension.getTestDescriptorSet;
import static io.spine.tools.gradle.compiler.Extension.getTestProtoSrcDir;

public class ColumnGenPlugin extends ProtoPlugin {

    /**
     * Applies the plug-in to a project.
     */
    @Override
    public void apply(Project project) {

        Action<Task> mainScopeAction =
                createAction(project,
                             mainProtoFiles(project),
                             () -> getTargetGenColumnsRootDir(project),
                             () -> getMainProtoSrcDir(project));
        ProtoModule module = new ProtoModule(project);
        GradleTask mainTask =
                newTask(generateColumns, mainScopeAction)
                        .insertAfterTask(mergeDescriptorSet)
                        .insertBeforeTask(compileJava)
                        .withInputFiles(module.protoSource())
                        .withOutputFiles(module.compiledColumns())
                        .applyNowTo(project);
        Action<Task> testScopeAction =
                createAction(project,
                             testProtoFiles(project),
                             () -> getTargetTestGenColumnsRootDir(project),
                             () -> getTestProtoSrcDir(project));

        GradleTask testTask =
                newTask(generateTestColumns, testScopeAction)
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

    private static Action<Task> createAction(Project project,
                                             Supplier<FileSet> files,
                                             Supplier<String> targetDirPath,
                                             Supplier<String> protoSrcDir) {
        return new GenAction(project, files, targetDirPath, protoSrcDir);
    }

    @Override
    protected Supplier<File> mainDescriptorFile(Project project) {
        return () -> getMainDescriptorSet(project);
    }

    @Override
    protected Supplier<File> testDescriptorFile(Project project) {
        return () -> getTestDescriptorSet(project);
    }

    /**
     * Generates source code of rejections.
     */
    private static class GenAction extends CodeGenerationAction {

        private GenAction(Project project,
                          Supplier<FileSet> files,
                          Supplier<String> targetDirPath,
                          Supplier<String> protoSrcDirPath) {
            super(project, files, targetDirPath, protoSrcDirPath);
        }

        @Override
        public void execute(Task task) {
            Predicate<FileDescriptor> belongsToModule =
                    new SourceProtoBelongsToModule(protoSrcDir()).forDescriptor();
            FileSet fileSet = protoFiles().get()
                                          .filter(belongsToModule);
            ImmutableCollection<MessageType> types = topLevelMessages(fileSet);
            types.forEach(type -> {
                if (hasColumns(type)) {
                    GeneratedTypeSpec spec = new EntityStateWithColumns(type);
                    spec.writeToFile(targetDir(), indent());
                }
            });
        }

        @Override
        protected Indent getIndent(Project project) {
            return Extension.getIndent(project);
        }
    }
}
