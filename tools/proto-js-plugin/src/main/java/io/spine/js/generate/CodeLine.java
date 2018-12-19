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
public final class CodeLine {

    private final String content;
    private final Depth depth;

    /**
     * Creates a new {@code CodeLine} without the zero depth.
     *
     * @param content
     *         the JS code
     */
    public CodeLine(String content) {
        this(content, 0);
    }

    /**
     * Creates a new {@code CodeLine}.
     *
     * @param content
     *         the JS code
     * @param depth
     *         the depth of the code
     */
    public CodeLine(String content, int depth) {
        this(content, Depth.of(depth));
    }

    /**
     * Creates a new {@code CodeLine}.
     *
     * @param content
     *         the JS code
     * @param depth
     *         the depth of the code
     */
    public CodeLine(String content, Depth depth) {
        checkNotNull(content);
        this.content = content;
        this.depth = depth;
    }

    /**
     * Prepends the correct indent to the code line content.
     *
     * @param indentPerDepth
     *         how many spaces are inserted per depth level
     * @return the {@code CodeLine} content with the correct indent
     */
    String indent(Indent indentPerDepth) {
        Indent indent = indentPerDepth.ofDepth(depth);
        String result = indent + content;
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CodeLine)) {
            return false;
        }
        CodeLine codeLine = (CodeLine) o;
        return depth == codeLine.depth &&
                content.equals(codeLine.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, depth);
    }

    @Override
    public String toString() {
        return content;
    }
}
