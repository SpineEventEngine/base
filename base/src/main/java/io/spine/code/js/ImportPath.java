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
 * A path to a file used in a JavaScript import statement.
 */
public final class ImportPath extends StringTypeValue {

    private static final long serialVersionUID = 0L;
    /** The path separator used in JavaScript imports. Not platform-dependant. */
    private static final String IMPORT_PATH_SEPARATOR = "/";
    private static final String PARENT_DIR = "../";
    private static final String CURRENT_DIR = "./";
    @SuppressWarnings("DuplicateStringLiteralInspection" /* Has a different meaning. */)
    private static final String SPINE_SIGN = "spine";

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
     * Obtains the path of the imported file skipping the library name.
     *
     * <p>Does nothing if a library name if absent.
     */
    public ImportPath skipLibrary() {
        String path = value();
        if (isRelativeToParent()) {
            return this;
        }
        int separatorIndex = path.indexOf(separator());
        checkState(separatorIndex != -1,
                   "The import path %s is expected to contain the separator `%s`.",
                   path, separator());
        String result = path.substring(separatorIndex + 1);
        return of(result);
    }

    /**
     * Obtains the name of the imported file.
     */
    public FileName fileName() {
        String name = value();
        if (name.startsWith(CURRENT_DIR)) {
            name = name.substring(CURRENT_DIR.length());
        }
        while (name.startsWith(PARENT_DIR)) {
            name = name.substring(PARENT_DIR.length());
        }
        return FileName.of(name);
    }

    /**
     * Tells if the path is relative.
     *
     * @return {@code true} if the path starts with the current or parent directory reference
     */
    public boolean isRelative() {
        boolean result = isRelativeToParent() || isRelativeToCurrent();
        return result;
    }

    /**
     * Tells whether the import refers to a Spine library.
     *
     * @return {@code true} if the import path starts with {@code spine}, {@code false} otherwise
     */
    public boolean isSpine() {
        boolean result = value().startsWith(SPINE_SIGN);
        return result;
    }

    private boolean isRelativeToParent() {
        return value().startsWith(parentDirectory());
    }

    private boolean isRelativeToCurrent() {
        return value().startsWith(currentDirectory());
    }

    /**
     * Obtains the separator used in JavaScript imports.
     */
    public static String separator() {
        return IMPORT_PATH_SEPARATOR;
    }

    /**
     * Obtains the string used to reference the parent directory in imports.
     */
    public static String parentDirectory() {
        return PARENT_DIR;
    }

    /**
     * Obtains the string used to reference the current directory in imports.
     */
    public static String currentDirectory() {
        return CURRENT_DIR;
    }
}
