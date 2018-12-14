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

import com.google.common.collect.ImmutableSet;
import io.spine.code.Indent;
import io.spine.code.java.PackageName;
import io.spine.code.java.SimpleClassName;
import io.spine.code.proto.FileSet;
import io.spine.code.proto.RejectionType;
import io.spine.code.proto.RejectionsFile;
import io.spine.tools.compiler.rejection.RejectionWriter;
import io.spine.tools.gradle.GradleTask;
import io.spine.tools.gradle.SpinePlugin;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.slf4j.Logger;

import java.io.File;
import java.util.List;

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

        FileSet mainFiles = FileSet.parse(mainFile);
        ImmutableSet<RejectionsFile> rejectionFiles = RejectionsFile.findAll(mainFiles);
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

        FileSet testFiles = FileSet.parse(testFile);

        ImmutableSet<RejectionsFile> rejectionFiles = RejectionsFile.findAll(testFiles);
        doGenerate(rejectionFiles, targetFolder, indent);
    }

    private void doGenerate(Iterable<RejectionsFile> files, String outDir, Indent indent) {
        Logger log = log();
        log.debug("Processing the file descriptors for the rejections {}", files);
        for (RejectionsFile file : files) {
            // We are sure that this is a rejections file because we got them filtered.
            generateRejections(file, outDir, indent);
        }
    }

    private void generateRejections(RejectionsFile file,
                                    String rejectionsRootDir,
                                    Indent indent) {
        Logger log = log();
        if (log.isDebugEnabled()) {
            log.debug(
                "Generating rejections from file: `{}` javaPackage: `{}`, javaOuterClassName: `{}`",
                file.getPath(),
                PackageName.resolve(file.getDescriptor()
                                        .toProto()),
                SimpleClassName.outerOf(file.getDescriptor())
            );
        }

        List<RejectionType> rejections = file.getRejectionDeclarations();
        File outDir = new File(rejectionsRootDir);
        for (RejectionType rejection : rejections) {
            // The name of the generated `ThrowableMessage` will be the same
            // as for the Protobuf message.
            log.debug("Processing rejection '{}'", rejection.simpleJavaClassName());
            RejectionWriter writer = new RejectionWriter(rejection, outDir, indent);
            writer.write();
        }
    }
}
