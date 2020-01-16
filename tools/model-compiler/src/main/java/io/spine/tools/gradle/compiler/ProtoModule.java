/*
 * Copyright 2020, TeamDev. All rights reserved.
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

import io.spine.tools.gradle.SourceScope;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.file.collections.ImmutableFileCollection;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.gradle.SourceScope.main;
import static io.spine.tools.gradle.SourceScope.test;
import static io.spine.tools.gradle.compiler.Extension.getTargetGenColumnsRootDir;
import static io.spine.tools.gradle.compiler.Extension.getTargetGenRejectionsRootDir;
import static io.spine.tools.gradle.compiler.Extension.getTargetTestGenColumnsRootDir;
import static io.spine.tools.gradle.compiler.Extension.getTargetTestGenRejectionsRootDir;

/**
 * A source code module with Protobuf.
 *
 * <p>A module is a set of source code, generated artifacts and temporary files aligned in a certain
 * layout. In terms of Gradle, a module is all the contents of a Gradle project.
 *
 * <p>It is assumed that the model compiler plugin is applied to the Gradle project represented by
 * this module.
 */
final class ProtoModule {

    /**
     * The name of the source set which defines where the Protobuf sources are located in a module.
     */
    @SuppressWarnings("DuplicateStringLiteralInspection") // local semantics
    private static final String PROTO_SOURCE_SET = "proto";

    private final Project project;

    /**
     * Creates a new instance atop of the given Gradle project.
     */
    ProtoModule(Project project) {
        this.project = checkNotNull(project);
    }

    /**
     * Obtains a {@linkplain FileCollection collection of files} containing all the production
     * Protobuf sources defined in this module.
     *
     * @apiNote The returned collection is a live view on the files, i.e. as the generated
     *        directory is changing, the contents of the collection are mutated.
     */
    FileCollection protoSource() {
        return protoSource(main);
    }

    /**
     * Obtains a {@linkplain FileCollection collection of files} containing all the test Protobuf
     * sources defined in this module.
     *
     * @apiNote The returned collection is a live view on the files, i.e. as the generated
     *        directory is changing, the contents of the collection are mutated.
     */
    FileCollection testProtoSource() {
        return protoSource(test);
    }

    private FileCollection protoSource(SourceScope sourceScope) {
        SourceSet sourceSet = sourceSet(sourceScope);
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

    private SourceSet sourceSet(SourceScope sourceScope) {
        JavaPluginConvention javaConvention = project.getConvention()
                                                     .getPlugin(JavaPluginConvention.class);
        SourceSet sourceSet = javaConvention.getSourceSets()
                                            .findByName(sourceScope.name());
        checkNotNull(sourceSet);
        return sourceSet;
    }

    /**
     * Obtains a {@linkplain FileCollection collection of files} containing all the production
     * {@linkplain io.spine.base.ThrowableMessage rejections} generated in this module.
     *
     * @apiNote The returned collection is a live view on the files, i.e. as the generated
     *        directory is changing, the contents of the collection are mutated.
     */
    FileCollection compiledRejections() {
        String targetDir = getTargetGenRejectionsRootDir(project);
        FileCollection files = project.fileTree(targetDir);
        return files;
    }

    /**
     * Obtains a {@linkplain FileCollection collection of files} containing all the test
     * {@linkplain io.spine.base.ThrowableMessage rejections} generated in this module.
     *
     * @apiNote The returned collection is a live view on the files, i.e. as the generated
     *        directory is changing, the contents of the collection are mutated.
     */
    FileCollection testCompiledRejections() {
        String targetDir = getTargetTestGenRejectionsRootDir(project);
        FileCollection files = project.fileTree(targetDir);
        return files;
    }

    /**
     * Obtains a {@linkplain FileCollection file collection} of all generated column-declaring
     * types for {@code main} source set.
     *
     * @apiNote The returned collection is a live view on the files, i.e. as the generated
     *        directory is changing, the contents of the collection are mutated.
     */
    FileCollection compiledColumns() {
        String targetDir = getTargetGenColumnsRootDir(project);
        FileCollection files = project.fileTree(targetDir);
        return files;
    }

    /**
     * Obtains a {@linkplain FileCollection file collection} of all generated column-declaring
     * types for {@code test} source set.
     *
     * @apiNote The returned collection is a live view on the files, i.e. as the generated
     *        directory is changing, the contents of the collection are mutated.
     */
    FileCollection testCompiledColumns() {
        String targetDir = getTargetTestGenColumnsRootDir(project);
        FileCollection files = project.fileTree(targetDir);
        return files;
    }
}
