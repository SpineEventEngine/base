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

package io.spine.testing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * Utilities for creating temporary directories.
 */
public class TempDir {

    /** Prevents direct instantiation. */
    private TempDir() {
    }

    /**
     * Creates a temporary directory at the location obtained from the system property
     * {@code java.io.tmpdir} with the passed prefix.
     *
     * <p>Replaces deprecated {@link com.google.common.io.Files#createTempDir()}.
     */
    @SuppressWarnings("deprecation") // to reference the deprecated method in Javadoc
    public static File withPrefix(String prefix) {
        @SuppressWarnings("AccessOfSystemProperties")
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        Path directory;
        try {
             directory = Files.createTempDirectory(baseDir.toPath(), prefix);
             return directory.toFile();
        } catch (IOException e) {
            throw newIllegalStateException(e, "Unable to create temp dir under %s", baseDir);
        }
    }
}
