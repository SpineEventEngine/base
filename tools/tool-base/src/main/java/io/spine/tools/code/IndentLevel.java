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

import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * An indent level, which determines a number of {@linkplain Indent indents} to align code with.
 *
 * <p>The value cannot be negative.
 */
public final class IndentLevel {

    private static final IndentLevel ZERO = of(0);

    private final int value;

    private IndentLevel(int value) {
        checkArgument(value >= 0, "An indent level cannot be negative.");
        this.value = value;
    }

    /**
     * Creates an indent level with the specified value.
     */
    public static IndentLevel of(int value) {
        return new IndentLevel(value);
    }

    /**
     * Creates the indent level of zero.
     */
    public static IndentLevel zero() {
        return ZERO;
    }

    /**
     * Obtains the value of the level.
     */
    public int value() {
        return value;
    }

    /**
     * Obtains the indent level by incrementing this value.
     */
    public IndentLevel incremented() {
        return of(value + 1);
    }

    /**
     * Obtains the indent level by decrementing this value.
     */
    public IndentLevel decremented() {
        return of(value - 1);
    }

    /**
     * Obtains the total indent for the level.
     *
     * @param indentPerLevel
     *         the indent per a level
     */
    public Indent totalIndent(Indent indentPerLevel) {
        int totalSize = indentPerLevel.size() * value;
        return Indent.of(totalSize);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IndentLevel)) {
            return false;
        }
        IndentLevel level = (IndentLevel) o;
        return value == level.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
