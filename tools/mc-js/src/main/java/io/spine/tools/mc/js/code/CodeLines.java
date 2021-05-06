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

package io.spine.tools.mc.js.code;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import io.spine.tools.code.Indent;
import io.spine.tools.code.IndentLevel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.String.join;
import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;

/**
 * The aggregator of the JavaScript output.
 *
 * <p>This class serves as the JS code storage as well as provides the convenience methods for
 * adding the code.
 *
 * <p>The writable representation of the output may be obtained through the {@link #toString()}
 * method.
 */
@SuppressWarnings({
        // The generated code duplicates the code used in test that checks it.
        "DuplicateStringLiteralInspection",
        "ClassWithTooManyMethods"
})
public final class CodeLines {

    @VisibleForTesting
    static final String LINE_SEPARATOR = lineSeparator();

    private static final Indent STANDARD_INDENTATION = Indent.of2();

    /**
     * The indentation of the code, i.e. how many spaces each indent level takes.
     */
    private final Indent indentation;

    /**
     * The aggregator of the JS code.
     */
    private final List<IndentedLine> codeLines;

    /**
     * The current indent level of the code on which the next line will be added.
     */
    private IndentLevel currentLevel;

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
        this.currentLevel = IndentLevel.zero();
        this.indentation = indentation;
    }

    /**
     * Appends another code lines to this code.
     *
     * <p>If the appended lines have different indent level, then the level
     * of appended lines is adjusted to match the level of the current lines.
     *
     * <p>The indent level is adjusted by the difference of the levels.
     *
     * @param appended
     *         the code to append
     */
    public void append(CodeLines appended) {
        checkArgument(indentation.equals(appended.indentation),
                      "Cannot merge code parts with different indentation.");
        int levelDifference = currentLevel.value() - appended.currentLevel.value();
        for (IndentedLine line : appended.codeLines) {
            IndentedLine adjusted = line.adjustLevelBy(levelDifference);
            appendIndented(adjusted);
        }
    }

    /**
     * Appends the lines of the snippet.
     */
    public void append(Snippet snippet) {
        append(snippet.value());
    }

    /**
     * Appends the line of code on the current indent level.
     *
     * @param codeLine
     *         the code to add
     */
    public void append(String codeLine) {
        checkNotNull(codeLine);
        IndentedLine indented = IndentedLine.of(codeLine, currentLevel, indentation);
        codeLines.add(indented);
    }

    /**
     * Appends the code line and indents it to the current indent level.
     *
     * @param line
     *         the line to append
     */
    public void append(CodeLine line) {
        checkNotNull(line);
        IndentedLine indented = IndentedLine.of(line, currentLevel, indentation);
        appendIndented(indented);
    }

    /**
     * Appends the code line without changing its indent level.
     *
     * @param codeLine
     *         the code to add
     */
    @VisibleForTesting // otherwise private
    void appendIndented(IndentedLine codeLine) {
        checkNotNull(codeLine);
        codeLines.add(codeLine);
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
        append(methodName + " = function(" + argString + ") {");
        increaseDepth();
    }

    /**
     * Exits method declaration.
     */
    public void exitMethod() {
        decreaseDepth();
        append("};");
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
        append("} else {");
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
        append(blockHeader + " {");
        increaseDepth();
    }

    /**
     * Exits the {@code if}, {@code else} or custom block.
     */
    public void exitBlock() {
        decreaseDepth();
        append("}");
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
        enterIfBlock(notNull(value));
    }

    /**
     * Enters the {@code if} block checking that the given value is not {@code undefined}.
     *
     * @param value
     *         the expression to check for not being {@code undefined}
     */
    public void ifNotUndefined(String value) {
        checkNotNull(value);
        enterIfBlock(notUndefined(value));
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
        enterIfBlock(notUndefined(value) + " && " + notNull(value));
    }

    private static String notNull(String value) {
        return value + " !== null";
    }

    private static String notUndefined(String value) {
        return value + " !== undefined";
    }

    /**
     * Manually increases the current indent level.
     */
    public void increaseDepth() {
        currentLevel = currentLevel.incremented();
    }

    /**
     * Manually decreases the current indent level.
     */
    public void decreaseDepth() {
        currentLevel = currentLevel.decremented();
    }

    /**
     * Merges lines by addition of a comma to each line except the last one.
     */
    public static CodeLines commaSeparated(List<CodeLine> lines) {
        checkNotNull(lines);
        CodeLines code = new CodeLines();
        for (Iterator<CodeLine> it = lines.iterator(); it.hasNext(); ) {
            CodeLine line = it.next();
            boolean isLast = !it.hasNext();
            String editedLine = isLast
                                ? line.content()
                                : line.content() + ',';
            code.append(editedLine);
        }
        return code;
    }

    /**
     * Concatenates all the code lines with the correct indentation and line separator.
     *
     * @return all accumulated JS code in a single {@code String}
     */
    @Override
    public String toString() {
        String result = codeLines.stream()
                                 .map(CodeLine::toString)
                                 .collect(joining(LINE_SEPARATOR));
        return result;
    }

    /**
     * Obtains these lines with {@link #LINE_SEPARATOR} at the end of each line.
     */
    public ImmutableList<String> separated() {
        ImmutableList<String> result =
                codeLines.stream()
                         .map(l -> l + LINE_SEPARATOR)
                         .collect(toImmutableList());
        return result;
    }

    @VisibleForTesting
    int currentDepth() {
        return currentLevel.value();
    }

    @VisibleForTesting
    Indent indent() {
        return indentation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CodeLines)) {
            return false;
        }
        CodeLines lines = (CodeLines) o;
        return indentation.equals(lines.indentation) &&
                codeLines.equals(lines.codeLines);
    }

    @Override
    public int hashCode() {
        return Objects.hash(indentation, codeLines);
    }
}
