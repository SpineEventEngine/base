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

package io.spine.testing;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.FieldMask;
import com.google.protobuf.Message;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.truth.Truth.assertThat;
import static java.lang.Math.abs;

/**
 * Utilities for testing.
 */
public final class Tests {

    /** Prevent instantiation of this utility class. */
    private Tests() {
    }

    /**
     * Asserts that two booleans are equal.
     *
     * <p>This method is needed to avoid dependency on JUnit 4.x in projects that use
     * Spine and JUnit5.
     */
    @VisibleForTesting
    static void assertEquals(boolean expected, boolean actual) {
        if (expected != actual) {
            throw new AssertionError();
        }
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
     *     {@literal @}Test
     *     public void have_private_utility_ctor() {
     *         assertHasPrivateParameterlessCtor(MyUtility.class));
     *     }
     * </pre>
     */
    public static void assertHasPrivateParameterlessCtor(Class<?> targetClass) {
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
        Constructor constructor;
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
     * Calls the passed constructor include it into the coverage.
     *
     * <p>Some of the coding conventions may encourage throwing {@link AssertionError}
     * to prevent the instantiation of the target class, if it is designed as a utility class.
     * This method catches all the exceptions which may be thrown by the constructor.
     */
    @SuppressWarnings("OverlyBroadCatchBlock") // see Javadoc
    private static void callConstructor(Constructor constructor) {
        boolean accessible = constructor.isAccessible();
        if (!accessible) {
            constructor.setAccessible(true);
        }
        try {
            constructor.newInstance();
        } catch (Exception ignored) {
            // Do nothing.
        } finally {
            if (!accessible) {
                constructor.setAccessible(false);
            }
        }
    }

    /**
     * Returns {@code null}.
     *
     * <p>Use it when it is needed to pass {@code null} to a method in tests so that no
     * warnings suppression is needed.
     *
     * @apiNote The method doesn't take {@code Class<T>} to keep
     *         the test API small and convenient
     */
    @SuppressWarnings("TypeParameterUnusedInFormals" /* See api note. */)
    public static <T> T nullRef() {
        T nullRef = null;
        return nullRef;
    }

    /**
     * Asserts that the passed message has a field that matches the passed field mask.
     *
     * @param message
     *         the message to assert
     * @param fieldMask
     *         which is matched against the message field
     * @throws AssertionError
     *         if the check fails
     */
    public static void assertMatchesMask(Message message, FieldMask fieldMask) {
        List<String> paths = fieldMask.getPathsList();

        List<FieldDescriptor> fields = message.getDescriptorForType()
                                              .getFields();

        List<String> fieldNames = fields.stream()
                                        .map(FieldDescriptor::getName)
                                        .collect(toImmutableList());

        // Assert that the passed field mask contains the field of this message type.
        assertThat(fieldNames).containsAllIn(paths);

        // Assert that values match the field mask.
        for (FieldDescriptor field : fields) {
            if (field.isRepeated()) {
                boolean pathsHasSuchField = paths.contains(field.getName());
                if (pathsHasSuchField) {
                    List<?> repeatedFieldValue = (List<?>) message.getField(field);
                    boolean fieldValueAbsent = repeatedFieldValue.isEmpty();
                    assertTrue(!fieldValueAbsent);
                }
            } else {
                assertEquals(message.hasField(field), paths.contains(field.getName()));
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
