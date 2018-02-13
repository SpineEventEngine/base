/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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
package io.spine.gradle.compiler.rejection;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import io.spine.gradle.compiler.message.MessageTypeCache;
import io.spine.tools.gradle.GradleTask;
import io.spine.tools.gradle.SpinePlugin;
import io.spine.tools.java.PackageName;
import io.spine.tools.java.SimpleClassName;
import io.spine.tools.proto.FileDescriptors;
import io.spine.tools.proto.Rejections;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Map;

import static io.spine.tools.gradle.TaskName.COMPILE_JAVA;
import static io.spine.tools.gradle.TaskName.COMPILE_TEST_JAVA;
import static io.spine.tools.gradle.TaskName.GENERATE_PROTO;
import static io.spine.tools.gradle.TaskName.GENERATE_REJECTIONS;
import static io.spine.tools.gradle.TaskName.GENERATE_TEST_PROTO;
import static io.spine.tools.gradle.TaskName.GENERATE_TEST_REJECTIONS;
import static io.spine.tools.gradle.compiler.Extension.getMainDescriptorSetPath;
import static io.spine.tools.gradle.compiler.Extension.getTargetGenRejectionsRootDir;
import static io.spine.tools.gradle.compiler.Extension.getTargetTestGenRejectionsRootDir;
import static io.spine.tools.gradle.compiler.Extension.getTestDescriptorSetPath;

/**
 * Plugin which generates Rejections declared in {@code rejections.proto} files.
 *
 * <p>Uses generated proto descriptors.
 *
 * <p>Logs a warning if there are no protobuf descriptors generated.
 *
 * @author Mikhail Mikhaylov
 * @author Alexander Yevsyukov
 * @author Alexander Litus
 * @author Alex Tymchenko
 */
public class RejectionGenPlugin extends SpinePlugin {

    /** A map from Protobuf type name to Java class FQN. */
    private final MessageTypeCache messageTypeCache = new MessageTypeCache();

    /**
     * Applies the plug-in to a project.
     *
     * <p>Adds {@code :generateRejections} and {@code :generateTestRejections} tasks.
     *
     * <p>Tasks depend on corresponding {@code :generateProto} tasks and are executed
     * before corresponding {@code :compileJava} tasks.
     */
    @Override
    public void apply(final Project project) {
        final Logger log = log();

        final Action<Task> mainScopeAction = new Action<Task>() {
            @Override
            public void execute(Task task) {
                final String mainFile = getMainDescriptorSetPath(project);
                final String targetFolder = getTargetGenRejectionsRootDir(project);

                generateRejections(mainFile, targetFolder);
            }
        };

        logDependingTask(log, GENERATE_REJECTIONS, COMPILE_JAVA, GENERATE_PROTO);
        final GradleTask mainTask =
                newTask(GENERATE_REJECTIONS, mainScopeAction)
                        .insertAfterTask(GENERATE_PROTO)
                        .insertBeforeTask(COMPILE_JAVA)
                        .applyNowTo(project);

        final Action<Task> testScopeAction = new Action<Task>() {
            @Override
            public void execute(Task task) {
                final String mainFile = getMainDescriptorSetPath(project);
                final String testFile = getTestDescriptorSetPath(project);
                final String targetFolder = getTargetTestGenRejectionsRootDir(project);

                generateTestRejections(mainFile, targetFolder, testFile);
            }
        };

        logDependingTask(log, GENERATE_TEST_REJECTIONS, COMPILE_TEST_JAVA, GENERATE_TEST_PROTO);

        final GradleTask testTask =
                newTask(GENERATE_TEST_REJECTIONS, testScopeAction)
                        .insertAfterTask(GENERATE_TEST_PROTO)
                        .insertBeforeTask(COMPILE_TEST_JAVA)
                        .applyNowTo(project);

        log.debug("Rejection generation phase initialized with tasks: {}, {}", mainTask, testTask);
    }

    private void generateRejections(String mainFile, String targetFolder) {
        log().debug("Generating rejections from {}", mainFile);
        final List<FileDescriptorProto> mainFiles = FileDescriptors.parse(mainFile);
        collectAllMessageTypes(mainFiles);
        final List<FileDescriptorProto> rejectionFiles = Rejections.collect(mainFiles);
        doGenerate(rejectionFiles, targetFolder);
    }

    private void generateTestRejections(String mainFile, String targetFolder, String testFile) {
        log().debug("Generating test rejections from {}", testFile);
        final List<FileDescriptorProto> mainFiles = FileDescriptors.parse(mainFile);
        collectAllMessageTypes(mainFiles);

        final List<FileDescriptorProto> testFiles = FileDescriptors.parse(testFile);
        collectAllMessageTypes(testFiles);
        final List<FileDescriptorProto> rejectionFiles = Rejections.collect(testFiles);
        doGenerate(rejectionFiles, targetFolder);
    }

    private void collectAllMessageTypes(Iterable<FileDescriptorProto> files) {
        for (FileDescriptorProto file : files) {
            messageTypeCache.cacheTypes(file);
        }
    }

    private void doGenerate(Iterable<FileDescriptorProto> files, String rejectionsRootDir) {
        final Logger log = log();
        log.debug("Processing the file descriptors for the rejections {}", files);
        for (FileDescriptorProto file : files) {
            // We are sure that this is a rejections file because we got them filtered.
            generateRejections(file, messageTypeCache.getCachedTypes(), rejectionsRootDir);
        }
    }

    private static void generateRejections(FileDescriptorProto file,
                                           Map<String, String> messageTypeMap,
                                           String rejectionsRootDir) {
        final Logger log = log();
        log.debug("Generating rejections from file {}", file.getName());

        if (log.isTraceEnabled()) {
            log.trace("javaPackage: {}, javaOuterClassName: {}",
                      PackageName.resolve(file),
                      SimpleClassName.outerOf(file));
        }

        final List<DescriptorProto> rejections = file.getMessageTypeList();
        for (DescriptorProto rejection : rejections) {
            // The name of the generated `ThrowableMessage` will be the same
            // as for the Protobuf message.
            log.trace("Processing rejection '{}'", rejection.getName());

            final RejectionMetadata metadata = new RejectionMetadata(rejection, file);
            final File outputDir = new File(rejectionsRootDir);
            final RejectionWriter writer = new RejectionWriter(metadata, outputDir, messageTypeMap);
            writer.write();
        }
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(RejectionGenPlugin.class);
    }
}
