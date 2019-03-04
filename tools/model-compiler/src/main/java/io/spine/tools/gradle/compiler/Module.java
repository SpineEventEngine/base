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

import io.spine.tools.gradle.SourceSetName;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.file.collections.ImmutableFileCollection;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.gradle.SourceSetName.MAIN;
import static io.spine.tools.gradle.SourceSetName.TEST;
import static io.spine.tools.gradle.compiler.Extension.getMainGenProtoDir;
import static io.spine.tools.gradle.compiler.Extension.getTargetGenRejectionsRootDir;
import static io.spine.tools.gradle.compiler.Extension.getTargetGenValidatorsRootDir;
import static io.spine.tools.gradle.compiler.Extension.getTargetTestGenRejectionsRootDir;
import static io.spine.tools.gradle.compiler.Extension.getTargetTestGenValidatorsRootDir;
import static io.spine.tools.gradle.compiler.Extension.getTestProtoSrcDir;

final class Module {

    private static final String PROTO_SOURCE_SET = "proto";

    private final Project project;

    Module(Project project) {
        this.project = checkNotNull(project);
    }

    FileCollection protoSource() {
        return protoSource(MAIN);
    }

    FileCollection testProtoSource() {
        return protoSource(TEST);
    }

    private FileCollection protoSource(SourceSetName sourceSetName) {
        SourceSet sourceSet = sourceSet(sourceSetName);
        Optional<FileCollection> files = protoSource(sourceSet);
        return files.orElse(ImmutableFileCollection.of());
    }

    private static Optional<FileCollection> protoSource(SourceSet sourceSet) {
        Object rawExtension = sourceSet.getExtensions()
                                       .findByName(PROTO_SOURCE_SET);
        if (rawExtension == null) {
            return Optional.empty();
        } else {
            FileCollection protoSet = (FileCollection) rawExtension;
            return Optional.of(protoSet);
        }
    }

    private SourceSet sourceSet(SourceSetName sourceSetName) {
        JavaPluginConvention javaConvention = project.getConvention()
                                                     .getPlugin(JavaPluginConvention.class);
        SourceSet sourceSet = javaConvention.getSourceSets()
                                            .findByName(sourceSetName.value());
        checkNotNull(sourceSet);
        return sourceSet;
    }

    FileCollection protoCompiledToJava() {
        String generationDir = getMainGenProtoDir(project);
        FileCollection files = project.fileTree(generationDir);
        return files;
    }

    FileCollection testProtoCompiledToJava() {
        String generationDir = getTestProtoSrcDir(project);
        FileCollection files = project.fileTree(generationDir);
        return files;
    }

    FileCollection validatingBuilders() {
        String vBuilderGenTarget = getTargetGenValidatorsRootDir(project);
        FileCollection files = project.fileTree(vBuilderGenTarget);
        return files;
    }

    FileCollection testValidatingBuilders() {
        String vBuilderGenTarget = getTargetTestGenValidatorsRootDir(project);
        FileCollection files = project.fileTree(vBuilderGenTarget);
        return files;
    }

    FileCollection compiledRejections() {
        String targetDir = getTargetGenRejectionsRootDir(project);
        FileCollection files = project.fileTree(targetDir);
        return files;
    }

    FileCollection testCompiledRejections() {
        String targetDir = getTargetTestGenRejectionsRootDir(project);
        FileCollection files = project.fileTree(targetDir);
        return files;
    }

    Project gradleProject() {
        return project;
    }
}
