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

import io.spine.code.js.FileReference;
import io.spine.logging.Logging;

import java.io.File;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A line of a source JavaScript file with an import statement, which is going to be resolved.
 */
public class ImportSnippet implements Logging {

    private static final String IMPORT_BEGIN_SIGN = "require('";
    private static final String IMPORT_END_SIGN = "')";

    private final String text;
    private final File originFile;

    /**
     * Creates a new instance.
     *
     * @param text
     *         the line with an import statement
     * @param originFile
     *         the name of the file the import belongs to
     */
    public ImportSnippet(String text, File originFile) {
        checkArgument(hasImport(text), "The text should contain an import statement.");
        checkNotNull(originFile);
        this.text = text;
        this.originFile = originFile;
    }

    /**
     * Tells whether the line contains an import statement.
     */
    static boolean hasImport(String line) {
        boolean result = line.contains(IMPORT_BEGIN_SIGN);
        return result;
    }

    /**
     * Obtains the file reference used in this import.
     */
    FileReference path() {
        int beginIndex = text.indexOf(IMPORT_BEGIN_SIGN) + IMPORT_BEGIN_SIGN.length();
        int endIndex = text.indexOf(IMPORT_END_SIGN, beginIndex);
        String importPath = text.substring(beginIndex, endIndex);
        return FileReference.of(importPath);
    }

    /**
     * Obtains a new instance with the updated path in the import statement.
     */
    ImportSnippet replacePath(CharSequence newPath) {
        String updatedText = text.replace(path().value(), newPath);
        return new ImportSnippet(updatedText, originFile);
    }

    /**
     * Obtains the text of the import.
     */
    String text() {
        return text;
    }

    /**
     * Tells whether the imported file is present on a file system.
     */
    boolean importedFileExists() {
        Path filePath = importedFilePath();
        boolean exists = filePath.toFile()
                                 .exists();
        _debug("Checking if the imported file {} exists, result: {}", filePath, exists);
        return exists;
    }

    /**
     * Obtains the absolute path to the imported file.
     */
    Path importedFilePath() {
        FileReference fileReference = path();
        Path filePath = originDirectory().resolve(fileReference.value());
        return filePath.normalize();
    }

    private Path originDirectory() {
        return originFile.getParentFile()
                         .toPath();
    }
}
