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

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * A line of a Javascript code.
 *
 * <p>The line is not aware of {@linkplain io.spine.js.generate.output.IndentedLine indentation}.
 */
public abstract class CodeLine {

    /**
     * Obtains the value of the line.
     */
    public abstract String content();

    /**
     * Obtains a code line with the specified content.
     */
    public static CodeLine of(String content) {
        return new CodeLine() {
            @Override
            public String content() {
                return content;
            }
        };
    }

    /**
     * Obtains an empty code line.
     */
    public static CodeLine emptyLine() {
        return of("");
    }

    /**
     * Obtains a map entry with the string literal key.
     */
    public static CodeLine mapEntry(String key, Object value) {
        checkNotNull(key);
        checkNotNull(value);
        String raw = format("['%s', %s]", key, value);
        return of(raw);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CodeLine)) {
            return false;
        }
        CodeLine line = (CodeLine) o;
        return content().equals(line.content());
    }

    @Override
    public int hashCode() {
        return Objects.hash(content());
    }

    @Override
    public String toString() {
        return content();
    }
}
