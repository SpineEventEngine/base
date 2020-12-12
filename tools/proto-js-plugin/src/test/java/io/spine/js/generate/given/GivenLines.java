/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.js.generate.given;

import io.spine.code.gen.Indent;
import io.spine.code.gen.IndentLevel;
import io.spine.js.generate.output.CodeLines;

public final class GivenLines {

    /** Prevents instantiation of this utility class. */
    private GivenLines() {
    }

    public static CodeLines withDifferentDepth(IndentLevel initialDepth) {
        CodeLines lines = linesWithDepth(initialDepth);
        lines.append("{");
        lines.increaseDepth();
        lines.append("in the code block");
        lines.decreaseDepth();
        lines.append("}");
        return lines;
    }

    public static CodeLines linesWithDepth(IndentLevel depth) {
        CodeLines lines = new CodeLines();
        for (int i = 0; i < depth.value(); i++) {
            lines.increaseDepth();
        }
        return lines;
    }

    /**
     * Obtains code lines with the specified first line.
     */
    public static CodeLines newCodeLines(String firstLine) {
        CodeLines lines = new CodeLines();
        lines.append(firstLine);
        return lines;
    }

    /**
     * Obtains code lines with the specified first line.
     */
    public static CodeLines newCodeLines(String firstLine, Indent indent) {
        CodeLines lines = new CodeLines(indent);
        lines.append(firstLine);
        return lines;
    }
}
