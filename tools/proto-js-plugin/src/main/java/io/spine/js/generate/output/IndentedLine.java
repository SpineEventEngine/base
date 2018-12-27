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

package io.spine.js.generate.output;

import io.spine.code.generate.Indent;
import io.spine.code.generate.IndentLevel;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The JS code line.
 *
 * <p>Consists of the code itself and the depth on which the code is located.
 *
 * <p>For example, the code inside the {@code if} block is one unit deeper than the {@code if}
 * declaration itself.
 */
final class IndentedLine extends CodeLine {

    private final IndentLevel depth;
    /** The indent per a depth level. */
    private final Indent indent;
    /** The line to prepend with indentation. */
    private final CodeLine unaliagned;

    private IndentedLine(CodeLine unaligned, IndentLevel depth, Indent indent) {
        super();
        checkNotNull(unaligned);
        checkNotNull(depth);
        checkNotNull(indent);
        this.unaliagned = unaligned;
        this.depth = depth;
        this.indent = indent;
    }

    /**
     * Creates a new {@code IndentedLine}.
     *
     * @param line
     *         the line to be indented
     * @param depth
     *         the depth of the code
     * @param indent
     *         the indent per a depth level
     */
    static IndentedLine of(String line, IndentLevel depth, Indent indent) {
        CodeLine rawLine = CodeLine.of(line);
        return of(rawLine, depth, indent);
    }

    /**
     * Creates a new {@code IndentedLine}.
     *
     * @param line
     *         the code line to be indented
     * @param depth
     *         the depth of the code
     * @param indent
     *         the indent per a depth level
     */
    static IndentedLine of(CodeLine line, IndentLevel depth, Indent indent) {
        return new IndentedLine(line, depth, indent);
    }

    /**
     * Obtains the content of the line prepended with the indent.
     */
    @Override
    public String content() {
        Indent totalIndent = depth.totalIndent(indent);
        String result = totalIndent + unaliagned.content();
        return result;
    }

    /**
     * Obtains a line with the depth adjusted by the specified value.
     *
     * @param depthChange
     *         the value to adjust the depth by
     * @return a new line with increased depth
     */
    IndentedLine adjustDepthBy(int depthChange) {
        int newDepthValue = depth.value() + depthChange;
        IndentLevel newDepth = IndentLevel.of(newDepthValue);
        return new IndentedLine(unaliagned, newDepth, indent);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IndentedLine)) {
            return false;
        }
        IndentedLine codeLine = (IndentedLine) o;
        return content().equals(codeLine.content()) &&
                depth.equals(codeLine.depth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content(), depth);
    }
}
