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

import com.google.common.base.Joiner;
import io.spine.annotation.Internal;

/**
 * Utilities for debug and error diagnostics.
 */
@Internal
public final class Diags {

    private static final char BACKTICK = '`';
    private static final Joiner COMMA_JOINER = Joiner.on(", ");

    /** Prevents instantiation of this utility class. */
    private Diags() {
    }

    /**
     * Wraps the string representation of the passed object into backticks.
     */
    public static String backtick(Object object) {
        return BACKTICK + object.toString() + BACKTICK;
    }

    /**
     * Lists the passed items separating with comma followed by a space character.
     */
    public static String join(Iterable<?> items) {
        return COMMA_JOINER.join(items);
    }

    /**
     * Lists the passed elements separating with comma followed by a space character.
     */
    @SafeVarargs
    public static <E> String join(E... elements) {
        return COMMA_JOINER.join(elements);
    }
}
