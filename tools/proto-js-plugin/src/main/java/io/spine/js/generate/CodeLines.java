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

package io.spine.js.generate;

import com.google.common.annotations.VisibleForTesting;
import io.spine.code.Depth;
import io.spine.code.Indent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.join;
import static java.lang.System.lineSeparator;

/**
 * The aggregator of the JavaScript output.
 *
 * <p>This class serves as the JS code storage as well as provides the convenience methods for
 * adding the code.
 *
 * <p>The writable representation of the output may be obtained through the {@link #toString()}
 * method.
 */
@SuppressWarnings({"DuplicateStringLiteralInspection"
        /* The generated code duplicates the code used in test that checks it. */,
        "ClassWithTooManyMethods" /* A lot of simple utility methods for code generation. */})
public final class CodeLines {

    @VisibleForTesting
    static final String LINE_SEPARATOR = lineSeparator();

    private static final Indent STANDARD_INDENTATION = Indent.of2();

    /**
     * The indentation of the code, i.e. how many spaces each depth level takes.
     */
    private final Indent indentation;

    /**
     * The aggregator of the JS code.
     */
    private final List<IndentedLine> codeLines;

    /**
     * The current depth of the code on which the next line will be added.
     */
    private Depth currentDepth;

    /**
     * Creates an instance of the {@code JsOutput} with the default indentation.
     */
    public CodeLines() {
        this(STANDARD_INDENTATION);
    }

    /**
     * Creates an instance of the {@code JsOutput} with the custom indentation.
     *
     * @param indentation
     *         the indentation to use
     */
    public CodeLines(Indent indentation) {
        this.codeLines = new ArrayList<>();
        this.currentDepth = Depth.zero();
        this.indentation = indentation;
    }

    /**
     * Appends another ouput to this output.
     *
     * @param output
     *         the code to add
     */
    public void addLinesFrom(CodeLines output) {
        checkArgument(indentation.equals(output.indentation),
                      "Cannot merge code parts with different indentation.");
        checkArgument(currentDepth.equals(output.currentDepth),
                      "Cannot merge code parts with different depth.");
        addLines(output.codeLines);
    }

    /**
     * Appends the line of code on the current depth.
     *
     * @param codeLine
     *         the code to add
     */
    public void addLine(String codeLine) {
        checkNotNull(codeLine);
        IndentedLine line = new IndentedLine(codeLine, currentDepth);
        codeLines.add(line);
    }

    /**
     * Appends the code line and indents it to the current depth.
     *
     * @param line
     *         the line to append
     */
    public void append(CodeLine line) {
        checkNotNull(line);
        IndentedLine indented = new IndentedLine(line.content(), currentDepth);
        addLine(indented);
    }

    /**
     * Appends the line of code to the output.
     *
     * @param codeLine
     *         the code to add
     */
    public void addLine(IndentedLine codeLine) {
        checkNotNull(codeLine);
        codeLines.add(codeLine);
    }

    /**
     * Appends the line of code to the output.
     *
     * @param lines
     *         the code to add
     */
    public void addLines(List<IndentedLine> lines) {
        checkNotNull(lines);
        for (IndentedLine line : lines) {
            addLine(line);
        }
    }

    /**
     * Declares JS method and enters its body.
     *
     * @param methodName
     *         the full method name including type name
     * @param methodArgs
     *         the args to pass to the method
     */
    public void enterMethod(String methodName, String... methodArgs) {
        checkNotNull(methodName);
        checkNotNull(methodArgs);
        String argString = join(", ", methodArgs);
        addLine(methodName + " = function(" + argString + ") {");
        increaseDepth();
    }

    /**
     * Exits method declaration.
     */
    public void exitMethod() {
        decreaseDepth();
        addLine("};");
    }

    /**
     * Enters the {@code if} block body.
     *
     * @param condition
     *         the {@code if} clause
     */
    public void enterIfBlock(String condition) {
        checkNotNull(condition);
        enterBlock("if (" + condition + ')');
    }

    /**
     * Closes the current {@code if} block and enters the {@code else} block.
     */
    public void enterElseBlock() {
        decreaseDepth();
        addLine("} else {");
        increaseDepth();
    }

    /**
     * Enters block with the custom header.
     *
     * <p>For example, the custom header may be {@code for (let value in list)}.
     *
     * @param blockHeader
     *         the block header
     */
    public void enterBlock(String blockHeader) {
        checkNotNull(blockHeader);
        addLine(blockHeader + " {");
        increaseDepth();
    }

    /**
     * Exits the {@code if}, {@code else} or custom block.
     */
    public void exitBlock() {
        decreaseDepth();
        addLine("}");
    }

    /**
     * Enters the {@code if} block checking that the given value is {@code null}.
     *
     * @param value
     *         the expression to check for {@code null}
     */
    public void ifNull(String value) {
        checkNotNull(value);
        enterIfBlock(value + " === null");
    }

    /**
     * Enters the {@code if} block checking that the given value is not {@code null}.
     *
     * @param value
     *         the expression to check for not being {@code null}
     */
    public void ifNotNull(String value) {
        checkNotNull(value);
        enterIfBlock(value + " !== null");
    }

    /**
     * Enters the {@code if} block checking that the given value is not {@code undefined}.
     *
     * @param value
     *         the expression to check for not being {@code undefined}
     */
    public void ifNotUndefined(String value) {
        checkNotNull(value);
        enterIfBlock(value + " !== undefined");
    }

    /**
     * Enters the {@code if} block checking that given value is not {@code null} or
     * {@code undefined}.
     *
     * @param value
     *         the expression to check for not being {@code null} or {@code undefined}
     */
    public void ifNotNullOrUndefined(String value) {
        checkNotNull(value);
        enterIfBlock(value + " !== undefined && " + value + " !== null");
    }

    /**
     * Exports the JS map with a given name and enters its declaration body.
     *
     * @param mapName
     *         the name of the map
     */
    public void exportMap(String mapName) {
        checkNotNull(mapName);
        addLine("module.exports." + mapName + " = new Map([");
        increaseDepth();
    }

    /**
     * Adds an entry to the map.
     *
     * <p>Assumes that the cursor is currently inside the exported map declaration.
     *
     * @param entry
     *         the entry to add
     * @param isLast
     *         whether this entry is last or there will be more
     */
    public void addMapEntry(String entry, boolean isLast) {
        checkNotNull(entry);
        if (isLast) {
            addLine(entry);
        } else {
            addLine(entry + ',');
        }
    }

    /**
     * Exits the exported map declaration.
     */
    public void quitMapDeclaration() {
        decreaseDepth();
        addLine("]);");
    }

    /**
     * Manually increases the current depth.
     */
    public void increaseDepth() {
        currentDepth = currentDepth.incremented();
    }

    /**
     * Manually decreases the current depth.
     */
    public void decreaseDepth() {
        currentDepth = currentDepth.decremented();
    }

    /**
     * Concatenates all the code lines with the correct indentation and line separator.
     *
     * @return all accumulated JS code in a single {@code String}
     */
    @Override
    public String toString() {
        String result = codeLines.stream()
                                 .map(codeLine -> codeLine.indent(indentation))
                                 .collect(Collectors.joining(LINE_SEPARATOR));
        return result;
    }

    @VisibleForTesting
    int currentDepth() {
        return currentDepth.value();
    }

    @VisibleForTesting
    Indent indent() {
        return indentation;
    }
}
