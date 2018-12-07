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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import io.spine.code.Indent;
import io.spine.code.java.PackageName;
import io.spine.code.java.SimpleClassName;
import io.spine.code.proto.FileName;
import io.spine.code.proto.RejectionDeclaration;
import io.spine.code.proto.RejectionsFile;
import io.spine.code.proto.SourceFile;
import io.spine.tools.compiler.TypeCache;
import io.spine.tools.compiler.rejection.RejectionWriter;
import io.spine.tools.gradle.GradleTask;
import io.spine.tools.gradle.SpinePlugin;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.slf4j.Logger;

import java.io.File;
import java.util.List;

import static io.spine.code.proto.FileDescriptors.parse;
import static io.spine.tools.gradle.TaskName.COMPILE_JAVA;
import static io.spine.tools.gradle.TaskName.COMPILE_TEST_JAVA;
import static io.spine.tools.gradle.TaskName.GENERATE_PROTO;
import static io.spine.tools.gradle.TaskName.GENERATE_REJECTIONS;
import static io.spine.tools.gradle.TaskName.GENERATE_TEST_PROTO;
import static io.spine.tools.gradle.TaskName.GENERATE_TEST_REJECTIONS;
import static io.spine.tools.gradle.compiler.Extension.getIndent;
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
 */
public class RejectionGenPlugin extends SpinePlugin {

    /** A map from Protobuf type name to Java class FQN. */
    private final TypeCache typeCache = new TypeCache();

    private List<RejectionsFile> collect(Iterable<FileDescriptorProto> files) {
        List<RejectionsFile> result = Lists.newLinkedList();
        Logger log = log();
        for (FileDescriptorProto file : files) {
            FileName fn = FileName.from(file);
            if (fn.isRejections()) {
                log.debug("Found rejections file: {}", fn.value());

                // See if the file content matches conventions.
                SourceFile sourceFile = SourceFile.from(file);
                if (sourceFile.isRejections()) {
                    RejectionsFile rejectionsFile = RejectionsFile.from(sourceFile);
                    result.add(rejectionsFile);
                } else {
                    log.error("Invalid rejections file: {}", file.getName());
                }
            }
        }
        log.debug("Found rejections in files: {}", result);

        return result;
    }

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

        Indent indent = getIndent(project);
        Action<Task> mainScopeAction = task -> {
            String mainFile = getMainDescriptorSetPath(project);
            String targetFolder = getTargetGenRejectionsRootDir(project);

            generateRejections(mainFile, targetFolder, indent);
        };

        logDependingTask(GENERATE_REJECTIONS, COMPILE_JAVA, GENERATE_PROTO);
        GradleTask mainTask =
                newTask(GENERATE_REJECTIONS, mainScopeAction)
                        .insertAfterTask(GENERATE_PROTO)
                        .insertBeforeTask(COMPILE_JAVA)
                        .applyNowTo(project);

        Action<Task> testScopeAction = task -> {
            String mainFile = getMainDescriptorSetPath(project);
            String testFile = getTestDescriptorSetPath(project);
            String targetFolder = getTargetTestGenRejectionsRootDir(project);

            generateTestRejections(mainFile, testFile, targetFolder, indent);
        };

        logDependingTask(GENERATE_TEST_REJECTIONS, COMPILE_TEST_JAVA, GENERATE_TEST_PROTO);

        GradleTask testTask =
                newTask(GENERATE_TEST_REJECTIONS, testScopeAction)
                        .insertAfterTask(GENERATE_TEST_PROTO)
                        .insertBeforeTask(COMPILE_TEST_JAVA)
                        .applyNowTo(project);

        log.debug("Rejection generation phase initialized with tasks: {}, {}", mainTask, testTask);
    }

    /**
     * Verifies if the descriptor set file exists. If not writes about this into the debug log.
     */
    private boolean fileExists(String descriptorSetFile) {
        File setFile = new File(descriptorSetFile);
        if (setFile.exists()) {
            return true;
        }
        logMissingDescriptorSetFile(setFile);
        return false;
    }

    private void generateRejections(String mainFile, String targetFolder, Indent indent) {
        if (!fileExists(mainFile)) {
            return;
        }

        log().debug("Generating rejections from {}", mainFile);
        List<FileDescriptorProto> mainFiles = parse(mainFile);
        collectAllMessageTypes(mainFiles);
        List<RejectionsFile> rejectionFiles = collect(mainFiles);
        doGenerate(rejectionFiles, targetFolder, indent);
    }

    private void generateTestRejections(String mainFile,
                                        String testFile,
                                        String targetFolder,
                                        Indent indent) {
        if (!(fileExists(mainFile) && fileExists(testFile))) {
            return;
        }

        log().debug("Generating test rejections from {}", testFile);

        List<FileDescriptorProto> mainFiles = parse(mainFile);
        collectAllMessageTypes(mainFiles);

        List<FileDescriptorProto> testFiles = parse(testFile);
        collectAllMessageTypes(testFiles);
        List<RejectionsFile> rejectionFiles = collect(testFiles);
        doGenerate(rejectionFiles, targetFolder, indent);
    }

    private void collectAllMessageTypes(Iterable<FileDescriptorProto> files) {
        for (FileDescriptorProto file : files) {
            typeCache.cacheTypes(file);
        }
    }

    private void doGenerate(Iterable<RejectionsFile> files, String outDir, Indent indent) {
        Logger log = log();
        log.debug("Processing the file descriptors for the rejections {}", files);
        for (RejectionsFile file : files) {
            // We are sure that this is a rejections file because we got them filtered.
            generateRejections(file, typeCache.getCachedTypes(), outDir, indent);
        }
    }

    private void generateRejections(RejectionsFile file,
                                    ImmutableMap<String, String> messageTypeMap,
                                    String rejectionsRootDir,
                                    Indent indent) {
        Logger log = log();
        if (log.isDebugEnabled()) {
            log.debug(
                "Generating rejections from file: `{}` javaPackage: `{}`, javaOuterClassName: `{}`",
                file.getPath(),
                PackageName.resolve(file.getDescriptor()),
                SimpleClassName.outerOf(file.getDescriptor())
            );
        }

        List<RejectionDeclaration> rejections = file.getRejectionDeclarations();
        File outDir = new File(rejectionsRootDir);
        for (RejectionDeclaration rejection : rejections) {
            // The name of the generated `ThrowableMessage` will be the same
            // as for the Protobuf message.
            log.debug("Processing rejection '{}'", rejection.getSimpleTypeName());
            RejectionWriter writer = new RejectionWriter(rejection, outDir, messageTypeMap, indent);
            writer.write();
        }
    }
}
