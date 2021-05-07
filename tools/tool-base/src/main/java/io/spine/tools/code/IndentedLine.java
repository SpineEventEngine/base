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

package io.spine.tools.code;

import com.google.errorprone.annotations.Immutable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The JavaScript code line.
 *
 * <p>Consists of the code itself and the level on which the code is indented.
 *
 * <p>For example, the code inside the {@code if} block is one unit deeper than the {@code if}
 * declaration itself.
 */
@Immutable
public final class IndentedLine extends CodeLine {

    /** The level of indentation before the code. */
    private final Indent indent;

    /** The line of code to be executed. */
    private final CodeLine code;

    private IndentedLine(Indent indent, CodeLine code) {
        super();
        this.code = checkNotNull(code);
        this.indent = checkNotNull(indent);
    }

    /**
     * Creates a new {@code IndentedLine}.
     *
     * @param indent
     *         the indent per a level
     * @param code
 *             the source code text
     */
    public static IndentedLine of(Indent indent, String code) {
        CodeLine pure = CodeLine.of(code);
        return of(indent, pure);
    }

    /**
     * Creates a new {@code IndentedLine}.
     *
     * @param indent
     *         the indent before the code
     * @param code
 *         the code to be added
     */
    private static IndentedLine of(Indent indent, CodeLine code) {
        return new IndentedLine(indent, code);
    }

    /**
     * Obtains the content of the line prepended with the indent.
     */
    @Override
    public String content() {
        String result = indent + code.content();
        return result;
    }

    /**
     * Obtains a line with the indent level adjusted by the specified value.
     *
     * <p>The passed value of shift can be negative, but the resulting indentation
     * should be still greater or equal to zero.
     *
     * <p>If zero shift passed, this line is returned.
     *
     * @param shift
     *         the offset for levels of indentation
     *
     * @return a new line with the adjusted indent level
     */
    public IndentedLine adjustLevelBy(int shift) {
        if (shift == 0) {
            return this;
        }
        Indent newIndent = indent.shifted(shift);
        return new IndentedLine(newIndent, code);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IndentedLine)) {
            return false;
        }
        IndentedLine other = (IndentedLine) o;
        boolean codeEquals = code.equals(other.code);
        boolean indentEquals = indent.equals(other.indent);
        return codeEquals && indentEquals;
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }
}
