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

package io.spine.tools.fs;

import com.google.errorprone.annotations.Immutable;
import io.spine.code.fs.AbstractDirectory;

import java.io.File;
import java.nio.file.Path;

import static io.spine.tools.fs.DirectoryName.dotSpine;

/**
 * This class represents a default directory structure for a Spine-based project of any language.
 *
 * <p>The descendants of the class contain the language-specific project structures.
 *
 * <p>The {@code DefaultProject} helps resolving names of the directories and files under the
 * project directory. It is expected that for most projects, the default values of paths remain
 * unchanged.
 */
@Immutable
public class DefaultPaths extends AbstractDirectory {

    public DefaultPaths(Path path) {
        super(path);
    }

    public BuildRoot buildRoot() {
        return BuildRoot.of(this);
    }

    /**
     * Obtains the directory for temporary Spine build artifacts.
     *
     * <p>Spine Gradle tasks may write some temporary files into this directory.
     *
     * <p>The directory is deleted on {@code :pre-clean"}.
     */
    public File tempArtifacts() {
        File result = new File(path().toFile(), dotSpine.value());
        return result;
    }
}
