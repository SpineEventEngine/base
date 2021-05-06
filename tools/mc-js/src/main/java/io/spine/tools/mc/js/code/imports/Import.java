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

import io.spine.tools.js.fs.FileName;
import io.spine.tools.code.CodeLine;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * An import of a file or a library.
 *
 * <p>The import is anonymous, but can be transformed
 * into a {@linkplain #namedAs(String) named} one.
 */
public class Import extends CodeLine {

    private static final String DEFAULT_IMPORT_ENDING = ".default;";

    private final String content;

    private Import(String content) {
        super();
        this.content = checkNotNull(content);
    }

    /**
     * Obtains an import of a file assuming the name is relative to the current directory.
     */
    public static Import fileRelativeToRoot(FileName file) {
        checkNotNull(file);
        String content = require(file.pathFromRoot());
        return new Import(content);
    }

    /**
     * Creates import statement.
     *
     * @param param the file to be imported
     */
    private static String require(String param) {
        return format("require('%s');", param);
    }

    /**
     * Obtains an import of a file relative to another file.
     *
     * @param fileToImport
     *         the name of the file to import
     * @param relativeTo
     *         the name of the file to compose the relative import path
     */
    public static Import fileRelativeTo(FileName fileToImport, FileName relativeTo) {
        checkNotNull(fileToImport);
        checkNotNull(relativeTo);
        String prefix = relativeTo.pathToRoot();
        return withPrefix(prefix, fileToImport);
    }

    /**
     * Obtains an import of a library.
     *
     * <p>It is allowed to specify just a library name like {@code google-protobuf}
     * or the path to a library file, e.g. {@code google-protobuf/proto-file.js}.
     */
    public static Import library(String libraryName) {
        checkNotNull(libraryName);
        String content = require(libraryName);
        return new Import(content);
    }

    private static Import withPrefix(String prefix, FileName fileToImport) {
        String path = prefix + fileToImport;
        String content = require(path);
        return new Import(content);
    }

    @Override
    public String content() {
        return content;
    }

    /**
     * Converts the import to the default import.
     *
     * <p>Does nothing if the import is already default.
     */
    public Import toDefault() {
        if (content.endsWith(DEFAULT_IMPORT_ENDING)) {
            return this;
        }
        String withoutSemicolon = content.substring(0, content.length() - 1);
        return new Import(withoutSemicolon + DEFAULT_IMPORT_ENDING);
    }

    /**
     * Obtains the named version of the import.
     *
     * @param importName
     *         the name for the import
     */
    public String namedAs(String importName) {
        String result = format("let %s = %s", importName, content());
        return result;
    }
}
