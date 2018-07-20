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

package io.spine.util;

import com.google.common.base.Suppliers;
import io.spine.testing.TestValues;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static com.google.common.testing.SerializableTester.reserializeAndAssert;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings({"InnerClassMayBeStatic", "Guava"}) // we test adaptation API.
@DisplayName("Suppliers2 utility class should")
class Suppliers2Test extends UtilityClassTest<Suppliers2> {

    Suppliers2Test() {
        super(Suppliers2.class);
    }

    @Nested
    @DisplayName("Adapt Guava Supplier")
    class Adapter {

        private com.google.common.base.Supplier<Long> adaptee;
        private Supplier<Long> adapter;

        @BeforeEach
        void setUp() {
            adaptee = Suppliers.ofInstance((long) TestValues.random(10_000));
            adapter = Suppliers2.adapt(adaptee);
        }

        @Test
        @DisplayName("returning its values")
        void gettingValues() {
            assertEquals(adaptee.get(), adapter.get());
        }

        @Test
        @DisplayName("being Serializable")
        void serialize() {
            reserializeAndAssert(adapter);
        }
    }

    @Nested
    @DisplayName("Provide reverse adapter for Guava Supplier")
    class ReverseAdapter {

        private com.google.common.base.Supplier<String> adapter;
        private Supplier<String> adaptee;

        @BeforeEach
        void setUp() {
            String value = TestValues.randomString();
            adaptee = Suppliers2.adapt(Suppliers.ofInstance(value));
            adapter = Suppliers2.reverse(adaptee);
        }

        @Test
        @DisplayName("which returns adaptee values")
        void gettingValues() {
            assertEquals(adaptee.get(), adapter.get());
        }

        @Test
        @DisplayName("which is Serializable")
        void serialize() {
            reserializeAndAssert(adapter);
        }
    }

    @Nested
    @DisplayName("Provide memoizing Supplier")
    class Memoizing {

        @Test
        @DisplayName("which remembers values")
        void remember() {
            Supplier<Integer> supplier = new CountingSupplier();
            assertOutput(0, supplier);
            // We incremented the counter here.

            Supplier<Integer> memoizing = Suppliers2.memoize(supplier);
            // See that we get a new value from the adaptee.
            assertOutput(1, memoizing);
            // Should be the same as we memoize.
            assertOutput(1, memoizing);
            // Still the same.
            assertOutput(1, memoizing);
        }

        private void assertOutput(int expected, Supplier<Integer> supplier) {
            assertEquals(expected, supplier.get()
                                           .intValue());
        }

        /** The counter incrementing after each {@link #get()}. */
        private class CountingSupplier implements Supplier<Integer> {

            private int count = 0;

            @Override
            public Integer get() {
                Integer result = count;
                ++count;
                return result;
            }
        }
    }
}
