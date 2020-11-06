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

package io.spine.testing;


import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.spine.type.UnknownTypeException;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Convenience assertions accompanying {@link org.junit.jupiter.api.Assertions},
 * {@link com.google.common.truth.Truth}, and {@link com.google.common.truth.Truth8}.
 */
public final class Assertions {

    /** Prevents instantiation of this utility class. */
    private Assertions() {
    }

    /**
     * Asserts that running the passed executable causes {@link IllegalArgumentException}.
     */
    @CanIgnoreReturnValue
    public static IllegalArgumentException assertIllegalArgument(Executable e) {
        return assertThrows(IllegalArgumentException.class, e);
    }

    /**
     * Asserts that running the passed executable causes {@link IllegalStateException}.
     */
    @CanIgnoreReturnValue
    public static IllegalStateException assertIllegalState(Executable e) {
        return assertThrows(IllegalStateException.class, e);
    }

    /**
     * Asserts that running the passed executable causes {@link UnknownTypeException}.
     */
    @CanIgnoreReturnValue
    public static UnknownTypeException assertUnknownType(Executable e) {
        return assertThrows(UnknownTypeException.class, e);
    }

    /**
     * Asserts that running the passed executable cases {@link NullPointerException}.
     */
    @CanIgnoreReturnValue
    public static NullPointerException assertNpe(Executable e) {
        return assertThrows(NullPointerException.class, e);
    }
}
