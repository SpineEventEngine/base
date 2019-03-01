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
import io.spine.tools.gradle.SpinePlugin;
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
import static io.spine.tools.gradle.compiler.Extension.getTestProtoSrcDir;

/**
 * A {@link SpinePlugin} which performs its tasks in the incremental manner.
 *
 * <p>When running a task configured for incremental build, it may be skipped if its outcome is
 * already achieved, e.g. the files are written.
 */
public abstract class IncrementalPlugin extends SpinePlugin {

    private static final String PROTO_SOURCE_SET = "proto";

    protected FileCollection protoSource(Project project) {
        return protoSource(project, MAIN);
    }

    protected FileCollection testProtoSource(Project project) {
        return protoSource(project, TEST);
    }

    private static FileCollection protoSource(Project project, SourceSetName sourceSetName) {
        SourceSet sourceSet = sourceSet(project, sourceSetName);
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

    private static SourceSet sourceSet(Project project, SourceSetName sourceSetName) {
        JavaPluginConvention javaConvention = project.getConvention()
                                                     .getPlugin(JavaPluginConvention.class);
        SourceSet sourceSet = javaConvention.getSourceSets()
                                            .findByName(sourceSetName.value());
        checkNotNull(sourceSet);
        return sourceSet;
    }

    protected FileCollection protoCompiledToJava(Project project) {
        String generationDir = getMainGenProtoDir(project);
        FileCollection files = project.fileTree(generationDir);
        return files;
    }

    protected FileCollection testProtoCompiledToJava(Project project) {
        String generationDir = getTestProtoSrcDir(project);
        FileCollection files = project.fileTree(generationDir);
        return files;
    }
}
