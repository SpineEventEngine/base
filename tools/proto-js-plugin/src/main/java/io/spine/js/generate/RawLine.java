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

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * A line of a JavaScript code represented by a string.
 */
public class RawLine extends CodeLine {

    private final String content;

    private RawLine(String content) {
        super();
        this.content = content;
    }

    public static RawLine of(String content) {
        checkNotNull(content);
        return new RawLine(content);
    }

    @Override
    public String content() {
        return content;
    }

    /**
     * Obtains the comment from the specified text.
     */
    public static CodeLine comment(String commentText) {
        checkNotNull(commentText);
        String raw = "// " + commentText;
        return of(raw);
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

    /**
     * Obtains an empty line.
     */
    public static CodeLine emptyLine() {
        return of("");
    }
}
