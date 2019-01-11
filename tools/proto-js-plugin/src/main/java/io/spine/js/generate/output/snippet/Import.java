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

package io.spine.js.generate.output.snippet;

import com.google.common.base.Strings;
import io.spine.code.js.FileName;
import io.spine.js.generate.output.CodeLine;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * An import of a file or a library.
 *
 * <p>The import is anonymous, but can be transformed
 * into a {@linkplain #namedAs(String) named} one.
 */
public class Import extends CodeLine {

    /**
     * The path to parent directory.
     */
    private static final String PARENT_DIR = "../";

    /**
     * The import format.
     *
     * <p>The placeholder represents the file to be imported.
     */
    private static final String IMPORT_FORMAT = "require('%s');";

    private static final String DEFAULT_IMPORT_FORMAT = "require('%s').default;";

    /**
     * The named import format.
     */
    private static final String NAMED_IMPORT_FORMAT = "let %s = %s";

    /**
     * The path to the current directory.
     */
    private static final String CURRENT_DIR = "./";

    private final String content;

    private Import(String content) {
        this.content = checkNotNull(content);
    }

    /**
     * Obtains an import of a file assuming the name is relative to the current directory.
     */
    public static Import fileRelativeToRoot(FileName file) {
        checkNotNull(file);
        return withPrefix(CURRENT_DIR, file);
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
        String prefix = composePathToRoot(relativeTo);
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
        String content = format(IMPORT_FORMAT, libraryName);
        return new Import(content);
    }

    /**
     * Obtains the default import of the library.
     */
    public static Import libraryDefault(String libraryName) {
        checkNotNull(libraryName);
        String content = format(DEFAULT_IMPORT_FORMAT, libraryName);
        return new Import(content);
    }

    private static Import withPrefix(String prefix, FileName fileToImport) {
        String path = prefix + fileToImport;
        String content = format(IMPORT_FORMAT, path);
        return new Import(content);
    }

    @Override
    public String content() {
        return content;
    }

    /**
     * Obtains the named version of the import.
     *
     * @param importName
     *         the name for the import
     */
    public String namedAs(String importName) {
        String result = format(NAMED_IMPORT_FORMAT, importName, content());
        return result;
    }

    /**
     * Composes the path from the given file to its root.
     *
     * <p>Basically, the method replaces all preceding path elements by the {@link #PARENT_DIR}.
     */
    private static String composePathToRoot(FileName fileName) {
        String[] pathElements = fileName.pathElements();
        int fileLocationDepth = pathElements.length - 1;
        String pathToRoot = Strings.repeat(PARENT_DIR, fileLocationDepth);
        String result = pathToRoot.isEmpty() ? CURRENT_DIR : pathToRoot;
        return result;
    }
}
