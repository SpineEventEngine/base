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
import io.spine.value.StringTypeValue;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * A single-line statement.
 */
public class Statement extends StringTypeValue {

    private static final long serialVersionUID = 0L;

    private Statement(String value) {
        super(value);
    }

    public static Statement of(String value) {
        return new Statement(value);
    }

    /**
     * Obtains the comment from the specified text.
     */
    public static Statement comment(String commentText) {
        checkNotNull(commentText);
        return new Statement("// " + commentText);
    }

    public static Statement mapEntry(String key, Object value) {
        checkNotNull(key);
        checkNotNull(value);
        String raw = format("['%s', %s]", key, value);
        return new Statement(raw);
    }

    /**
     * Converts the statement to a line with the specified depth.
     */
    public IndentedLine toLine(Depth depth) {
        return new IndentedLine(value(), depth.value());
    }
}
