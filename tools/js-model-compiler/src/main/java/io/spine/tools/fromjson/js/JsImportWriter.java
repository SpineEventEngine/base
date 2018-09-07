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

package io.spine.tools.fromjson.js;

import java.util.regex.Pattern;

class JsImportWriter {

    private static final Pattern PARENT_OR_CURRENT_DIR = Pattern.compile("(\\.\\./|\\./)");
    private static final Pattern JS_EXTENSION = Pattern.compile(".js", Pattern.LITERAL);
    private final String importPrefix;

    private JsImportWriter(String importPrefix) {
        this.importPrefix = importPrefix;
    }

    static JsImportWriter createFor(String filePath) {
        String importPrefix = composePathToRoot(filePath);
        return new JsImportWriter(importPrefix);
    }

    String importStatement(String fileToImport) {
        String importPath = importPrefix + fileToImport;
        String importStatement = "require('" + importPath + "');";
        return importStatement;
    }

    String namedImportStatement(String fileToImport) {
        String importPath = importPrefix + fileToImport;
        String importName = generateImportName(importPath);
        String importStatement = "let " + importName + " = require('" + importPath + "');";
        return importStatement;
    }

    private static String composePathToRoot(String filePath) {
        String[] pathElements = filePath.split("/");
        int fileLocationDepth = pathElements.length - 1;
        StringBuilder pathToRoot = new StringBuilder(fileLocationDepth);
        for (int i = 0; i < fileLocationDepth; i++) {
            pathToRoot.append("../");
        }
        String result = pathToRoot.length() > 0 ? pathToRoot.toString() : "./";
        return result;
    }

    private static String generateImportName(String importPath) {
        String pathRelativeToRoot = PARENT_OR_CURRENT_DIR.matcher(importPath)
                                                         .replaceAll("");
        String pathWithoutExtension = JS_EXTENSION.matcher(pathRelativeToRoot)
                                                  .replaceAll("");
        String importName = pathWithoutExtension.replace('/', '_');
        return importName;
    }
}
