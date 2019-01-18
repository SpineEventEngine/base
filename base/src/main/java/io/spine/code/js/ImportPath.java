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

package io.spine.code.js;

import io.spine.value.StringTypeValue;

import static com.google.common.base.Preconditions.checkState;

/**
 * A path of an import in JavaScript.
 */
public final class ImportPath extends StringTypeValue {

    private static final long serialVersionUID = 0L;
    /** The path separator used in JavaScript imports. Not platform-dependant. */
    private static final String IMPORT_PATHS_SEPARATOR = "/";
    private static final String PARENT_DIR = "../";
    private static final String CURRENT_DIR = "./";

    private ImportPath(String value) {
        super(value);
    }

    /**
     * Creates a new instance.
     *
     * @param value
     *         the value of the path
     * @return a new instance
     */
    public static ImportPath of(String value) {
        return new ImportPath(value);
    }

    /**
     * Obtains the path of the imported file.
     *
     * <p>The method skips a library name if it is present.
     */
    public String filePath() {
        String path = value();
        if (isRelativeToParent()) {
            return path;
        }
        int separatorIndex = path.indexOf(separator());
        checkState(separatorIndex != -1,
                   "The import path %s is expected to contain the separator `%s`.",
                   path, separator());
        String result = path.substring(separatorIndex + 1);
        return result;
    }

    private boolean isRelativeToParent() {
        return value().startsWith(parentDirectory());
    }

    /**
     * Obtains the separator used in JavaScript imports.
     */
    public static String separator() {
        return IMPORT_PATHS_SEPARATOR;
    }

    /**
     * Obtains the path to the parent directory.
     */
    public static String parentDirectory() {
        return PARENT_DIR;
    }

    /**
     * Obtains the path to the current directory.
     */
    public static String currentDirectory() {
        return CURRENT_DIR;
    }
}
