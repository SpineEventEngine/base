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

package io.spine.string;

import io.spine.util.SerializableFunction;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Abstract base for stringifiers that convert values using function objects.
 */
public abstract class FnStringifier<T> extends SerializableStringifier<T> {

    private static final long serialVersionUID = 0L;

    private final SerializableFunction<T, String> printer;
    private final SerializableFunction<String, T> parser;

    protected FnStringifier(String identity,
                            SerializableFunction<T, String> printer,
                            SerializableFunction<String, T> parser) {
        super(identity);
        this.printer = checkNotNull(printer);
        this.parser = checkNotNull(parser);
    }


    @Override
    protected final String toString(T obj) {
        String result = printer.apply(obj);
        return result;
    }

    @Override
    protected final T fromString(String s) {
        T result = parser.apply(s);
        return result;
    }
}
