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

public class JsImportGenerator {

    private static final String PARENT_DIR = "../";
    private static final String CURRENT_DIR = "./";
    private static final String PATH_SEPARATOR = "/";

    private final String importPrefix;

    private JsImportGenerator(String importPrefix) {
        this.importPrefix = importPrefix;
    }

    public static JsImportGenerator createFor(String filePath) {
        String pathToRoot = composePathToRoot(filePath);
        return new JsImportGenerator(pathToRoot);
    }

    public String importStatement(String fileToImport) {
        String importPath = importPrefix + fileToImport;
        String importStatement = "require('" + importPath + "');";
        return importStatement;
    }

    public String namedImport(String fileToImport, String importName) {
        String importPath = importPrefix + fileToImport;
        String importStatement = "let " + importName + " = require('" + importPath + "');";
        return importStatement;
    }

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
