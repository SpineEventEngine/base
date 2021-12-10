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

package io.spine.io;

import java.io.File;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Additional utilities for working with files.
 */
public final class Files2 {

    /** Prevents instantiation of this utility class. */
    private Files2() {
    }

    /**
     * Verifies if a passed file exists and has non-zero size.
     */
    public static boolean existsNonEmpty(File file) {
        checkNotNull(file);
        if (!file.exists()) {
            return false;
        }
        var nonEmpty = file.length() > 0;
        return nonEmpty;
    }

    /**
     * Normalizes and transforms the passed path to an absolute file reference.
     */
    @SuppressWarnings("unused") /* Part of the public API. */
    public static File toAbsolute(String path) {
        checkNotNull(path);
        var file = new File(path);
        var normalized = file.toPath().normalize();
        var result = normalized.toAbsolutePath().toFile();
        return result;
    }

    /**
     * Obtains the value of the {@code System} property for a temporary directory.
     */
    @SuppressWarnings("AccessOfSystemProperties")
    public static String systemTempDir() {
        return System.getProperty("java.io.tmpdir");
    }
}
