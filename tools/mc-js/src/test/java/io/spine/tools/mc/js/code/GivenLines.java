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

import io.spine.tools.code.Indent;

final class GivenLines {

    /** Prevents instantiation of this utility class. */
    private GivenLines() {
    }

    static CodeWriter withDifferentDepth(int initialDepth) {
        CodeWriter lines = linesWithDepth(initialDepth);
        lines.append("{");
        lines.increaseDepth();
        lines.append("in the code block");
        lines.decreaseDepth();
        lines.append("}");
        return lines;
    }

    static CodeWriter linesWithDepth(int depth) {
        CodeWriter lines = new CodeWriter();
        for (int i = 0; i < depth; i++) {
            lines.increaseDepth();
        }
        return lines;
    }

    /**
     * Obtains code lines with the specified first line.
     */
    public static CodeWriter newCodeLines(String firstLine) {
        CodeWriter lines = new CodeWriter();
        lines.append(firstLine);
        return lines;
    }

    /**
     * Obtains code lines with the specified first line.
     */
    public static CodeWriter newCodeLines(String firstLine, Indent indent) {
        CodeWriter lines = new CodeWriter(indent);
        lines.append(firstLine);
        return lines;
    }
}
