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
package io.spine.gradle.compiler.failure;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import io.spine.gradle.SpinePlugin;
import io.spine.gradle.compiler.message.MessageTypeCache;
import io.spine.gradle.compiler.util.JavaCode;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static io.spine.gradle.TaskName.COMPILE_JAVA;
import static io.spine.gradle.TaskName.COMPILE_TEST_JAVA;
import static io.spine.gradle.TaskName.GENERATE_FAILURES;
import static io.spine.gradle.TaskName.GENERATE_PROTO;
import static io.spine.gradle.TaskName.GENERATE_TEST_FAILURES;
import static io.spine.gradle.TaskName.GENERATE_TEST_PROTO;
import static io.spine.gradle.compiler.Extension.getMainDescriptorSetPath;
import static io.spine.gradle.compiler.Extension.getTargetGenFailuresRootDir;
import static io.spine.gradle.compiler.Extension.getTargetTestGenFailuresRootDir;
import static io.spine.gradle.compiler.Extension.getTestDescriptorSetPath;
import static io.spine.gradle.compiler.util.DescriptorSetUtil.getProtoFileDescriptors;

/**
 * Plugin which generates Failures, based on failures.proto files.
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
public class FailuresGenPlugin extends SpinePlugin {

    /** A map from Protobuf type name to Java class FQN. */
    private final MessageTypeCache messageTypeCache = new MessageTypeCache();

    /**
     * Applies the plug-in to a project.
     *
     * <p>Adds {@code :generateFailures} and {@code :generateTestFailures} tasks.
     *
     * <p>Tasks depend on corresponding {@code :generateProto} tasks and are executed
     * before corresponding {@code :compileJava} tasks.
     */
    @Override
    public void apply(final Project project) {
        log().debug("Preparing to generate failures");
        final Action<Task> mainScopeAction = new Action<Task>() {
            @Override
            public void execute(Task task) {
                final String path = getMainDescriptorSetPath(project);
                log().debug("Generating the failures from {}", path);
                final List<FileDescriptorProto> filesWithFailures =
                        getFailureProtoFileDescriptors(path);
                processDescriptors(filesWithFailures, getTargetGenFailuresRootDir(project));
            }
        };

        logDependingTask(log(), GENERATE_FAILURES, COMPILE_JAVA, GENERATE_PROTO);
        final GradleTask generateFailures =
                newTask(GENERATE_FAILURES, mainScopeAction).insertAfterTask(GENERATE_PROTO)
                                                           .insertBeforeTask(COMPILE_JAVA)
                                                           .applyNowTo(project);
        log().debug("Preparing to generate test failures");
        final Action<Task> testScopeAction = new Action<Task>() {
            @Override
            public void execute(Task task) {
                final String path = getTestDescriptorSetPath(project);
                log().debug("Generating the test failures from {}", path);
                final List<FileDescriptorProto> filesWithFailures =
                        getFailureProtoFileDescriptors(path);
                processDescriptors(filesWithFailures, getTargetTestGenFailuresRootDir(project));
            }
        };

        logDependingTask(log(), GENERATE_TEST_FAILURES, COMPILE_TEST_JAVA, GENERATE_TEST_PROTO);
        final GradleTask generateTestFailures =
                newTask(GENERATE_TEST_FAILURES,
                        testScopeAction).insertAfterTask(GENERATE_TEST_PROTO)
                                        .insertBeforeTask(COMPILE_TEST_JAVA)
                                        .applyNowTo(project);
        log().debug("Failure generation phase initialized with tasks: {}, {}",
                    generateFailures,
                    generateTestFailures);
    }

    private List<FileDescriptorProto> getFailureProtoFileDescriptors(String descFilePath) {
        final List<FileDescriptorProto> result = new LinkedList<>();
        final Collection<FileDescriptorProto> allDescriptors =
                getProtoFileDescriptors(descFilePath);
        for (FileDescriptorProto file : allDescriptors) {
            if (file.getName()
                    .endsWith("failures.proto")) {
                log().trace("Found failures file: {}", file.getName());
                result.add(file);
            }
            messageTypeCache.cacheTypes(file);
        }
        log().trace("Found failures in files: {}", result);

        return result;
    }

    private void processDescriptors(List<FileDescriptorProto> descriptors, String failuresRootDir) {
        log().debug("Processing the file descriptors for the failures {}", descriptors);
        for (FileDescriptorProto file : descriptors) {
            if (isFileWithFailures(file)) {
                generateFailures(file, messageTypeCache.getCachedTypes(), failuresRootDir);
            } else {
                log().error("Invalid failures file: {}", file.getName());
            }
        }
    }

    private static boolean isFileWithFailures(FileDescriptorProto descriptor) {
        // By convention failures are generated into one file.
        if (descriptor.getOptions()
                      .getJavaMultipleFiles()) {
            return false;
        }
        final String javaOuterClassName = descriptor.getOptions()
                                                    .getJavaOuterClassname();
        if (javaOuterClassName.isEmpty()) {
            // There's no outer class name given in options.
            // Assuming the file name ends with `failures.proto`, it's a good failures file.
            return true;
        }

        // it's OK, since a duplicated piece is totally unrelated and
        // is located in the test codebase.
        @SuppressWarnings("DuplicateStringLiteralInspection")
        final boolean result = javaOuterClassName.endsWith("Failures");
        return result;
    }

    private static void generateFailures(FileDescriptorProto descriptor,
                                         Map<String, String> messageTypeMap,
                                         String failuresRootDir) {
        log().debug("Generating failures from file {}", descriptor.getName());
        final String javaPackage = descriptor.getOptions()
                                             .getJavaPackage();
        final String javaOuterClassName = JavaCode.getOuterClassName(descriptor);
        log().trace("Found options: javaPackage: {}, javaOuterClassName: {}",
                    javaPackage,
                    javaOuterClassName);
        final List<DescriptorProto> failures = descriptor.getMessageTypeList();
        for (DescriptorProto failure : failures) {
            // The name of the generated ThrowableFailure will be the same
            // as for the Protobuf message.
            log().trace("Processing failure '{}'", failure.getName());

            final FailureMetadata metadata = new FailureMetadata(failure, descriptor);
            final File outputDir = new File(failuresRootDir);
            final FailureWriter writer = new FailureWriter(metadata, outputDir, messageTypeMap);
            writer.write();
        }
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(FailuresGenPlugin.class);
    }
}
