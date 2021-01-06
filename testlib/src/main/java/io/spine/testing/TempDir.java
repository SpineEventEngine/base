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

package io.spine.testing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.newIllegalStateException;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * Utilities for creating temporary directories.
 *
 * @apiNote Replaces deprecated {@code com.google.common.io.Files#createTempDir()}.
 */
public final class TempDir {

    /** Prevents direct instantiation. */
    private TempDir() {
    }

    /**
     * Creates a temporary directory.
     *
     * <p>The parent directory for the created is obtained from the system property
     * {@code java.io.tmpdir}.
     *
     * <p>The prefix is used by the underlying JDK implementation and is <em>NOT</em> guaranteed
     * in the name of the created directory name.
     *
     * @param prefix
     *         this value would be used for generating the name of the created directory,
     *         cannot be empty, or blank
     * @param attrs
     *         an optional list of file attributes to set atomically when
     *         creating the directory
     * @throws IllegalStateException
     *         if the directory could not be created
     * @see java.nio.file.Files#createTempDirectory(Path, String, FileAttribute...)
     */
    public static File withPrefix(String prefix, FileAttribute<?>... attrs) {
        checkNotNull(prefix);
        checkNotEmptyOrBlank(prefix);
        @SuppressWarnings("AccessOfSystemProperties")
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        Path directory;
        try {
             directory = Files.createTempDirectory(baseDir.toPath(), prefix, attrs);
             return directory.toFile();
        } catch (IOException e) {
            throw newIllegalStateException(e, "Unable to create temp dir under `%s`.", baseDir);
        }
    }

    /**
     * Creates a temporary directory for the passed test suite class.
     *
     * <p>The parent directory for the created is obtained from the system property
     * {@code java.io.tmpdir}.
     *
     * <p>Implementation <em>may</em> use a simple name of the passed class as the prefix
     * for the name of the generated directory, but this is <em>NOT</em> guaranteed.
     *
     * @param testSuite
     *         the test suite class which needs the temporary directory
     * @param attrs
     *         an optional list of file attributes to set atomically when
     *         creating the directory
     * @throws IllegalStateException
     *         if the directory could not be created
     * @see #withPrefix(String, FileAttribute...)
     * @see java.nio.file.Files#createTempDirectory(Path, String, FileAttribute...)
     */
    public static File forClass(Class<?> testSuite, FileAttribute<?>... attrs) {
        checkNotNull(testSuite);
        String prefix = testSuite.getSimpleName();
        return withPrefix(prefix, attrs);
    }
}
