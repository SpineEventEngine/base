/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.js.generate.output.snippet;

import io.spine.js.generate.output.CodeLine;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * A code line representing a {@code return} statement.
 */
public class Return extends CodeLine {

    /**
     * The value to be returned.
     */
    private final Object value;

    private Return(Object returnedValue) {
        super();
        this.value = returnedValue;
    }

    /**
     * Composes a statement returning the value.
     */
    public static Return value(Object value) {
        checkNotNull(value);
        return new Return(value);
    }

    /**
     * Composes a statement returning a string literal.
     */
    public static Return stringLiteral(String literal) {
        checkNotNull(literal);
        String quoted = format("'%s'", literal);
        return new Return(quoted);
    }

    /**
     * Composes a statement returning {@code null}.
     */
    public static Return nullReference() {
        return value("null");
    }

    @Override
    public String content() {
        String result = format("return %s;", value);
        return result;
    }
}
