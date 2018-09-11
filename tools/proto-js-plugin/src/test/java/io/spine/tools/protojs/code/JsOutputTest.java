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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("JsOutput should")
class JsOutputTest {

    @Test
    @DisplayName("add line on specified depth level")
    void addLine() {
        JsOutput jsOutput = new JsOutput();
        int depth = 2;
        jsOutput.addLine("line of code", depth);
        LineOfCode addedLine = jsOutput.linesOfCode()
                                       .get(0);
        assertEquals(depth, addedLine.depth());
    }

    @Test
    @DisplayName("concatenate all lines of code with correct indent in `toString`")
    void provideToString() {
        JsOutput jsOutput = new JsOutput();
        jsOutput.addLine("line 1", 1);
        jsOutput.addLine("line 2", 2);
        String output = jsOutput.toString();
        String expected = "  line 1" + jsOutput.lineSeparator() + "    line 2";
        assertEquals(expected, output);
    }
}
