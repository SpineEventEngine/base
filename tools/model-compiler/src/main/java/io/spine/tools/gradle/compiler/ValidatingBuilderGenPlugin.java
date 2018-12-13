/*
 * Copyright 2018, TeamDev. All rights reserved.
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

import io.spine.code.Indent;
import io.spine.tools.compiler.validation.VBuilderGenerator;
import io.spine.tools.gradle.GradleTask;
import io.spine.tools.gradle.SpinePlugin;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.slf4j.Logger;

import java.io.File;

import static io.spine.tools.gradle.TaskName.COMPILE_JAVA;
import static io.spine.tools.gradle.TaskName.COMPILE_TEST_JAVA;
import static io.spine.tools.gradle.TaskName.GENERATE_PROTO;
import static io.spine.tools.gradle.TaskName.GENERATE_TEST_PROTO;
import static io.spine.tools.gradle.TaskName.GENERATE_TEST_VALIDATING_BUILDERS;
import static io.spine.tools.gradle.TaskName.GENERATE_VALIDATING_BUILDERS;
import static io.spine.tools.gradle.compiler.Extension.getIndent;
import static io.spine.tools.gradle.compiler.Extension.getMainDescriptorSetPath;
import static io.spine.tools.gradle.compiler.Extension.getMainProtoSrcDir;
import static io.spine.tools.gradle.compiler.Extension.getTargetGenValidatorsRootDir;
import static io.spine.tools.gradle.compiler.Extension.getTargetTestGenValidatorsRootDir;
import static io.spine.tools.gradle.compiler.Extension.getTestDescriptorSetPath;
import static io.spine.tools.gradle.compiler.Extension.getTestProtoSrcDir;
import static io.spine.tools.gradle.compiler.Extension.isGenerateValidatingBuilders;
import static io.spine.tools.gradle.compiler.Extension.isGenerateValidatingBuildersFromClasspath;

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
 * @see io.spine.validate.AbstractValidatingBuilder
 */
public class ValidatingBuilderGenPlugin extends SpinePlugin {

    @Override
    public void apply(Project project) {
        Logger log = log();
        log.debug("Preparing to generate validating builders.");
        Action<Task> mainScopeAction =
                createAction(project,
                             getMainDescriptorSetPath(project),
                             getTargetGenValidatorsRootDir(project),
                             getMainProtoSrcDir(project));

        logDependingTask(
                GENERATE_VALIDATING_BUILDERS,
                         COMPILE_JAVA,
                         GENERATE_PROTO);

        GradleTask generateValidator =
                newTask(GENERATE_VALIDATING_BUILDERS, mainScopeAction)
                        .insertAfterTask(GENERATE_PROTO)
                        .insertBeforeTask(COMPILE_JAVA)
                        .applyNowTo(project);
        log.debug("Preparing to generate test validating builders.");
        Action<Task> testScopeAction =
                createAction(project,
                             getTestDescriptorSetPath(project),
                             getTargetTestGenValidatorsRootDir(project),
                             getTestProtoSrcDir(project));

        logDependingTask(GENERATE_TEST_VALIDATING_BUILDERS,
                         COMPILE_TEST_JAVA,
                         GENERATE_TEST_PROTO);

        GradleTask generateTestValidator =
                newTask(GENERATE_TEST_VALIDATING_BUILDERS, testScopeAction)
                        .insertAfterTask(GENERATE_TEST_PROTO)
                        .insertBeforeTask(COMPILE_TEST_JAVA)
                        .applyNowTo(project);
        log.debug("Validating builders generation phase initialized with tasks: {}, {}.",
                  generateValidator, generateTestValidator);
    }

    private Action<Task> createAction(Project project,
                                      String descriptorPath,
                                      String targetDirPath,
                                      String protoSrcDirPath) {
        return new GenAction(this, project, descriptorPath, targetDirPath, protoSrcDirPath);
    }

    private static class GenAction implements Action<Task> {

        private final ValidatingBuilderGenPlugin plugin;

        /**
         * Source Gradle project.
         */
        private final Project project;

        /**
         * Path to the generated Protobuf descriptor {@code .desc} file.
         */
        private final String descriptorPath;

        /**
         * An absolute path to the folder, serving as a target
         * for the generation for the given scope.
         */
        private final String targetDirPath;

        /**
         * An absolute path to the folder, containing the {@code .proto} files for the given scope.
         */
        private final String protoSrcDirPath;

        private GenAction(ValidatingBuilderGenPlugin plugin,
                          Project project,
                          String descriptorPath,
                          String targetDirPath,
                          String protoSrcDirPath) {
            this.plugin = plugin;
            this.project = project;
            this.descriptorPath = descriptorPath;
            this.targetDirPath = targetDirPath;
            this.protoSrcDirPath = protoSrcDirPath;
        }

        @Override
        public void execute(Task task) {
            if (!isGenerateValidatingBuilders(project)) {
                return;
            }
            File setFile = new File(descriptorPath);
            if (!setFile.exists()) {
                plugin.logMissingDescriptorSetFile(setFile);
                return;
            }

            Indent indent = getIndent(project);
            boolean allTypes = isGenerateValidatingBuildersFromClasspath(project);
            VBuilderGenerator generator =
                    new VBuilderGenerator(protoSrcDirPath, allTypes, targetDirPath, indent);
            generator.process(setFile);
        }
    }

    /** Opens the method to the helper class. */
    @SuppressWarnings("RedundantMethodOverride")
    @Override
    protected void logMissingDescriptorSetFile(File setFile) {
        super.logMissingDescriptorSetFile(setFile);
    }
}
