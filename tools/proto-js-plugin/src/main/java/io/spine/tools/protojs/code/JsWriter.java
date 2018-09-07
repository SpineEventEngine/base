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

import static java.lang.String.join;
import static java.lang.System.lineSeparator;

public final class JsWriter {

    private static final String LINE_SEPARATOR = lineSeparator();
    private static final int STANDARD_INDENT = 2;

    private final JsOutput generatedCode;
    private int currentDepth;

    public JsWriter() {
        this.generatedCode = new JsOutput(LINE_SEPARATOR, STANDARD_INDENT);
        this.currentDepth = 0;
    }

    public JsWriter(int indent) {
        this.generatedCode = new JsOutput(LINE_SEPARATOR, indent);
        this.currentDepth = 0;
    }

    public void addLine(String lineOfCode) {
        generatedCode.addLine(lineOfCode, currentDepth);
    }

    public void addImports(String importingFile, String... filesToImport) {
        JsImportWriter generator = JsImportWriter.createFor(importingFile);
        for (String fileToImport : filesToImport) {
            String importStatement = generator.importStatement(fileToImport);
            addLine(importStatement);
        }
    }

    public void addNamedImports(String importingFile, String... filesToImport) {
        JsImportWriter generator = JsImportWriter.createFor(importingFile);
        for (String fileToImport : filesToImport) {
            String importStatement = generator.namedImportStatement(fileToImport);
            addLine(importStatement);
        }
    }

    public void addEmptyLine() {
        addLine("");
    }

    public void increaseDepth() {
        currentDepth++;
    }

    public void decreaseDepth() {
        currentDepth--;
    }

    /**
     * @param functionName full function name including type name
     * @param functionArgs
     */
    public void enterFunction(String functionName, String... functionArgs) {
        String argString = join(", ", functionArgs);
        addLine(functionName + " = function(" + argString + ") {");
        currentDepth++;
    }

    public void exitFunction() {
        currentDepth--;
        addLine("};");
    }

    public void enterIfBlock(String condition) {
        enterBlock("if (" + condition + ')');
    }

    public void enterElseBlock() {
        currentDepth--;
        addLine("} else {");
        currentDepth++;
    }

    public void enterBlock(String blockHeader) {
        addLine(blockHeader + " {");
        currentDepth++;
    }

    public void exitBlock() {
        currentDepth--;
        addLine("}");
    }

    public JsOutput getGeneratedCode() {
        return generatedCode;
    }
}
