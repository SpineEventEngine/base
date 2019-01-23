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

package io.spine.io;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.io.File;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.io.Files.createParentDirs;

/**
 * Additional utilities for working with files.
 *
 * <p>These utilities are specific to Spine work with code files in code generation, project
 * structure analysis, and other tasks that are not covered by well-known file management libraries.
 *
 * <p>For more file-related utilities please see:
 * <ul>
 *     <li>{@link java.nio.file.Files Files} from NIO
 *     <li>{@link java.nio.file.Paths Paths} from NIO2
 *     <li>{@link com.google.common.io.Files Files from Guava}
 * </ul>
 */
public final class Files2 {

    /** Prevents instantiation of this utility class. */
    private Files2() {
    }

    /**
     * Ensures the given file existence.
     *
     * <p>Performs no action if the given file {@linkplain File#exists() exists}.
     *
     * <p>If the given file does not exist, it is created (with the parent directories,
     * if required).
     *
     * @param file a file to create
     * @return {@code true} if the named file does not exist and was successfully created;
     *         {@code false} if the named file already exists
     */
    @CanIgnoreReturnValue
    public static boolean ensureFile(File file) {
        try {
            if (!file.exists()) {
                createParentDirs(file);
                boolean result = file.createNewFile();
                return result;
            }
            return false;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Verifies if a passed file exists and has non-zero size.
     */
    public static boolean existsNonEmpty(File file) {
        checkNotNull(file);
        if (!file.exists()) {
            return false;
        }
        boolean nonEmpty = file.length() > 0;
        return nonEmpty;
    }
}
