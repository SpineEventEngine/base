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

package io.spine.tools.mc.js.code.imports;

import io.spine.tools.fs.FileReference;
import io.spine.logging.Logging;

import java.io.File;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An import statement extracted from a source file.
 */
final class ImportStatement implements Logging {

    private static final String IMPORT_START = "require('";
    private static final String IMPORT_END = "')";

    private final File file;
    private final String text;

    /**
     * Creates a new instance.
     *
     * @param file
     *         the file which declares the import statement
     * @param text
     *         the text of the statement
     */
    public ImportStatement(File file, String text) {
        checkArgument(
                declaredIn(text),
                "An import statement should contain: `%s ... %s`.", IMPORT_START, IMPORT_END
        );
        checkNotNull(file);
        this.text = text;
        this.file = file;
    }

    /**
     * Tells whether the line contains an import statement.
     */
    static boolean declaredIn(String line) {
        return line.contains(IMPORT_START);
    }

    /**
     * Obtains the file reference used in this import.
     */
    public FileReference fileRef() {
        int beginIndex = text.indexOf(IMPORT_START) + IMPORT_START.length();
        int endIndex = text.indexOf(IMPORT_END, beginIndex);
        String importPath = text.substring(beginIndex, endIndex);
        return FileReference.of(importPath);
    }

    /**
     * Obtains a new instance with the updated path in the import statement.
     */
    public ImportStatement replaceRef(CharSequence newFileRef) {
        String updatedText = text.replace(fileRef().value(), newFileRef);
        return new ImportStatement(file, updatedText);
    }

    /**
     * Obtains the text of the import.
     */
    public String text() {
        return text;
    }

    /**
     * Tells whether the imported file is present on a file system.
     */
    public boolean importedFileExists() {
        Path filePath = importedFilePath();
        boolean exists = filePath.toFile()
                                 .exists();
        _debug().log("Checking if the imported file `%s` exists, result: %b.", filePath, exists);
        return exists;
    }

    /**
     * Obtains the absolute path to the imported file.
     */
    public Path importedFilePath() {
        Path filePath = sourceDirectory().resolve(fileRef().value());
        return filePath.normalize();
    }

    /**
     * Obtains the path of the directory with the file containing this import.
     */
    public Path sourceDirectory() {
        return file.getParentFile()
                   .toPath();
    }
}
