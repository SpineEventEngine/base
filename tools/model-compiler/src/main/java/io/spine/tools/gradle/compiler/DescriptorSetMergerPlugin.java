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
import io.spine.code.proto.FileDescriptors;
import io.spine.tools.compiler.type.DescriptorSetFiles;
import io.spine.tools.gradle.SpinePlugin;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;

import static io.spine.code.proto.FileDescriptors.KNOWN_TYPES;
import static io.spine.tools.gradle.TaskName.GENERATE_PROTO;
import static io.spine.tools.gradle.TaskName.GENERATE_TEST_PROTO;
import static io.spine.tools.gradle.TaskName.GENERATE_TEST_VALIDATING_BUILDERS;
import static io.spine.tools.gradle.TaskName.GENERATE_VALIDATING_BUILDERS;
import static io.spine.tools.gradle.TaskName.MERGE_DESCRIPTOR_SET;
import static io.spine.tools.gradle.TaskName.MERGE_TEST_DESCRIPTOR_SET;
import static io.spine.tools.gradle.compiler.Extension.getFatArchive;
import static io.spine.tools.gradle.compiler.Extension.getMainDescriptorSetPath;
import static io.spine.tools.gradle.compiler.Extension.getTestDescriptorSetPath;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isSameFile;
import static java.util.stream.Collectors.toList;

/**
 * A Gradle plugin which merges the descriptor file with all the descriptor files from
 * the project runtime classpath.
 *
 * @author Dmytro Dashenkov
 */
public class DescriptorSetMergerPlugin extends SpinePlugin {

    @Override
    public void apply(Project project) {
        createMainTask(project);
        createTestTask(project);

        stripOffKnownTypes(project);
    }

    private void stripOffKnownTypes(Project project) {
        project.afterEvaluate(evaluatedProject -> {
            Optional<AbstractArchiveTask> task = getFatArchive(evaluatedProject);
            task.ifPresent(archiveTask -> stripOffKnownTypes(evaluatedProject, archiveTask));
        });
    }

    private void stripOffKnownTypes(Project project, AbstractArchiveTask archiveTask) {
        Path ownMainFile = Paths.get(getTestDescriptorSetPath(project));
        Path ownTestFile = Paths.get(getMainDescriptorSetPath(project));
        boolean mainFileExists = exists(ownMainFile);
        boolean testFileExists = exists(ownTestFile);

        archiveTask.exclude(file -> {
            boolean knownTypes = FileDescriptors.KNOWN_TYPES.equals(file.getName());
            if (!knownTypes) {
                return false;
            }
            Path path = file.getFile()
                            .toPath();
            boolean domesticFile;
            try {
                domesticFile = mainFileExists && isSameFile(path, ownMainFile);
                domesticFile = domesticFile || (testFileExists && isSameFile(path, ownTestFile));
            } catch (IOException e) {
                throw illegalStateWithCauseOf(e);
            }
            if (!domesticFile) {
                log().debug("Excluding descriptor file {} from the module artifacts.", path);
            }
            return !domesticFile;
        });
    }

    private void createMainTask(Project project) {
        logDependingTask(MERGE_DESCRIPTOR_SET,
                         GENERATE_VALIDATING_BUILDERS,
                         GENERATE_PROTO);
        newTask(MERGE_DESCRIPTOR_SET,
                createMergingAction(configuration(project, "runtime"),
                                    getMainDescriptorSetPath(project)))
                .insertAfterTask(GENERATE_PROTO)
                .insertBeforeTask(GENERATE_VALIDATING_BUILDERS)
                .applyNowTo(project);
    }

    private void createTestTask(Project project) {
        logDependingTask(MERGE_TEST_DESCRIPTOR_SET,
                         GENERATE_TEST_VALIDATING_BUILDERS,
                         GENERATE_TEST_PROTO);
        newTask(MERGE_TEST_DESCRIPTOR_SET,
                createMergingAction(configuration(project, "testRuntime"),
                                    getTestDescriptorSetPath(project)))
                .insertAfterTask(GENERATE_TEST_PROTO)
                .insertBeforeTask(GENERATE_TEST_VALIDATING_BUILDERS)
                .applyNowTo(project);
    }

    private Action<Task> createMergingAction(Configuration configuration,
                                             String descriptorSetPath) {
        return task -> {
            File descriptorSet = new File(descriptorSetPath);
            Collection<File> descriptors =
                    configuration.getFiles()
                                 .stream()
                                 .map(task.getProject()::zipTree)
                                 .flatMap(fileTree -> fileTree.getFiles().stream())
                                 .filter(file -> KNOWN_TYPES.equals(file.getName()))
                                 .peek(file -> log().debug("Merging descriptors from {}", file))
                                 .collect(toList());
            ImmutableSet.Builder<File> files = ImmutableSet
                    .<File>builder()
                    .addAll(descriptors);
            if (descriptorSet.exists()) {
                files.add(descriptorSet);
            }
            DescriptorSetFiles.merge(files.build())
                              .writeTo(descriptorSet);
        };
    }

    private static Configuration configuration(Project project, String name) {
        return project.getConfigurations()
                      .getByName(name);
    }
}
