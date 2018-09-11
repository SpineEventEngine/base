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

import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static io.spine.tools.protojs.given.Generators.assertContains;
import static io.spine.tools.protojs.given.Generators.assertNotContains;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("JsGenerator should")
class JsGeneratorTest {

    private static final String LINE = "line";
    private static final String FUNCTION_NAME = "func";
    private static final String FUNCTION_ARG = "arg";
    private static final String COMMENT = "comment";
    private static final String VALUE = "someValue";
    private static final String CONDITION = VALUE + " > 0";
    private static final String CUSTOM_BLOCK = "for (let i in values)";
    private static final String MAP_NAME = "mapName";

    private JsGenerator jsGenerator;

    @BeforeEach
    void setUp() {
        jsGenerator = new JsGenerator();
    }

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester().testAllPublicInstanceMethods(jsGenerator);
    }

    @Test
    @DisplayName("return generated code")
    void returnGeneratedCode() {
        jsGenerator.addLine(LINE);
        assertEquals(LINE, generatorOutput());
    }

    @Test
    @DisplayName("support custom indent")
    void setIndent() {
        JsGenerator jsGenerator = new JsGenerator(4);
        jsGenerator.increaseDepth();
        jsGenerator.addLine(LINE);
        String expected = "    " + LINE;
        assertEquals(expected, generatorOutput(jsGenerator));
    }

    @Test
    @DisplayName("add line on the current depth")
    void addLine() {
        jsGenerator.increaseDepth();
        jsGenerator.addLine(LINE);
        JsOutput jsOutput = jsGenerator.getGeneratedCode();
        List<LineOfCode> linesOfCode = jsOutput.linesOfCode();
        LineOfCode line = linesOfCode.get(0);
        assertEquals(1, line.depth());
    }

    @Nested
    @DisplayName("allow manually")
    class AllowManually {

        @Test
        @DisplayName("increase depth")
        void increaseDepth() {
            jsGenerator.increaseDepth();
            assertEquals(1, jsGenerator.currentDepth());
        }

        @Test
        @DisplayName("decrease depth")
        void decreaseDepth() {
            jsGenerator.increaseDepth();
            jsGenerator.decreaseDepth();
            assertEquals(0, jsGenerator.currentDepth());
        }
    }

    @Test
    @DisplayName("declare function")
    void declareFunction() {
        jsGenerator.enterFunction(FUNCTION_NAME, FUNCTION_ARG);
        String declaration = FUNCTION_NAME + " = function(" + FUNCTION_ARG + ") {";
        assertContains(jsGenerator, declaration);
        assertEquals(1, jsGenerator.currentDepth());
    }

    @Test
    @DisplayName("exit function body")
    void exitFunction() {
        jsGenerator.enterFunction(FUNCTION_NAME, FUNCTION_ARG);
        jsGenerator.exitFunction();
        String functionExit = "};";
        assertContains(jsGenerator, functionExit);
        assertEquals(0, jsGenerator.currentDepth());
    }

    @Test
    @DisplayName("add comment")
    void addComment() {
        jsGenerator.addComment(COMMENT);
        String comment = "// " + COMMENT;
        assertContains(jsGenerator, comment);
    }

    @Test
    @DisplayName("compose return statement")
    void addReturn() {
        jsGenerator.returnValue(VALUE);
        String returnStatement = "return " + VALUE + ';';
        assertContains(jsGenerator, returnStatement);
    }

    @Nested
    @DisplayName("enter")
    class Enter {

        @Test
        @DisplayName("if block")
        void enterIf() {
            jsGenerator.enterIfBlock(CONDITION);
            String ifDeclaration = "if (" + CONDITION + ") {";
            assertContains(jsGenerator, ifDeclaration);
            assertEquals(1, jsGenerator.currentDepth());
        }

        @Test
        @DisplayName("else block")
        void enterElse() {
            jsGenerator.enterIfBlock(CONDITION);
            jsGenerator.enterElseBlock();
            String elseDeclaration = "} else {";
            assertContains(jsGenerator, elseDeclaration);
            assertEquals(1, jsGenerator.currentDepth());
        }

        @Test
        @DisplayName("custom block")
        void enterCustomBlock() {
            jsGenerator.enterBlock(CUSTOM_BLOCK);
            String blockDeclaration = CUSTOM_BLOCK + " {";
            assertContains(jsGenerator, blockDeclaration);
            assertEquals(1, jsGenerator.currentDepth());
        }
    }

    @Test
    @DisplayName("exit block")
    void exitBlock() {
        jsGenerator.enterBlock(CUSTOM_BLOCK);
        jsGenerator.exitBlock();
        String blockExit = "}";
        assertContains(jsGenerator, blockExit);
        assertEquals(0, jsGenerator.currentDepth());
    }

    @Test
    @DisplayName("declare map export")
    void exportMap() {
        jsGenerator.exportMap(MAP_NAME);
        String mapDeclaration = "export const " + MAP_NAME + " = new Map([";
        assertContains(jsGenerator, mapDeclaration);
        assertEquals(1, jsGenerator.currentDepth());
    }

    @Test
    @DisplayName("add entry to exported map")
    void addMapEntry() {
        jsGenerator.exportMap(MAP_NAME);

        String entry1 = "entry1";
        jsGenerator.addMapEntry(entry1, false);
        String entry2 = "entry2";
        jsGenerator.addMapEntry(entry2, true);

        String entry1WithComma = entry1 + ',';
        String entry2WithComma = entry2 + ',';
        assertContains(jsGenerator, entry1WithComma);
        assertContains(jsGenerator, entry2);
        assertNotContains(jsGenerator, entry2WithComma);
    }

    @Test
    @DisplayName("quit exported map declaration")
    void quitMapDeclaration() {
        jsGenerator.exportMap(MAP_NAME);
        jsGenerator.quitMapDeclaration();
        String mapDeclarationExit = "]);";
        assertContains(jsGenerator, mapDeclarationExit);
        assertEquals(0, jsGenerator.currentDepth());
    }

    private String generatorOutput() {
        return generatorOutput(jsGenerator);
    }

    private static String generatorOutput(JsGenerator jsGenerator) {
        JsOutput jsOutput = jsGenerator.getGeneratedCode();
        String output = jsOutput.toString();
        return output;
    }
}
