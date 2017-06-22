/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

package io.spine.gradle.compiler.validate;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import io.spine.gradle.SpinePlugin;
import io.spine.gradle.compiler.Indent;
import io.spine.gradle.compiler.message.MessageTypeCache;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.gradle.TaskName.COMPILE_JAVA;
import static io.spine.gradle.TaskName.COMPILE_TEST_JAVA;
import static io.spine.gradle.TaskName.GENERATE_PROTO;
import static io.spine.gradle.TaskName.GENERATE_TEST_PROTO;
import static io.spine.gradle.TaskName.GENERATE_TEST_VALIDATING_BUILDERS;
import static io.spine.gradle.TaskName.GENERATE_VALIDATING_BUILDERS;
import static io.spine.gradle.compiler.Extension.getIndent;
import static io.spine.gradle.compiler.Extension.getMainDescriptorSetPath;
import static io.spine.gradle.compiler.Extension.getMainProtoSrcDir;
import static io.spine.gradle.compiler.Extension.getTargetGenValidatorsRootDir;
import static io.spine.gradle.compiler.Extension.getTargetTestGenValidatorsRootDir;
import static io.spine.gradle.compiler.Extension.getTestDescriptorSetPath;
import static io.spine.gradle.compiler.Extension.getTestProtoSrcDir;
import static io.spine.gradle.compiler.Extension.isGenerateValidatingBuilders;
import static io.spine.gradle.compiler.Extension.isGenerateValidatingBuildersFromClasspath;

/**
 * Plugin which generates validating builders based on the Protobuf Message definitions.
 *
 * <p>Uses generated proto descriptors.
 *
 * <p>Logs a warning if there are no Protobuf descriptors generated.
 *
 * <p>To switch off the generation of the validating builders
 * use the {@code generateValidatingBuilders} property as follows:
 *
 * {@code
 * <pre>
 * modelCompiler {
 *     generateValidatingBuilders = false
 * }
 * </pre>
 * }
 *
 * <p>The default value is {@code true}.
 *
 * <p>The tabular indentation for the generated code is done with whitespaces.
 * To set the width please use the {@code indent} property as follows:
 *
 * {@code
 * <pre>
 * modelCompiler {
 *     indent = 2
 * }
 * </pre>
 * }
 *
 * <p>The default value is 4.
 *
 * @author Illia Shepilov
 * @see io.spine.validate.ValidatingBuilder
 * @see io.spine.validate.AbstractValidatingBuilder
 */
public class ValidatingBuilderGenPlugin extends SpinePlugin {

    @Override
    public void apply(Project project) {
        log().debug("Preparing to generate validating builders.");
        final Action<Task> mainScopeAction =
                createAction(project,
                             getMainDescriptorSetPath(project),
                             getTargetGenValidatorsRootDir(project),
                             getMainProtoSrcDir(project));

        logDependingTask(log(), GENERATE_VALIDATING_BUILDERS, COMPILE_JAVA, GENERATE_PROTO);
        final GradleTask generateValidator =
                newTask(GENERATE_VALIDATING_BUILDERS,
                        mainScopeAction).insertAfterTask(GENERATE_PROTO)
                                        .insertBeforeTask(COMPILE_JAVA)
                                        .applyNowTo(project);
        log().debug("Preparing to generate test validating builders.");
        final Action<Task> testScopeAction =
                createAction(project,
                             getTestDescriptorSetPath(project),
                             getTargetTestGenValidatorsRootDir(project),
                             getTestProtoSrcDir(project));

        logDependingTask(log(), GENERATE_TEST_VALIDATING_BUILDERS,
                         COMPILE_TEST_JAVA, GENERATE_TEST_PROTO);
        final GradleTask generateTestValidator =
                newTask(GENERATE_TEST_VALIDATING_BUILDERS,
                        testScopeAction).insertAfterTask(GENERATE_TEST_PROTO)
                                        .insertBeforeTask(COMPILE_TEST_JAVA)
                                        .applyNowTo(project);
        log().debug("Validating builders generation phase initialized with tasks: {}, {}.",
                    generateValidator, generateTestValidator);
    }

    private static Action<Task> createAction(final Project project,
                                             final String descriptorPath,
                                             final String targetDirPath,
                                             final String protoSrcDirPath) {
        return new GenerationAction(project, descriptorPath, targetDirPath, protoSrcDirPath);
    }

    private enum LogSingleton {
        INSTANCE;

        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(ValidatingBuilderGenPlugin.class);
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }

    /**
     * Gradle {@code Action} for validating builder generation.
     *
     * <p>An instance-per-scope is usually created. E.g. test sources and main source are
     * generated with different instances of this class.
     */
    private static class GenerationAction implements Action<Task> {
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

        private GenerationAction(Project project,
                                 String descriptorPath,
                                 String targetDirPath,
                                 String protoSrcDirPath) {
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
            log().debug("Generating the validating builders from {}.", descriptorPath);

            final Indent indent = getIndent(project);
            final boolean classpathGenEnabled =
                    isGenerateValidatingBuildersFromClasspath(project);

            final MetadataAssembler assembler = new MetadataAssembler(descriptorPath);
            final Set<VBMetadata> metadataItems = assembler.assemble();

            final MessageTypeCache messageTypeCache = assembler.getAssembledMessageTypeCache();
            final ValidatingBuilderWriter writer =
                    new ValidatingBuilderWriter(targetDirPath, indent, messageTypeCache);

            final Iterable<VBMetadata> metadataToWrite = filter(classpathGenEnabled, metadataItems);

            for (VBMetadata metadata : metadataToWrite) {
                try {
                    writer.write(metadata);
                } catch (RuntimeException e) {
                    final String message =
                            "Cannot generate the validating builder for " + metadata + ". ";
                    log().warn(message);
                    log().trace(message, e);
                }
            }
            log().debug("The validating builder generation is finished.");
        }

        private Iterable<VBMetadata> filter(boolean classpathGenEnabled,
                                            Set<VBMetadata> metadataItems) {
            final Predicate<VBMetadata> shouldWritePredicate = getPredicate(classpathGenEnabled);
            final Iterable<VBMetadata> result =
                    Iterables.filter(metadataItems, shouldWritePredicate);
            return result;
        }

        private Predicate<VBMetadata> getPredicate(final boolean classpathGenEnabled) {
            final Predicate<VBMetadata> result;
            if (classpathGenEnabled) {
                result = Predicates.alwaysTrue();
            } else {
                final String rootPath = protoSrcDirPath.endsWith(File.separator)
                                        ? protoSrcDirPath
                                        : protoSrcDirPath + File.separator;
                result = new SourceProtoBelongsToModule(rootPath);
            }
            return result;
        }

        /**
         * A predicate determining if the given {@linkplain VBMetadata validating builder metadata}
         * has been collected from the source file in the specified module.
         *
         * <p>Each predicate instance requires to specify the root folder of Protobuf definitions
         * for the module. This value is used to match the given {@code VBMetadata}.
         */
        private static class SourceProtoBelongsToModule implements Predicate<VBMetadata> {

            /**
             *  An absolute path to the root folder for the {@code .proto} files in the module.
             */
            private final String rootPath;

            private SourceProtoBelongsToModule(String rootPath) {
                this.rootPath = rootPath;
            }

            @Override
            public boolean apply(@Nullable VBMetadata input) {
                checkNotNull(input);

                final Optional<String> optionalPath = input.getSourceProtoFilePath();

                /*
                 * In case it's not possible to determine the origin for this metadata,
                 * let's think it is originated from the proper module.
                 *
                 * <p>Such a gentle approach is required in order not to lose any builder metadata
                 * items. It's better to generate something and let the end-user decide,
                 * than silently filter out the suspicious data.
                 */
                if (!optionalPath.isPresent()) {
                    return true;
                }
                final String relativeProtoPath = optionalPath.get();
                final File protoFile = new File(rootPath + relativeProtoPath);
                final boolean belongsToModule = protoFile.exists();
                return belongsToModule;
            }
        }
    }
}
