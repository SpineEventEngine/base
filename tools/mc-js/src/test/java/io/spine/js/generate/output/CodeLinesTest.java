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

package io.spine.js.generate.output;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.NullPointerTester;
import com.google.common.truth.StringSubject;
import com.google.common.truth.Truth;
import io.spine.code.gen.Indent;
import io.spine.code.gen.IndentLevel;
import io.spine.js.generate.given.GivenLines;
import io.spine.js.generate.output.snippet.Comment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.code.gen.Indent.of2;
import static io.spine.code.gen.Indent.of4;
import static io.spine.js.generate.given.Generators.assertContains;
import static io.spine.js.generate.given.GivenLines.linesWithDepth;
import static io.spine.js.generate.given.GivenLines.newCodeLines;
import static io.spine.js.generate.output.CodeLines.LINE_SEPARATOR;
import static io.spine.testing.Assertions.assertIllegalArgument;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("`CodeLines` should")
@SuppressWarnings("DuplicateStringLiteralInspection")
// Generated code duplication needed to check main class.
class CodeLinesTest {

    private static final String LINE = "line";
    private static final String METHOD_NAME = "func";
    private static final String FUNCTION_ARG = "arg";
    private static final String VALUE = "someValue";
    private static final String CONDITION = VALUE + " > 0";
    private static final String CUSTOM_BLOCK = "for (let i in values)";

    private CodeLines jsOutput;

    @BeforeEach
    void setUp() {
        jsOutput = new CodeLines();
    }

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester().testAllPublicInstanceMethods(jsOutput);
    }

    @Test
    @DisplayName("support custom indent")
    void setIndent() {
        Indent indent = Indent.of4();
        CodeLines jsOutput = new CodeLines(indent);
        jsOutput.increaseDepth();
        jsOutput.append(LINE);
        String expected = indent + LINE;
        assertEquals(expected, jsOutput.toString());
    }

    @Nested
    @DisplayName("allow manually")
    class AllowManually {

        @Test
        @DisplayName("increase depth")
        void increaseDepth() {
            jsOutput.increaseDepth();
            assertEquals(1, jsOutput.currentDepth());
        }

        @Test
        @DisplayName("decrease depth")
        void decreaseDepth() {
            jsOutput.increaseDepth();
            jsOutput.decreaseDepth();
            assertEquals(0, jsOutput.currentDepth());
        }
    }

    @Test
    @DisplayName("declare function")
    void declareFunction() {
        jsOutput.enterMethod(METHOD_NAME, FUNCTION_ARG);
        String declaration = METHOD_NAME + " = function(" + FUNCTION_ARG + ") {";
        assertContains(jsOutput, declaration);
        assertEquals(1, jsOutput.currentDepth());
    }

    @Test
    @DisplayName("exit function body")
    void exitFunction() {
        jsOutput.enterMethod(METHOD_NAME, FUNCTION_ARG);
        jsOutput.exitMethod();
        String functionExit = "};";
        assertContains(jsOutput, functionExit);
        assertEquals(0, jsOutput.currentDepth());
    }

    @Nested
    @DisplayName("enter")
    class Enter {

        @Test
        @DisplayName("if block")
        void enterIf() {
            jsOutput.enterIfBlock(CONDITION);
            String ifDeclaration = "if (" + CONDITION + ") {";
            assertContains(jsOutput, ifDeclaration);
            assertEquals(1, jsOutput.currentDepth());
        }

        @Test
        @DisplayName("else block")
        void enterElse() {
            jsOutput.enterIfBlock(CONDITION);
            jsOutput.enterElseBlock();
            String elseDeclaration = "} else {";
            assertContains(jsOutput, elseDeclaration);
            assertEquals(1, jsOutput.currentDepth());
        }

        @Test
        @DisplayName("custom block")
        void enterCustomBlock() {
            jsOutput.enterBlock(CUSTOM_BLOCK);
            String blockDeclaration = CUSTOM_BLOCK + " {";
            assertContains(jsOutput, blockDeclaration);
            assertEquals(1, jsOutput.currentDepth());
        }
    }

    @Nested
    @DisplayName("append CodeLines")
    class AppendCodeLines {

        private static final String FIRST_PART = "first part";
        private static final String SECOND_PART = "second part";

        @Test
        @DisplayName("of the same depth")
        void sameDepth() {
            CodeLines first = newCodeLines(FIRST_PART);
            CodeLines second = newCodeLines(SECOND_PART);
            first.append(second);
            String expected = FIRST_PART + LINE_SEPARATOR + SECOND_PART;
            assertThat(first).isEqualTo(expected);
        }

        @Test
        @DisplayName("only of the same indent")
        void notAllowDifferentIndents() {
            CodeLines first = newCodeLines(FIRST_PART, of2());
            CodeLines second = newCodeLines(FIRST_PART, of4());
            assertIllegalArgument(() -> first.append(second));
        }

        @Test
        @DisplayName("and increase depth")
        void increaseDepth() {
            assertMergedAndAligned(IndentLevel.of(2), IndentLevel.zero());
        }

        @Test
        @DisplayName("and decrease depth")
        void decreaseDepth() {
            assertMergedAndAligned(IndentLevel.zero(), IndentLevel.of(2));
        }

        /**
         * Asserts that two {@link io.spine.js.generate.output.CodeLines} are merged
         * and the depth of the appended lines is adjusted.
         *
         * @param firthDepth
         *         the depth of lines to append to
         * @param secondDepth
         *         the depth of the appended lines
         */
        private void assertMergedAndAligned(IndentLevel firthDepth, IndentLevel secondDepth) {
            CodeLines first = linesWithDepth(firthDepth);
            CodeLines second = GivenLines.withDifferentDepth(secondDepth);
            first.append(second);
            CodeLines expected = GivenLines.withDifferentDepth(firthDepth);
            assertEquals(expected, first);
        }
    }

    @Test
    @DisplayName("append an unaligned line")
    void appendUnalignedLine() {
        CodeLines lines = new CodeLines();
        lines.increaseDepth();
        CodeLine comment = Comment.of("The field...");
        lines.append(comment);
        String expected = lines.indent() + comment.content();
        assertThat(lines).isEqualTo(expected);
    }

    @Test
    @DisplayName("append an indented line")
    void appendIndentedLine() {
        CodeLines lines = new CodeLines();
        IndentLevel lineDepth = IndentLevel.of(5);
        Indent indent = lines.indent();
        IndentedLine indentedLine = IndentedLine.of("some code line", lineDepth, indent);
        lines.appendIndented(indentedLine);
        assertThat(lines).isEqualTo(indentedLine.toString());
    }

    @Test
    @DisplayName("exit block")
    void exitBlock() {
        jsOutput.enterBlock(CUSTOM_BLOCK);
        jsOutput.exitBlock();
        String blockExit = "}";
        assertContains(jsOutput, blockExit);
        assertEquals(0, jsOutput.currentDepth());
    }

    @Test
    @DisplayName("join lines using comma")
    void joinLinesWithComma() {
        CodeLine first = CodeLine.of("entry1");
        CodeLine second = CodeLine.of("entry2");
        CodeLine last = CodeLine.of("entry3");
        List<CodeLine> lines = ImmutableList.of(first, second, last);
        CodeLines code = CodeLines.commaSeparated(lines);
        assertThat(code).contains(first + ",");
        assertThat(code).contains(second + ",");
        assertThat(code).contains(last.content());
        assertThat(code).doesNotContain(last + ",");
    }

    @Test
    @DisplayName("concatenate all lines of code with correct indent in `toString`")
    void provideToString() {
        CodeLines jsOutput = newCodeLines("line 1");
        jsOutput.increaseDepth();
        jsOutput.append("line 2");
        String output = jsOutput.toString();
        String expected = "line 1" + LINE_SEPARATOR + "  line 2";
        assertEquals(expected, output);
    }

    private static StringSubject assertThat(CodeLines lines) {
        return Truth.assertThat(lines.toString());
    }
}
