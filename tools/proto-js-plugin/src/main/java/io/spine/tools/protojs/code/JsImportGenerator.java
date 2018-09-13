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

package io.spine.tools.protojs.code;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The helper that generates imports for the JS code.
 *
 * <p>Currently all imports are generated in the CommonJS style.
 *
 * @author Dmytro Kuzmin
 */
public final class JsImportGenerator {

    /**
     * The path to parent dir.
     */
    private static final String PARENT_DIR = "../";

    /**
     * The path to the current dir.
     */
    private static final String CURRENT_DIR = "./";

    /**
     * The path separator used for imports.
     */
    private static final String PATH_SEPARATOR = "/";

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
     * Creates a {@code JsImportGenerator} set to generate imports for the specified file.
     *
     * <p>The provided file path should be relative to the desired import root (e.g. sources root,
     * generated protos root).
     *
     * <p>Unlike the Java {@link java.nio.file.Path}, the {@code filePath} must be split by the
     * {@link #PATH_SEPARATOR} independently of OS as it represents the path inside the JS code.
     *
     * @param filePath
     *         the file for which the imports will be generated
     * @return the {@code JsImportGenerator} that will generate imports relatively to this file
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
     * <p>Named import is a statement of type "{@code let a = require('./b')}".
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
        String importStatement = "require('" + fileToImport + "');";
        return importStatement;
    }

    /**
     * Generates a named JS import.
     *
     * <p>Named import is a statement of type "{@code let a = require('./b')}".
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
        String importStatement = "let " + importName + " = require('" + fileToImport + "');";
        return importStatement;
    }

    /**
     * Composes the path from the given file to the import root.
     *
     * <p>The {@code filePath} is assumed to be relative to the import root, so basically the
     * method replaces all preceding path elements by the {@link #PARENT_DIR}.
     */
    private static String composePathToRoot(String filePath) {
        String[] pathElements = filePath.split(PATH_SEPARATOR);
        int fileLocationDepth = pathElements.length - 1;
        StringBuilder pathToRoot = new StringBuilder(fileLocationDepth);
        for (int i = 0; i < fileLocationDepth; i++) {
            pathToRoot.append(PARENT_DIR);
        }
        String result = pathToRoot.length() > 0 ? pathToRoot.toString() : CURRENT_DIR;
        return result;
    }
}
