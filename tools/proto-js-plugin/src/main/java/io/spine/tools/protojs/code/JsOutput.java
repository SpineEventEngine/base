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

import com.google.common.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.join;
import static java.lang.System.lineSeparator;

public final class JsOutput {

    @VisibleForTesting
    static final String LINE_SEPARATOR = lineSeparator();

    private static final int STANDARD_INDENT = 2;

    private final int indent;
    private final List<CodeLine> codeLines;
    private int currentDepth;

    public JsOutput() {
        this.codeLines = new ArrayList<>();
        this.currentDepth = 0;
        this.indent = STANDARD_INDENT;
    }

    public JsOutput(int indent) {
        this.codeLines = new ArrayList<>();
        this.currentDepth = 0;
        this.indent = indent;
    }

    public void addLine(String codeLine) {
        checkNotNull(codeLine);
        CodeLine line = new CodeLine(codeLine, currentDepth);
        codeLines.add(line);
    }

    public void addEmptyLine() {
        addLine("");
    }

    @SuppressWarnings("DuplicateStringLiteralInspection") // Duplication with unrelated module.
    public void returnValue(String value) {
        checkNotNull(value);
        addLine("return " + value + ';');
    }

    public void addComment(String comment) {
        checkNotNull(comment);
        addLine("// " + comment);
    }

    /**
     * @param functionName full function name including type name
     */
    public void enterFunction(String functionName, String... functionArgs) {
        checkNotNull(functionName);
        checkNotNull(functionArgs);
        String argString = join(", ", functionArgs);
        addLine(functionName + " = function(" + argString + ") {");
        currentDepth++;
    }

    public void exitFunction() {
        currentDepth--;
        addLine("};");
    }

    public void enterIfBlock(String condition) {
        checkNotNull(condition);
        enterBlock("if (" + condition + ')');
    }

    public void enterElseBlock() {
        currentDepth--;
        addLine("} else {");
        currentDepth++;
    }

    public void enterBlock(String blockHeader) {
        checkNotNull(blockHeader);
        addLine(blockHeader + " {");
        currentDepth++;
    }

    public void exitBlock() {
        currentDepth--;
        addLine("}");
    }

    public void ifNull(String value) {
        checkNotNull(value);
        enterIfBlock(value + " === null");
    }

    public void ifNotNull(String value) {
        checkNotNull(value);
        enterIfBlock(value + " !== null");
    }

    public void ifNotUndefined(String value) {
        checkNotNull(value);
        enterIfBlock(value + " !== undefined");
    }

    public void ifNotNullOrUndefined(String value) {
        checkNotNull(value);
        enterIfBlock(value + " !== undefined && " + value + " !== null");
    }

    public void exportMap(String mapName) {
        checkNotNull(mapName);
        addLine("export const " + mapName + " = new Map([");
        increaseDepth();
    }

    public void addMapEntry(String entry, boolean isLast) {
        checkNotNull(entry);
        if (isLast) {
            addLine(entry);
        } else {
            addLine(entry + ',');
        }
    }

    public void quitMapDeclaration() {
        decreaseDepth();
        addLine("]);");
    }

    public void increaseDepth() {
        currentDepth++;
    }

    public void decreaseDepth() {
        currentDepth--;
    }

    @Override
    public String toString() {
        String result = codeLines.stream()
                                 .map(codeLine -> codeLine.printToString(indent))
                                 .collect(Collectors.joining(LINE_SEPARATOR));
        return result;
    }

    @VisibleForTesting
    int currentDepth() {
        return currentDepth;
    }
}
