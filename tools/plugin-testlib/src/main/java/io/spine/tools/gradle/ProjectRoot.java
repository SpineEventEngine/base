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

package io.spine.tools.gradle;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.base.Preconditions.checkState;
import static java.nio.file.Files.exists;

/**
 * Finds a root of a project by presence of the {@link #VERSION_GRADLE_NAME} file.
 */
final class ProjectRoot {

    private static final String VERSION_GRADLE_NAME = "version.gradle";

    /** Prevents instantiation of this utility class. */
    private ProjectRoot() {
    }

    /**
     * Finds a root directory of the project by searching for the file
     * named {@link #VERSION_GRADLE_NAME version.gradle}.
     *
     * <p>Starts from the current directory, climbing up, if the file is not found.
     *
     * @throws IllegalStateException if the file is not found
     */
    static Path find() {
        Path workingFolderPath = Paths.get(".")
                                      .toAbsolutePath();
        Path extGradleDirPath = workingFolderPath;
        while (extGradleDirPath != null
                && !exists(extGradleDirPath.resolve(VERSION_GRADLE_NAME))) {
            extGradleDirPath = extGradleDirPath.getParent();
        }
        checkState(extGradleDirPath != null,
                   "%s file not found in %s or parent directories.",
                   VERSION_GRADLE_NAME,
                   workingFolderPath);
        return extGradleDirPath;
    }
}
