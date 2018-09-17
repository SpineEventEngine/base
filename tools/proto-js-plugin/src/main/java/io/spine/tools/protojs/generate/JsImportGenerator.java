/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.tools.protojs.generate;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.protojs.generate.JsOutput.VARIABLE_MODIFIER;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Collections.nCopies;

/**
 * The helper that generates imports for the JS code.
 *
 * <p>Currently all imports are generated in the CommonJS style.
 *
 * @author Dmytro Kuzmin
 */
public final class JsImportGenerator {

    /**
     * The path separator used for imports.
     */
    private static final String PATH_SEPARATOR = "/";

    /**
     * The path to parent dir.
     */
    private static final String PARENT_DIR = "../";

    /**
     * The path to the current dir.
     */
    private static final String CURRENT_DIR = "./";

    /**
     * The import format.
     *
     * <p>The placeholder represents the file to be imported.
     */
    private static final String IMPORT_FORMAT = "require('%s');";

    /**
     * The value which is prepended to every import.
     *
     * <p>In case of JS proto definitions, this prefix represents the path from the current file
     * to the proto JS location root.
     */
    private final String importPrefix;

    private JsImportGenerator(String importPrefix) {
        this.importPrefix = importPrefix;
    }

    /**
     * Creates a {@code JsImportGenerator} to generate imports for the specified file.
     *
     * <p>The provided file path should be relative to the desired import root (e.g. sources root,
     * generated protos root).
     *
     * @param filePath
     *         the file for which the imports will be generated
     * @return the {@code JsImportGenerator} that generates imports relative to this file
     */
    public static JsImportGenerator createFor(String filePath) {
        checkNotNull(filePath);
        String pathToRoot = composePathToRoot(filePath);
        return new JsImportGenerator(pathToRoot);
    }

    /**
     * Generates a JS import statement with a stored {@code importPrefix}.
     *
     * @param fileToImport
     *         the path from the import root to the file which should be imported
     * @return the import statement
     */
    public String importStatement(String fileToImport) {
        checkNotNull(fileToImport);
        String importPath = importPrefix + fileToImport;
        String importStatement = rawImport(importPath);
        return importStatement;
    }

    /**
     * Generates a named JS import with a stored {@code importPrefix}.
     *
     * <p>Named import is a statement of type {@code let a = require('./b')}.
     *
     * @param fileToImport
     *         the path from the import root to the file which should be imported
     * @param importName
     *         the name of the variable which will hold the imported module
     * @return the named import statement
     */
    public String namedImport(String fileToImport, String importName) {
        checkNotNull(fileToImport);
        checkNotNull(importName);
        String importPath = importPrefix + fileToImport;
        String importStatement = rawNamedImport(importPath, importName);
        return importStatement;
    }

    /**
     * Generates a JS import statement.
     *
     * @param fileToImport
     *         the path from the import root to the file which should be imported
     * @return the import statement
     */
    public static String rawImport(String fileToImport) {
        checkNotNull(fileToImport);
        String importStatement = format(IMPORT_FORMAT, fileToImport);
        return importStatement;
    }

    /**
     * Generates a named JS import.
     *
     * <p>Named import is a statement of type {@code let a = require('./b')}.
     *
     * @param fileToImport
     *         the path from the import root to the file which should be imported
     * @param importName
     *         the name of the variable which will hold the imported module
     * @return the named import statement
     */
    public static String rawNamedImport(String fileToImport, String importName) {
        checkNotNull(fileToImport);
        checkNotNull(importName);
        String importStatement = rawImport(fileToImport);
        String namedImport = VARIABLE_MODIFIER + ' ' + importName + " = " + importStatement;
        return namedImport;
    }

    /**
     * Composes the path from the given file to its root.
     *
     * <p>Basically, the method replaces all preceding path elements by the {@link #PARENT_DIR}.
     */
    private static String composePathToRoot(String filePath) {
        String[] pathElements = filePath.split(PATH_SEPARATOR);
        int fileLocationDepth = pathElements.length - 1;
        List<String> pathToRootElements = nCopies(fileLocationDepth, PARENT_DIR);
        String pathToRoot = join("", pathToRootElements);
        String result = pathToRoot.isEmpty() ? CURRENT_DIR : pathToRoot;
        return result;
    }
}
