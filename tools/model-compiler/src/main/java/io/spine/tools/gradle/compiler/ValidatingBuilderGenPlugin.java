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

import io.spine.code.generate.Indent;
import io.spine.code.proto.FileSet;
import io.spine.tools.compiler.validation.VBuilderGenerator;
import io.spine.tools.gradle.CodeGenerationAction;
import io.spine.tools.gradle.GradleTask;
import io.spine.tools.gradle.ProtoPlugin;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.File;
import java.util.function.Supplier;

import static io.spine.tools.gradle.TaskName.COMPILE_JAVA;
import static io.spine.tools.gradle.TaskName.COMPILE_TEST_JAVA;
import static io.spine.tools.gradle.TaskName.GENERATE_TEST_VALIDATING_BUILDERS;
import static io.spine.tools.gradle.TaskName.GENERATE_VALIDATING_BUILDERS;
import static io.spine.tools.gradle.TaskName.MERGE_DESCRIPTOR_SET;
import static io.spine.tools.gradle.TaskName.MERGE_TEST_DESCRIPTOR_SET;
import static io.spine.tools.gradle.compiler.Extension.getMainDescriptorSet;
import static io.spine.tools.gradle.compiler.Extension.getMainProtoSrcDir;
import static io.spine.tools.gradle.compiler.Extension.getTargetGenValidatorsRootDir;
import static io.spine.tools.gradle.compiler.Extension.getTargetTestGenValidatorsRootDir;
import static io.spine.tools.gradle.compiler.Extension.getTestDescriptorSet;
import static io.spine.tools.gradle.compiler.Extension.getTestProtoSrcDir;
import static io.spine.tools.gradle.compiler.Extension.isGenerateValidatingBuilders;

/**
 * Plugin which generates validating builders based on the Protobuf Message definitions.
 *
 * <p>Uses generated proto descriptors.
 *
 * <p>Logs a warning if there are no Protobuf descriptors generated.
 *
 * <p>To switch off the generation of the validating builders
 * use the {@code generateValidatingBuilders} property in the {@code modelCompiler} section
 * of a Gradle build file:
 *
 * <pre>{@code
 * modelCompiler {
 *     generateValidatingBuilders = false
 * }
 * }</pre>
 *
 * <p>The default value of the {@code generateValidatingBuilders} property is {@code true}.
 *
 * <p>The indentation for the generated code is done with whitespaces. The default indentation is 4.
 * To set another value, please use the {@code indent} property:
 *
 * <pre>{@code
 * modelCompiler {
 *     indent = 2
 * }
 * }</pre>
 *
 * @see io.spine.validate.ValidatingBuilder
 */
public class ValidatingBuilderGenPlugin extends ProtoPlugin {

    @Override
    public void apply(Project project) {
        _debug("Preparing to generate validating builders.");
        Action<Task> mainScopeAction =
                createAction(project,
                             mainProtoFiles(project),
                             () -> getTargetGenValidatorsRootDir(project),
                             () -> getMainProtoSrcDir(project));
        ProtoModule module = new ProtoModule(project);
        GradleTask generateValidator =
                newTask(GENERATE_VALIDATING_BUILDERS, mainScopeAction)
                        .insertAfterTask(MERGE_DESCRIPTOR_SET)
                        .insertBeforeTask(COMPILE_JAVA)
                        .withInputFiles(module.protoSource())
                        .withOutputFiles(module.validatingBuilders())
                        .applyNowTo(project);
        _debug("Preparing to generate test validating builders.");
        Action<Task> testScopeAction =
                createAction(project,
                             testProtoFiles(project),
                             () -> getTargetTestGenValidatorsRootDir(project),
                             () -> getTestProtoSrcDir(project));

        GradleTask generateTestValidator =
                newTask(GENERATE_TEST_VALIDATING_BUILDERS, testScopeAction)
                        .insertAfterTask(MERGE_TEST_DESCRIPTOR_SET)
                        .insertBeforeTask(COMPILE_TEST_JAVA)
                        .withInputFiles(module.protoSource())
                        .withInputFiles(module.testProtoSource())
                        .withOutputFiles(module.validatingBuilders())
                        .withOutputFiles(module.testValidatingBuilders())
                        .applyNowTo(project);
        _debug("Validating builders generation phase initialized with tasks: {}, {}.",
               generateValidator, generateTestValidator);
    }

    private static Action<Task> createAction(Project project,
                                             Supplier<FileSet> files,
                                             Supplier<String> targetDirPath,
                                             Supplier<String> protoSrcDirPath) {
        return new GenAction(project, files, targetDirPath, protoSrcDirPath);
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
     * Code generation task.
     *
     * @implNote This class uses {@code Supplier}s instead of direct values because at the time
     *           of creation Gradle project is not fully evaluated, and the values
     *           are not yet defined.
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
            if (!isGenerateValidatingBuilders(project())) {
                return;
            }
            VBuilderGenerator generator =
                    new VBuilderGenerator(protoSrcDir(), targetDir(), indent());
            generator.process(protoFiles().get());
        }

        @Override
        protected Indent getIndent(Project project) {
            return Extension.getIndent(project);
        }
    }
}
