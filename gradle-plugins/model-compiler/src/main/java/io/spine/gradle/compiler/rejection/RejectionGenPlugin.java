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
package io.spine.gradle.compiler.rejection;

import com.google.common.collect.Lists;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import io.spine.gradle.SpinePlugin;
import io.spine.gradle.compiler.message.MessageTypeCache;
import io.spine.tools.java.PackageName;
import io.spine.tools.java.SimpleClassName;
import io.spine.tools.proto.FileDescriptors;
import io.spine.type.RejectionMessage;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static io.spine.gradle.TaskName.COMPILE_JAVA;
import static io.spine.gradle.TaskName.COMPILE_TEST_JAVA;
import static io.spine.gradle.TaskName.GENERATE_PROTO;
import static io.spine.gradle.TaskName.GENERATE_REJECTIONS;
import static io.spine.gradle.TaskName.GENERATE_TEST_PROTO;
import static io.spine.gradle.TaskName.GENERATE_TEST_REJECTIONS;
import static io.spine.gradle.compiler.Extension.getMainDescriptorSetPath;
import static io.spine.gradle.compiler.Extension.getTargetGenRejectionsRootDir;
import static io.spine.gradle.compiler.Extension.getTargetTestGenRejectionsRootDir;
import static io.spine.gradle.compiler.Extension.getTestDescriptorSetPath;

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
        log().trace("Preparing to generate rejections");
        final Action<Task> mainScopeAction = new Action<Task>() {
            @Override
            public void execute(Task task) {
                final String path = getMainDescriptorSetPath(project);
                log().debug("Generating rejections from {}", path);
                final List<FileDescriptorProto> filesWithRejections =
                        collectRejectionFiles(path);
                processDescriptors(filesWithRejections, getTargetGenRejectionsRootDir(project));
            }
        };

        logDependingTask(log(), GENERATE_REJECTIONS, COMPILE_JAVA, GENERATE_PROTO);
        final GradleTask generateRejections =
                newTask(GENERATE_REJECTIONS, mainScopeAction).insertAfterTask(GENERATE_PROTO)
                                                           .insertBeforeTask(COMPILE_JAVA)
                                                           .applyNowTo(project);
        log().trace("Preparing to generate test rejections");
        final Action<Task> testScopeAction = new Action<Task>() {
            @Override
            public void execute(Task task) {
                final String path = getTestDescriptorSetPath(project);
                log().debug("Generating test rejections from {}", path);
                final List<FileDescriptorProto> rejectionFiles = collectRejectionFiles(path);
                processDescriptors(rejectionFiles, getTargetTestGenRejectionsRootDir(project));
            }
        };

        logDependingTask(log(), GENERATE_TEST_REJECTIONS, COMPILE_TEST_JAVA, GENERATE_TEST_PROTO);
        final GradleTask generateTestRejections =
                newTask(GENERATE_TEST_REJECTIONS,
                        testScopeAction).insertAfterTask(GENERATE_TEST_PROTO)
                                        .insertBeforeTask(COMPILE_TEST_JAVA)
                                        .applyNowTo(project);
        log().debug("Rejection generation phase initialized with tasks: {}, {}",
                    generateRejections,
                    generateTestRejections);
    }

    private List<FileDescriptorProto> collectRejectionFiles(String descFilePath) {
        final List<FileDescriptorProto> result = Lists.newLinkedList();
        final Collection<FileDescriptorProto> allDescriptors =
                FileDescriptors.parse(descFilePath);
        for (FileDescriptorProto file : allDescriptors) {
            if (file.getName()
                    .endsWith(RejectionMessage.PROTO_FILE_SUFFIX)) {
                log().trace("Found rejections file: {}", file.getName());
                result.add(file);
            }
            messageTypeCache.cacheTypes(file);
        }
        log().trace("Found rejections in files: {}", result);

        return result;
    }

    private void processDescriptors(List<FileDescriptorProto> descriptors,
                                    String rejectionsRootDir) {
        log().debug("Processing the file descriptors for the rejections {}", descriptors);
        for (FileDescriptorProto file : descriptors) {
            if (isRejectionsFile(file)) {
                generateRejections(file, messageTypeCache.getCachedTypes(), rejectionsRootDir);
            } else {
                log().error("Invalid rejections file: {}", file.getName());
            }
        }
    }

    private static boolean isRejectionsFile(FileDescriptorProto descriptor) {
        // By convention rejections are generated into one file.
        if (descriptor.getOptions()
                      .getJavaMultipleFiles()) {
            return false;
        }
        final String javaOuterClassName = descriptor.getOptions()
                                                    .getJavaOuterClassname();
        if (javaOuterClassName.isEmpty()) {
            // There's no outer class name given in options.
            // Assuming the file name ends with `rejections.proto`, it's a good rejections file.
            return true;
        }

        final boolean result =
                javaOuterClassName.endsWith(RejectionMessage.OUTER_CLASS_NAME_SUFFIX);
        return result;
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
