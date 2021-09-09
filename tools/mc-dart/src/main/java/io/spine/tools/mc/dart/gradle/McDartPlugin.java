/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.tools.mc.dart.gradle;

import io.spine.tools.gradle.SpinePlugin;
import io.spine.tools.dart.fs.DartFile;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileTree;

import java.io.File;
import java.nio.file.Path;

import static io.spine.tools.gradle.BaseTaskName.assemble;
import static io.spine.tools.mc.dart.gradle.McDartTaskName.copyGeneratedDart;
import static io.spine.tools.mc.dart.gradle.McDartTaskName.resolveImports;
import static io.spine.tools.mc.dart.gradle.McDartTaskName.resolveTestImports;

/**
 * A Gradle plugin which configures Protobuf Dart code generation.
 *
 * <p>Generates mapping between Protobuf type URLs and Dart types and reflective
 * descriptors (a.k.a. {@code BuilderInfo}s).
 *
 * @see ProtocConfig
 */
public final class McDartPlugin extends SpinePlugin {

    @Override
    public void apply(Project project) {
        McDartExtension extension = new McDartExtension(project);
        extension.register();

        Plugin<Project> protocConfig = new ProtocConfig();
        protocConfig.apply(project);

        extension.createMainCopyTaskIn(project);
        extension.createTestCopyTaskIn(project);
        createMainResolveImportTask(project, extension);
        createTestResolveImportTask(project, extension);
    }

    private void createMainResolveImportTask(Project project, McDartExtension extension) {
        DirectoryProperty rootDir = extension.getGeneratedMainDir();
        doCreateResolveImportsTask(project, extension, rootDir, false);
    }

    private void createTestResolveImportTask(Project project, McDartExtension extension) {
        DirectoryProperty rootDir = extension.getGeneratedTestDir();
        doCreateResolveImportsTask(project, extension, rootDir, true);
    }

    private void doCreateResolveImportsTask(Project project,
                                            McDartExtension extension,
                                            DirectoryProperty rootDir,
                                            boolean tests) {
        Action<Task> action = task -> {
            FileTree generatedFiles = rootDir.getAsFileTree();
            generatedFiles.forEach(file -> resolveImports(file, extension));
        };
        newTask(tests ? resolveTestImports : resolveImports, action)
                .insertAfterTask(copyGeneratedDart)
                .insertBeforeTask(assemble)
                .applyNowTo(project);
    }

    private void resolveImports(File sourceFile, McDartExtension extension) {
        _debug().log("Resolving imports in the file `%s`.", sourceFile);
        DartFile file = DartFile.read(sourceFile.toPath());
        Path libPath = extension.getLibDir()
                                .getAsFile()
                                .map(File::toPath)
                                .get();
        file.resolveImports(libPath, extension.modules());
    }
}
