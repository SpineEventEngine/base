/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.testing;

import com.google.common.annotations.VisibleForTesting;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.FieldMask;
import com.google.protobuf.Message;
import io.spine.type.UnknownTypeException;
import org.junit.jupiter.api.function.Executable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.Testing.callConstructor;
import static java.lang.Math.abs;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        checkNotNull(e);
        return assertThrows(IllegalArgumentException.class, e);
    }

    /**
     * Asserts that running the passed executable causes {@link IllegalStateException}.
     */
    @CanIgnoreReturnValue
    public static IllegalStateException assertIllegalState(Executable e) {
        checkNotNull(e);
        return assertThrows(IllegalStateException.class, e);
    }

    /**
     * Asserts that running the passed executable causes {@link UnknownTypeException}.
     */
    @CanIgnoreReturnValue
    public static UnknownTypeException assertUnknownType(Executable e) {
        checkNotNull(e);
        return assertThrows(UnknownTypeException.class, e);
    }

    /**
     * Asserts that running the passed executable cases {@link NullPointerException}.
     */
    @CanIgnoreReturnValue
    public static NullPointerException assertNpe(Executable e) {
        checkNotNull(e);
        return assertThrows(NullPointerException.class, e);
    }

    /**
     * Asserts that a condition is true. If it isn't, it throws an
     * {@link AssertionError} without a message.
     *
     * <p>This method is needed to avoid dependency on JUnit 4.x in projects that use
     * Spine and JUnit5.
     */
    @VisibleForTesting
    static void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError();
        }
    }

    /**
     * Asserts that if the passed class has private parameter-less constructor and invokes it
     * using Reflection.
     *
     * <p>Typically this method is used to add a constructor of a utility class into
     * the covered code.
     *
     * <p>Example:
     * <pre>
     * public class MyUtilityShould
     *     ...
     *    {@literal @}Test
     *     public void haveUtilityCtor() {
     *         assertHasPrivateParameterlessCtor(MyUtility.class));
     *     }
     * </pre>
     * @see UtilityClassTest
     */
    public static void assertHasPrivateParameterlessCtor(Class<?> targetClass) {
        checkNotNull(targetClass);
        assertTrue(hasPrivateParameterlessCtor(targetClass));
    }

    /**
     * Verifies if the passed class has private parameter-less constructor and invokes it
     * using Reflection.
     *
     * @return {@code true} if the class has private parameter-less constructor,
     *         {@code false} otherwise
     */
    @VisibleForTesting
    static boolean hasPrivateParameterlessCtor(Class<?> targetClass) {
        checkNotNull(targetClass);
        Constructor<?> constructor;
        try {
            constructor = targetClass.getDeclaredConstructor();
        } catch (NoSuchMethodException ignored) {
            return false;
        }

        if (!Modifier.isPrivate(constructor.getModifiers())) {
            return false;
        }

        callConstructor(constructor);
        return true;
    }

    /**
     * Asserts that the passed message has a field that matches the passed field mask.
     *
     * <p>If the passed mask contains repeated message fields, asserts whether that field repeats
     * at least once, e.g. for a mask of
     * <pre>
     *     mask {
     *         paths: friends
     *     }
     * </pre>
     * and messages
     * <pre>
     *     message Animal {
     *         string kind = 1;
     *     }
     *
     *     message User {
     *         string name = 1;
     *         repeated User friends = 2;
     *         repeated Animal animals = 3;
     *     }
     * </pre>
     * the mask matches if a user has at least one friend.
     *
     * <p>However, a mask
     * <pre>
     *     mask {
     *         paths: animals.kind
     *     }
     * </pre>
     * will never match against a user, since if a repeated field is a part of the mask,
     * it should always be the last part of its path.
     *
     * @param message
     *         the message to assert
     * @param fieldMask
     *         which is matched against the message field
     * @throws AssertionError
     *         if the check fails
     */
    public static void assertMatchesMask(Message message, FieldMask fieldMask) {
        checkNotNull(message);
        checkNotNull(fieldMask);
        List<String> paths = fieldMask.getPathsList();

        List<FieldDescriptor> fields =
                message.getDescriptorForType()
                       .getFields();

        List<String> fieldNames =
                fields.stream()
                      .map(FieldDescriptor::getName)
                      .collect(toImmutableList());

        // Assert that the passed field mask contains the field of this message type.
        assertThat(fieldNames).containsAtLeastElementsIn(paths);

        for (FieldDescriptor field : fields) {
            boolean maskHasSuchField = paths.contains(field.getName());
            if (field.isRepeated()) {
                if (maskHasSuchField) {
                    List<?> repeatedFieldValue = (List<?>) message.getField(field);
                    boolean repeatsAtLeastOnce = repeatedFieldValue.isEmpty();
                    assertFalse(repeatsAtLeastOnce,
                                format("Field %s wasn't found in the specified message.",
                                       field.getName()));
                }
            } else {
                assertEquals(message.hasField(field), maskHasSuchField,
                             format("Mismatch found between field %s in the " +
                                            "specified message and the mask.",
                                    field.getName()));
            }
        }
    }

    /**
     * Asserts that the difference between expected value and actual value is not bigger
     * than the set delta.
     *
     * <p>The assertion will be passed if the actual delta equals to the set one.
     *
     * @param expectedValue
     *         expected value
     * @param actualValue
     *         actual value
     * @param delta
     *         the maximum expected difference between the values
     */
    public static void assertInDelta(long expectedValue, long actualValue, long delta) {
        long actualDelta = abs(expectedValue - actualValue);
        assertTrue(actualDelta <= delta);
    }
}
