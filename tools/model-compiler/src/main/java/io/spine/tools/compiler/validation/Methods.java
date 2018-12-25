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

import java.util.Arrays;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;

/**
 * Utility class for working with method code generation.
 */
final class Methods {

    /** Prevents instantiation of this utility class. */
    private Methods() {
    }

    /** Returns the suffix for the `raw` methods of the `ValidatingBuilder` classes. */
    static String rawSuffix() {
        return "Raw";
    }

    /** Returns the prefix for the `clear` methods of the `ValidatingBuilder` classes. */
    static String clearPrefix() {
        return "clear";
    }

    /** Returns the prefix for the `remove` methods of the `ValidatingBuilder` classes. */
    static String removePrefix() {
        return "remove";
    }

    /** Returns the `return` statement for the methods of the `ValidatingBuilder` classes.*/
    @SuppressWarnings("DuplicateStringLiteralInspection") // different semantics of gen'ed code.
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

    /** Returns a call to the specified method of the parent class with the specified parameters. */
    static String callToParentMethod(String methodName, String... parameters) {
        StringBuilder superMethodCall = new StringBuilder();
        superMethodCall.append(format("super.%s(", methodName));
        Arrays.stream(parameters)
              .limit(parameters.length - 1)
              .forEach(parameter -> superMethodCall.append(parameter)
                                                   .append(", "));
        superMethodCall.append(parameters[parameters.length - 1])
                       .append(')');
        return superMethodCall.toString();
    }

    /** Returns the getter code fragment of the predefined {@code Message.Builder}. */
    static String getMessageBuilder() {
        return "getMessageBuilder()";
    }
}
