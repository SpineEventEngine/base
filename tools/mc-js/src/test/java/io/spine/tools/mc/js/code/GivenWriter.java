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

final class GivenWriter {

    /** Prevents instantiation of this utility class. */
    private GivenWriter() {
    }

    static CodeWriter withSomeCodeIndentedAt(int depth) {
        CodeWriter lines = withDepth(depth);
        lines.append("{");
        lines.increaseDepth();
        lines.append("in the code block");
        lines.decreaseDepth();
        lines.append("}");
        return lines;
    }

    static CodeWriter withDepth(int depth) {
        CodeWriter writer = new CodeWriter();
        for (int i = 0; i < depth; i++) {
            writer.increaseDepth();
        }
        return writer;
    }

    /**
     * Obtains code lines with the specified first line.
     */
    static CodeWriter newCodeLines(String firstLine) {
        CodeWriter writer = new CodeWriter();
        writer.append(firstLine);
        return writer;
    }

    /**
     * Obtains code lines with the specified first line.
     */
    static CodeWriter newCodeLines(Indent indent, String firstLine) {
        CodeWriter writer = new CodeWriter(indent);
        writer.append(firstLine);
        return writer;
    }
}
