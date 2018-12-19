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

package io.spine.code;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * The depth of the code.
 *
 * <p>In other words, the depth is a number of {@linkplain Indent indents}
 * to align the code with.
 *
 * <p>The value cannot be negative.
 */
public class Depth {

    private static final Depth ZERO = of(0);

    private final int value;

    private Depth(int value) {
        checkArgument(value >= 0, "Depth cannot be negative");
        this.value = value;
    }

    /**
     * Creates depth of the specified value.
     */
    public static Depth of(int value) {
        return new Depth(value);
    }

    /**
     * Creates zero depth.
     */
    public static Depth zero() {
        return ZERO;
    }

    /**
     * Obtains the value of the depth.
     */
    public int value() {
        return value;
    }

    /**
     * Obtains the depth by incrementing this depth.
     */
    public Depth incremented() {
        return of(value + 1);
    }

    /**
     * Obtains the depth by decrementing this depth.
     */
    public Depth decremented() {
        return of(value - 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Depth)) {
            return false;
        }
        Depth depth = (Depth) o;
        return value == depth.value;
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
