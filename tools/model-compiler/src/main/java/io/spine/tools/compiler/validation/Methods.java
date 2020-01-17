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

package io.spine.tools.compiler.validation;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * Utility class for working with method code generation.
 */
final class Methods {

    /**
     * Prevents instantiation of this utility class.
     */
    private Methods() {
    }

    /**
     * Returns the `return` statement which always return {@code this}.
     */
    static String returnThis() {
        return returnValue("this");
    }

    /**
     * Returns the {@code return} statement for with the given return value.
     *
     * @param value
     *         the value to return
     */
    static String returnValue(String value) {
        checkNotNull(value);
        return "return " + value;
    }

    /**
     * Creates a Java statement which calls a method on an object.
     *
     * @param receiver
     *         the object on which the method is called
     * @param methodName
     *         the name of the method
     * @param parameters
     *         the method parameters
     * @return the constructed statement
     */
    static String callMethod(String receiver, String methodName, String... parameters) {
        checkNotNull(receiver);
        checkNotNull(methodName);
        checkNotNull(parameters);
        StringBuilder superMethodCall = new StringBuilder();
        superMethodCall.append(format("%s.%s(", receiver, methodName));
        String parameterList = String.join(", ", parameters);
        superMethodCall.append(parameterList)
                       .append(')');
        return superMethodCall.toString();
    }

    /**
     * Returns a call to the specified method of the parent class with the specified parameters.
     */
    static String callSuper(String methodName, String... parameters) {
        return callMethod("super", methodName, parameters);
    }

    /**
     * Returns the getter code fragment of the predefined {@code Message.Builder}.
     */
    static String getMessageBuilder() {
        return "getMessageBuilder()";
    }
}
