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

import com.google.common.collect.ImmutableSet;
import io.spine.code.generate.Indent;
import io.spine.code.java.PackageName;
import io.spine.code.java.SimpleClassName;
import io.spine.code.proto.FileSet;
import io.spine.code.proto.RejectionType;
import io.spine.code.proto.RejectionsFile;
import io.spine.logging.Logging;
import io.spine.tools.compiler.SourceProtoBelongsToModule;
import io.spine.tools.compiler.rejection.RejectionWriter;
import io.spine.tools.gradle.GradleTask;
import io.spine.tools.gradle.SpinePlugin;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.slf4j.Logger;

import java.io.File;
import java.util.List;
import java.util.function.Supplier;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static io.spine.code.proto.RejectionsFile.findAll;
import static io.spine.tools.gradle.TaskName.COMPILE_JAVA;
import static io.spine.tools.gradle.TaskName.COMPILE_TEST_JAVA;
import static io.spine.tools.gradle.TaskName.GENERATE_REJECTIONS;
import static io.spine.tools.gradle.TaskName.GENERATE_TEST_REJECTIONS;
import static io.spine.tools.gradle.TaskName.MERGE_DESCRIPTOR_SET;
import static io.spine.tools.gradle.TaskName.MERGE_TEST_DESCRIPTOR_SET;
import static io.spine.tools.gradle.compiler.Extension.getIndent;
import static io.spine.tools.gradle.compiler.Extension.getMainDescriptorSetPath;
import static io.spine.tools.gradle.compiler.Extension.getMainProtoSrcDir;
import static io.spine.tools.gradle.compiler.Extension.getTargetGenRejectionsRootDir;
import static io.spine.tools.gradle.compiler.Extension.getTargetTestGenRejectionsRootDir;
import static io.spine.tools.gradle.compiler.Extension.getTestDescriptorSetPath;
import static io.spine.tools.gradle.compiler.Extension.getTestProtoSrcDir;

/**
 * Plugin which generates Rejections declared in {@code rejections.proto} files.
 *
 * <p>Uses generated proto descriptors.
 *
 * <p>Logs a warning if there are no protobuf descriptors generated.
 */
public class RejectionGenPlugin extends SpinePlugin {

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
        Logger log = log();

        Action<Task> mainScopeAction =
                createAction(project,
                             () -> getMainDescriptorSetPath(project),
                             () -> getTargetGenRejectionsRootDir(project),
                             () -> getMainProtoSrcDir(project));

        GradleTask mainTask =
                newTask(GENERATE_REJECTIONS, mainScopeAction)
                        .insertAfterTask(MERGE_DESCRIPTOR_SET)
                        .insertBeforeTask(COMPILE_JAVA)
                        .applyNowTo(project);

        Action<Task> testScopeAction =
                createAction(project,
                             () -> getTestDescriptorSetPath(project),
                             () -> getTargetTestGenRejectionsRootDir(project),
                             () -> getTestProtoSrcDir(project));

        GradleTask testTask =
                newTask(GENERATE_TEST_REJECTIONS, testScopeAction)
                        .insertAfterTask(MERGE_TEST_DESCRIPTOR_SET)
                        .insertBeforeTask(COMPILE_TEST_JAVA)
                        .applyNowTo(project);

        log.debug("Rejection generation phase initialized with tasks: {}, {}", mainTask, testTask);
    }

    private Action<Task> createAction(Project project,
                                      Supplier<String> descriptorSetPath,
                                      Supplier<String> targetDirPath,
                                      Supplier<String> protoSrcDir) {

        return new GenAction(this, project, descriptorSetPath, targetDirPath, protoSrcDir);
    }

    private static class GenAction implements Action<Task>, Logging {

        /** The plugin which executes this task. */
        private final RejectionGenPlugin plugin;

        /**
         * Source Gradle project.
         */
        private final Project project;

        /**
         * Obtains the path to the generated Protobuf descriptor {@code .desc} file.
         */
        private final Supplier<String> descriptorPath;

        /**
         * Obtains an absolute path to the folder, serving as a target
         * for the generation for the given scope.
         */
        private final Supplier<String> targetDirPath;

        /**
         * Obtains an absolute path to the folder, containing the {@code .proto} files for
         * the given scope.
         */
        private final Supplier<String> protoSrcDirPath;

        /** Indentation for the generated code. */
        private @MonotonicNonNull Indent indent;

        private GenAction(RejectionGenPlugin plugin,
                          Project project,
                          Supplier<String> descriptorPath,
                          Supplier<String> targetDirPath,
                          Supplier<String> protoSrcDirPath) {
            this.plugin = plugin;
            this.project = project;
            this.descriptorPath = descriptorPath;
            this.targetDirPath = targetDirPath;
            this.protoSrcDirPath = protoSrcDirPath;
        }

        protected Indent indent() {
            if (indent == null) {
                indent = getIndent(project);
            }
            return indent;
        }

        protected File descriptorSetFile() {
            return resolve(descriptorPath);
        }

        protected File protoSrcDir() {
            return resolve(protoSrcDirPath);
        }

        protected File targetDir() {
            return resolve(targetDirPath);
        }

        @Override
        public void execute(Task task) {
            File descriptorSetFile = descriptorSetFile();
            if (!descriptorSetFile.exists()) {
                plugin.logMissingDescriptorSetFile(descriptorSetFile);
                return;
            }
            _debug("Generating from {}", descriptorSetFile);

            FileSet mainFiles = FileSet.parse(descriptorSetFile);
            ImmutableSet<RejectionsFile> rejectionFiles = findAll(mainFiles);
            Logger log = log();
            log.debug("Processing the file descriptors for the rejections {}", rejectionFiles);
            File targetDir = targetDir();
            for (RejectionsFile file : rejectionFiles) {
                // We are sure that this is a rejections file because we got them filtered.
                generateRejections(file, targetDir);
            }
        }

        private void generateRejections(RejectionsFile source, File rejectionsOutDir) {
            Logger log = log();
            if (log.isDebugEnabled()) {
                _debug(
                        "Generating rejections from file: `{}` javaPackage: `{}`, javaOuterClassName: `{}`",
                        source.getPath(),
                        PackageName.resolve(source.getDescriptor()
                                                  .toProto()),
                        SimpleClassName.outerOf(source.getDescriptor())
                );
            }

            List<RejectionType> rejections =
                    source.getRejectionDeclarations()
                          .stream()
                          .filter(new SourceProtoBelongsToModule(protoSrcDir()))
                          .collect(toImmutableList());

            for (RejectionType rejection : rejections) {
                // The name of the generated `ThrowableMessage` will be the same
                // as for the Protobuf message.
                log.debug("Processing rejection '{}'", rejection.simpleJavaClassName());
                RejectionWriter writer = new RejectionWriter(rejection, rejectionsOutDir, indent());
                writer.write();
            }
        }
    }
}
