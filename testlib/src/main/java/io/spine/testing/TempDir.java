/*
 * Copyright 2023, TeamDev. All rights reserved.
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
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.Locale;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * Utilities for creating temporary directories.
 *
 * @apiNote Replaces deprecated {@code com.google.common.io.Files#createTempDir()}.
 */
public final class TempDir {

    /**
     * The directory under which all instances of this class will be created.
     *
     * <p>This directory is automatically deleted when JVM exists.
     * Please see the {@code static} block below.
     */
    private static final Path baseDir;

    static {
        baseDir = createBaseDir();
        Testing.deleteRecursivelyOnShutdownHook(baseDir);
    }

    /** Prevents direct instantiation. */
    private TempDir() {
    }

    /**
     * Creates a directory named after the package of this class under a directory
     * specified by the {@link Testing#systemTempDir()}.
     */
    private static Path createBaseDir() {
        var tmpDir = Testing.systemTempDir();
        var packageName = TempDir.class.getPackageName();
        var baseDir = Paths.get(tmpDir, packageName);
        return Testing.ensureDirectory(baseDir);
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
        Testing.checkNotEmptyOrBlank(prefix);
        try {
            var directory = Files.createTempDirectory(baseDir, prefix, attrs);
            return directory.toFile();
        } catch (IOException e) {
            throw newIllegalStateException(
                    e, "Unable to create temp dir under `%s` (prefix: `%s`).", baseDir, prefix
            );
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
        var prefix = testSuite.getSimpleName();
        return withPrefix(prefix, attrs);
    }

    private static IllegalStateException
    newIllegalStateException(Throwable cause, String format, Object... args) {
        var errMsg = format(Locale.ROOT, format, args);
        throw new IllegalStateException(errMsg, cause);
    }

}
