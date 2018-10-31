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

package io.spine.tools.compiler.validation;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;

/**
 * Utility class for working with {@code MethodConstructor}s.
 */
final class MethodConstructors {

    /** Prevents instantiation of this utility class. */
    private MethodConstructors() {
    }

    /**
     * Returns the suffix for the `raw` methods of the `ValidatingBuilder` classes.
     *
     * @return the {@code String} which represents the raw suffix
     */
    static String rawSuffix() {
        return "Raw";
    }

    /**
     * Returns the prefix for the `clear` methods of the `ValidatingBuilder` classes.
     *
     * @return the {@code String} which represents the prefix for the `clear` methods
     */
    static String clearPrefix() {
        return "clear";
    }

    /**
     * Returns the prefix for the `remove` methods of the `ValidatingBuilder` classes.
     *
     * @return the {@code String} which represents the prefix for the `remove` methods
     */
    static String removePrefix() {
        return "remove";
    }

    /**
     * Returns the `return` statement for the methods of the `ValidatingBuilder` classes.
     *
     * @return the {@code String} which represents the `return` statement
     */
    static String returnThis() {
        return "return this";
    }

    /**
     * Creates the {@code ... .clearProperty()} statement for the given property name.
     *
     * @param propertyName
     *         the name of the property to clear
     * @return the {@code String} representing the clear call
     */
    static String clearProperty(String propertyName) {
        checkNotNull(propertyName);
        checkState(!propertyName.isEmpty());

        return format(".clear%s()", propertyName);
    }

    /**
     * Returns the getter code fragment of the predefined {@code Message.Builder}.
     *
     * @return the {@code String} which represents the pointer
     */
    static String getMessageBuilder() {
        return "getMessageBuilder()";
    }
}
