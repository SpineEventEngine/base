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

package io.spine.util;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.protobuf.Message;
import io.spine.protobuf.Messages;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static io.spine.protobuf.Messages.isNotDefault;
import static io.spine.util.Exceptions.newIllegalArgumentException;

/**
 * Utilities for checking preconditions.
 */
public final class Preconditions2 {

    /** Prevents instantiation of this utility class. */
    private Preconditions2() {
    }

    /**
     * Ensures that the passed string is not {@code null}, empty or blank string.
     *
     * @param str
     *         the string to check
     * @return the passed string
     * @throws IllegalArgumentException
     *         if the string is empty or blank
     * @throws NullPointerException
     *         if the passed string is {@code null}
     */
    @CanIgnoreReturnValue
    public static String checkNotEmptyOrBlank(String str) {
        return checkNotEmptyOrBlank(
                str, "Non-empty and non-blank string expected. Encountered: \"%s\".", str
        );
    }

    /**
     * Ensures that the passed string is not {@code null}, empty or blank string.
     *
     * @param str
     *         the string to check
     * @param errorMessageTemplate
     *         the exception message template to use if the check fails
     * @param errorMessageArgs
     *         the arguments to be substituted into the message template
     * @return the passed string
     * @throws IllegalArgumentException
     *         if the string is empty or blank
     * @throws NullPointerException
     *         if the passed string is {@code null}
     */
    @CanIgnoreReturnValue
    public static String checkNotEmptyOrBlank(String str,
                                              @Nullable String errorMessageTemplate,
                                              @Nullable Object @Nullable ... errorMessageArgs) {
        checkNotNull(str, errorMessageTemplate);
        checkArgument(!str.trim().isEmpty(), errorMessageTemplate, errorMessageArgs);
        return str;
    }

    /**
     * Ensures that the passed value is positive.
     *
     * @param value the value to check
     * @throws IllegalArgumentException if the value is negative or zero
     */
    public static long checkPositive(long value) {
        if (value <= 0) {
            throw newIllegalArgumentException("A positive value expected. Encountered: %d.", value);
        }
        return value;
    }

    /**
     * Ensures that the passed value is positive.
     *
     * @param value
     *         the value to check
     * @param errorMessageTemplate
     *         the exception message template to use if the check fails
     * @param errorMessageArgs
     *         the arguments to be substituted into the message template
     * @throws IllegalArgumentException
     *         if the value is negative or zero
     */
    public static long checkPositive(long value,
                                     @Nullable String errorMessageTemplate,
                                     @Nullable Object @Nullable ... errorMessageArgs) {
        checkArgument(value > 0L, errorMessageTemplate, errorMessageArgs);
        return value;
    }

    /**
     * Ensures that the passed message is not in the default state.
     *
     * @param message
     *         the message to check
     * @param <T>
     *         the type of the message
     * @return the passed message
     * @throws IllegalArgumentException
     *          if the passed message has the default state
     * @throws NullPointerException
     *          if the passed message is {@code null}
     */
    @CanIgnoreReturnValue
    public static <T extends @NonNull Message> T checkNotDefaultArg(T message) {
        checkArgument(!Messages.isDefault(message));
        return message;
    }

    /**
     * Ensures that the passed message is not in the default state.
     *
     * @param message
     *         the message to check
     * @param <T>
     *         the type of the message
     * @return the passed message
     * @throws IllegalStateException
     *          if the passed message has the default state
     * @throws NullPointerException
     *          if the passed message is {@code null}
     */
    @CanIgnoreReturnValue
    public static <T extends @NonNull Message> T checkNotDefaultState(T message) {
        checkState(!Messages.isDefault(message));
        return message;
    }

    /**
     * Ensures that the passed object is not in its default state and is not {@code null}.
     *
     * @param object
     *         the {@code Message} instance to check
     * @param errorMessage
     *         the message for the exception to be thrown;
     *         will be converted to a string using {@link String#valueOf(Object)}
     * @throws IllegalStateException
     *         if the object is in its default state
     * @throws NullPointerException
     *         if the passed message is {@code null}
     */
    @CanIgnoreReturnValue
    public static <M extends Message>
    M checkNotDefaultArg(M object, @Nullable Object errorMessage) {
        checkNotNull(object);
        checkArgument(isNotDefault(object), errorMessage);
        return object;
    }

    /**
     * Ensures that the passed object is not in its default state and is not {@code null}.
     *
     * @param object
     *         the {@code Message} instance to check
     * @param errorMessage
     *         the message for the exception to be thrown;
     *         will be converted to a string using {@link String#valueOf(Object)}
     * @throws IllegalStateException
     *         if the object is in its default state
     * @throws NullPointerException
     *         if the passed message is {@code null}
     */
    @CanIgnoreReturnValue
    public static <M extends Message>
    M checkNotDefaultState(M object, @Nullable Object errorMessage) {
        checkNotNull(object);
        checkState(isNotDefault(object), errorMessage);
        return object;
    }

    /**
     * Ensures that the passed object is not in its default state and is not {@code null}.
     *
     * @param object
     *         the {@code Message} instance to check
     * @param errorMessageTemplate
     *         a template for the exception message should the check fail
     * @param errorMessageArgs
     *         the arguments to be substituted into the message template
     * @throws IllegalArgumentException
     *         if the object is in its default state
     * @throws NullPointerException
     *          if the passed message is {@code null}
     */
    @CanIgnoreReturnValue
    @SuppressWarnings("OverloadedVarargsMethod")
    public static <M extends Message>
    M checkNotDefaultArg(M object,
                         @Nullable String errorMessageTemplate,
                         @Nullable Object @Nullable ... errorMessageArgs) {
        checkNotNull(object);
        checkArgument(isNotDefault(object), errorMessageTemplate, errorMessageArgs);
        return object;
    }

    /**
     * Ensures that the passed object is not in its default state and is not {@code null}.
     *
     * @param object
     *         the {@code Message} instance to check
     * @param errorMessageTemplate
     *         a template for the exception message should the check fail
     * @param errorMessageArgs
     *         the arguments to be substituted into the message template
     * @throws IllegalStateException
     *         if the object is in its default state
     * @throws NullPointerException
     *          if the passed message is {@code null}
     */
    @CanIgnoreReturnValue
    @SuppressWarnings("OverloadedVarargsMethod")
    public static <M extends Message>
    M checkNotDefaultState(M object,
                           @Nullable String errorMessageTemplate,
                           @Nullable Object @Nullable ... errorMessageArgs) {
        checkNotNull(object);
        boolean value = isNotDefault(object);
        checkState(value, errorMessageTemplate, errorMessageArgs);
        return object;
    }

    /**
     * Ensures that the passed value is within the specified range.
     *
     * <p>Both ends of the range are inclusive.
     *
     * @param value
     *         the value to check
     * @param paramName
     *         the name of the parameter which is included into the error messages
     *         if the check fails
     * @param lowBound
     *         the lower bound to check
     * @param highBound
     *         the higher bound
     */
    public static void checkBounds(int value, String paramName, int lowBound, int highBound) {
        checkNotNull(paramName);
        if (!isBetween(value, lowBound, highBound)) {
            throw newIllegalArgumentException(
                    "`%s` (value: %d) must be in bounds [%d, %d] inclusive.",
                    paramName, value, lowBound, highBound);
        }
    }

    private static boolean isBetween(int value, int lowBound, int highBound) {
        return lowBound <= value && value <= highBound;
    }
}
