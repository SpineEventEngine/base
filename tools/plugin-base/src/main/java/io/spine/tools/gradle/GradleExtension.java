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

package io.spine.tools.gradle;

import com.google.common.flogger.FluentLogger;
import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import io.spine.code.fs.DefaultPaths;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.gradle.api.Project;

import java.io.File;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.code.proto.FileDescriptors.DESC_EXTENSION;
import static java.util.Objects.requireNonNull;

/**
 * An abstract base for creating Gradle extensions.
 */
@SuppressWarnings({
        "PMD.MethodNamingConventions" /*  We use underscore for logging methods. */,
        "FloggerSplitLogStatement" /* See: https://github.com/SpineEventEngine/base/issues/612 */
})
public abstract class GradleExtension {

    protected static final FluentLogger logger = FluentLogger.forEnclosingClass();

    /**
     * The project where this extension is defined.
     *
     * <p>This field is {@code null} until {@link #injectProject(Project)} is called.
     */
    private @Nullable Project project;

    /**
     * Provides default paths in the {@link #project}.
     *
     * <p>The paths can be explicitly re-defined via setting properties of this extension.
     * Please see fields with the names ending with {@code Dir} for details.
     *
     * <p>This field is {@code null} until {@link #injectProject(Project)} is called.
     */
    private @Nullable DefaultPaths defaultPaths;

    protected final void injectProject(Project project) {
        checkNotNull(project);
        if (this.project != null) {
            boolean sameProject = project.equals(this.project);
            if (sameProject) {
                return;
            }
            // Replacing project, which is strange.
            _warn().log("Injecting another project (`%s`) instead of set before (`%s`).",
                        project, this.project);
        }
        this.project = project;
        this.defaultPaths = defaultPathsIn(project);
    }

    /**
     * Obtains non-null reference to a Gradle project in which this extension is defined.
     */
    protected final Project project() {
        return requireNonNull(this.project);
    }

    /**
     * Obtains default paths for the current project.
     *
     * <p>Overriding methods must only perform type casts to language-specific types they
     * produce in {@link #defaultPathsIn(Project)}.
     */
    @OverridingMethodsMustInvokeSuper
    protected DefaultPaths defaultPaths() {
        return requireNonNull(defaultPaths);
    }

    /**
     * Obtains the descriptor set file of the {@code "main"} artifact.
     */
    protected final File defaultMainDescriptor() {
        Project project = project();
        Artifact artifact = Artifact.newBuilder()
                .setGroup(project.getGroup().toString())
                .setName(project.getName())
                .setVersion(project.getVersion().toString())
                .build();
        String fileName = artifact.fileSafeId() + DESC_EXTENSION;
        Path mainDescriptor = defaultPaths()
                .buildDir()
                .descriptors()
                .mainDescriptors()
                .resolve(fileName);
        return mainDescriptor.toFile();
    }

    /**
     * Obtains the descriptor set file of the {@code "test"} artifact.
     */
    protected final File defaultTestDescriptor() {
        Project project = project();
        Artifact artifact = Artifact.newBuilder()
                .setGroup(project.getGroup().toString())
                .setName(project.getName())
                .setVersion(project.getVersion().toString())
                .useTestClassifier()
                .build();
        String fileName = artifact.fileSafeId() + DESC_EXTENSION;
        Path testDescriptor = defaultPaths()
                .buildDir()
                .descriptors()
                .testDescriptors()
                .resolve(fileName);
        return testDescriptor.toFile();
    }

    /**
     * Creates an instance of a default (language-specific) project paths taking the parent paths
     * from the passed Gradle project.
     */
    protected abstract DefaultPaths defaultPathsIn(Project project);

    @SuppressWarnings("unused")
    protected static FluentLogger.Api _error() {
        return logger.atSevere();
    }

    protected static FluentLogger.Api _warn() {
        return logger.atWarning();
    }

    protected static FluentLogger.Api _info() {
        return logger.atInfo();
    }

    protected static FluentLogger.Api _debug() {
        return logger.atFine();
    }
}
