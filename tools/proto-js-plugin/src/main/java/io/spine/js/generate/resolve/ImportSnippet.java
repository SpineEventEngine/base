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

package io.spine.js.generate.resolve;

import io.spine.code.js.FileName;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * A line of a source Javascript file with an import statement, which is going to be resolved.
 */
public class ImportSnippet {

    private static final String IMPORT_BEGIN_SIGN = "require('";
    private static final String IMPORT_END_SIGN = "')";
    private static final String IMPORT_PATHS_SEPARATOR = "/";
    @SuppressWarnings("DuplicateStringLiteralInspection" /* Has a different meaning. */)
    private static final String SPINE_SIGN = "spine";

    private final String text;
    private final FileName fileName;

    /**
     * Creates a new instance.
     *
     * @param text
     *         the line with an import statement
     * @param fileName
     *         the name of the file the text belongs to
     */
    public ImportSnippet(String text, FileName fileName) {
        checkArgument(hasImport(text), "The text should contain an import statement.");
        checkNotNull(fileName);
        this.text = text;
        this.fileName = fileName;
    }

    /**
     * Tells whether the line contains an import statement.
     */
    static boolean hasImport(String line) {
        boolean result = line.contains(IMPORT_BEGIN_SIGN);
        return result;
    }

    /**
     * Obtains the path specified in the import statement.
     */
    String path() {
        int beginIndex = text.indexOf(IMPORT_BEGIN_SIGN) + IMPORT_BEGIN_SIGN.length();
        int endIndex = text.indexOf(IMPORT_END_SIGN, beginIndex);
        String importPath = text.substring(beginIndex, endIndex);
        return importPath;
    }

    /**
     * Obtains the path of the imported file.
     *
     * <p>Unlike {@link #path()} the method skips a library name if it is present.
     */
    String importedFilePath() {
        String path = path();
        boolean relativeToParent = path.startsWith("../");
        if (relativeToParent) {
            return path;
        }
        int separatorIndex = path.indexOf(IMPORT_PATHS_SEPARATOR);
        checkState(separatorIndex != -1,
                   "The import path %s is expected to contain the separator `%s`.",
                   path, IMPORT_PATHS_SEPARATOR);
        String result = path.substring(separatorIndex + 1);
        return result;
    }

    /**
     * Obtains a new instance with the updated path in the import statement.
     */
    ImportSnippet replacePath(CharSequence newPath) {
        String updatedText = text.replace(path(), newPath);
        return new ImportSnippet(updatedText, fileName);
    }

    /**
     * Obtains the text of the import.
     */
    String text() {
        return text;
    }

    /**
     * Obtains the name of the file the import belongs to.
     */
    FileName fileName() {
        return fileName;
    }

    /**
     * Tells whether the import refers to a Spine library.
     *
     * @return {@code true} if the import path starts with {@code spine}, {@code false} otherwise
     */
    boolean isSpine() {
        boolean result = path().startsWith(SPINE_SIGN);
        return result;
    }
}
