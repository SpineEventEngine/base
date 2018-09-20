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

package io.spine.generate;

import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.generate.JsOutput.LINE_SEPARATOR;
import static io.spine.generate.given.Generators.assertContains;
import static io.spine.generate.given.Generators.assertNotContains;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Dmytro Kuzmin
 */
@SuppressWarnings("DuplicateStringLiteralInspection")
// Generated code duplication needed to check main class.
@DisplayName("JsOutput should")
class JsOutputTest {

    private static final String LINE = "line";
    private static final String METHOD_NAME = "func";
    private static final String FUNCTION_ARG = "arg";
    private static final String COMMENT = "comment";
    private static final String VALUE = "someValue";
    private static final String CONDITION = VALUE + " > 0";
    private static final String CUSTOM_BLOCK = "for (let i in values)";
    private static final String MAP_NAME = "mapName";

    private JsOutput jsOutput;

    @BeforeEach
    void setUp() {
        jsOutput = new JsOutput();
    }

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester().testAllPublicInstanceMethods(jsOutput);
    }

    @Test
    @DisplayName("support custom indent")
    void setIndent() {
        JsOutput jsOutput = new JsOutput(4);
        jsOutput.increaseDepth();
        jsOutput.addLine(LINE);
        String expected = "    " + LINE;
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
    @DisplayName("declare variable")
    void declareVariable() {
        jsOutput.declareVariable("someVariable", "someValue");
        String variableDeclaration = "let someVariable = someValue;";
        assertContains(jsOutput, variableDeclaration);
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
        jsOutput.exitFunction();
        String functionExit = "};";
        assertContains(jsOutput, functionExit);
        assertEquals(0, jsOutput.currentDepth());
    }

    @Test
    @DisplayName("add comment")
    void addComment() {
        jsOutput.addComment(COMMENT);
        String comment = "// " + COMMENT;
        assertContains(jsOutput, comment);
    }

    @Test
    @DisplayName("compose return statement")
    void addReturn() {
        jsOutput.returnValue(VALUE);
        String returnStatement = "return " + VALUE + ';';
        assertContains(jsOutput, returnStatement);
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
    @DisplayName("declare map export")
    void exportMap() {
        jsOutput.exportMap(MAP_NAME);
        String mapDeclaration = "export const " + MAP_NAME + " = new Map([";
        assertContains(jsOutput, mapDeclaration);
        assertEquals(1, jsOutput.currentDepth());
    }

    @Test
    @DisplayName("add entry to exported map")
    void addMapEntry() {
        jsOutput.exportMap(MAP_NAME);

        String entry1 = "entry1";
        jsOutput.addMapEntry(entry1, false);
        String entry2 = "entry2";
        jsOutput.addMapEntry(entry2, true);

        String entry1WithComma = entry1 + ',';
        String entry2WithComma = entry2 + ',';
        assertContains(jsOutput, entry1WithComma);
        assertContains(jsOutput, entry2);
        assertNotContains(jsOutput, entry2WithComma);
    }

    @Test
    @DisplayName("quit exported map declaration")
    void quitMapDeclaration() {
        jsOutput.exportMap(MAP_NAME);
        jsOutput.quitMapDeclaration();
        String mapDeclarationExit = "]);";
        assertContains(jsOutput, mapDeclarationExit);
        assertEquals(0, jsOutput.currentDepth());
    }

    @Test
    @DisplayName("concatenate all lines of code with correct indent in `toString`")
    void provideToString() {
        JsOutput jsOutput = new JsOutput();
        jsOutput.addLine("line 1");
        jsOutput.increaseDepth();
        jsOutput.addLine("line 2");
        String output = jsOutput.toString();
        String expected = "line 1" + LINE_SEPARATOR + "  line 2";
        assertEquals(expected, output);
    }
}
