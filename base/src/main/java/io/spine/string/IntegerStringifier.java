/*
 * Copyright 2019, TeamDev. All rights reserved.
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

import io.spine.repackaged.com.google.common.primitives.Ints;

/**
 * The {@code Stringifier} for the integer values.
 */
final class IntegerStringifier extends SerializableStringifier<Integer> {

    private static final long serialVersionUID = 0L;

    private static final IntegerStringifier INSTANCE = new IntegerStringifier();

    private IntegerStringifier() {
        super("Stringifiers.forInteger()");
    }

    static IntegerStringifier getInstance() {
        return INSTANCE;
    }

    @SuppressWarnings("ConstantConditions") // The used converter never returns `null`.
    @Override
    protected String toString(Integer obj) {
        return Ints.stringConverter()
                   .reverse()
                   .convert(obj);
    }

    @SuppressWarnings("ConstantConditions") // The used converter never returns `null`.
    @Override
    protected Integer fromString(String s) {
        return Ints.stringConverter()
                   .convert(s);
    }

    private Object readResolve() {
        return INSTANCE;
    }
}
