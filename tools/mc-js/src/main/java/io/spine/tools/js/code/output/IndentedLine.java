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

package io.spine.tools.js.code.output;

import io.spine.tools.code.Indent;
import io.spine.tools.code.IndentLevel;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The JS code line.
 *
 * <p>Consists of the code itself and the level on which the code is indented.
 *
 * <p>For example, the code inside the {@code if} block is one unit deeper than the {@code if}
 * declaration itself.
 */
final class IndentedLine extends CodeLine {

    private final IndentLevel indentLevel;
    /** The indent per a level. */
    private final Indent indent;
    /** The line to prepend with indentation. */
    private final CodeLine unaliagned;

    private IndentedLine(CodeLine unaligned, IndentLevel indentLevel, Indent indent) {
        super();
        checkNotNull(unaligned);
        checkNotNull(indentLevel);
        checkNotNull(indent);
        this.unaliagned = unaligned;
        this.indentLevel = indentLevel;
        this.indent = indent;
    }

    /**
     * Creates a new {@code IndentedLine}.
     *
     * @param line
     *         the line to be indented
     * @param level
     *         the indent level of the code
     * @param indent
     *         the indent per a level
     */
    static IndentedLine of(String line, IndentLevel level, Indent indent) {
        CodeLine rawLine = CodeLine.of(line);
        return of(rawLine, level, indent);
    }

    /**
     * Creates a new {@code IndentedLine}.
     *
     * @param line
     *         the code line to be indented
     * @param indentLevel
     *         the level of the line indent
     * @param indent
     *         the indent per a level
     */
    static IndentedLine of(CodeLine line, IndentLevel indentLevel, Indent indent) {
        return new IndentedLine(line, indentLevel, indent);
    }

    /**
     * Obtains the content of the line prepended with the indent.
     */
    @Override
    public String content() {
        Indent totalIndent = indentLevel.totalIndent(indent);
        String result = totalIndent + unaliagned.content();
        return result;
    }

    /**
     * Obtains a line with the indent level adjusted by the specified value.
     *
     * @param levelChange
     *         the value to adjust the indent level by
     * @return a new line with the adjusted indent level
     */
    IndentedLine adjustLevelBy(int levelChange) {
        int newLevelValue = indentLevel.value() + levelChange;
        IndentLevel newLevel = IndentLevel.of(newLevelValue);
        return new IndentedLine(unaliagned, newLevel, indent);
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
                indentLevel.equals(codeLine.indentLevel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content(), indentLevel);
    }
}
