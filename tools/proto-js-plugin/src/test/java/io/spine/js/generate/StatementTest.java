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

import io.spine.code.Depth;
import io.spine.code.Indent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Statement should")
class StatementTest {

    @Test
    @DisplayName("provide a comment")
    void comment() {
        String value = "A comment text";
        Statement comment = Statement.comment(value);
        assertEquals("// " + value, comment.value());
    }

    @Test
    @DisplayName("provide a map entry with string literal key")
    void mapEntry() {
        String key = "k";
        String value = "v";
        Statement mapEntry = Statement.mapEntry(key, value);
        assertEquals("['k', v]", mapEntry.value());
    }

    @Test
    @DisplayName("convert to a code line")
    void toCodeLine() {
        Depth depth = Depth.of(1);
        Indent indent = Indent.of2();
        Statement statement = Statement.of("callMethod();");
        IndentedLine codeLine = statement.toLine(depth);
        assertEquals(indent + statement.value(), codeLine.indent(indent));
    }
}
