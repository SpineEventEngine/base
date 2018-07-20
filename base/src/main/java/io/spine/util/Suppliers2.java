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
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utilities for working with {@link java.util.function.Supplier Supplier}s.
 *
 * <p>These utilities aim to adapt those {@linkplain com.google.common.base.Suppliers provided by
 * Guava}.
 *
 * @author Alexander Yevsyukov
 */
@SuppressWarnings("Guava") /* Adapt Guava Suppliers is one of the purposes of the class. */
public class Suppliers2 {

    /** Prevents instantiation of this utility class. */
    private Suppliers2() {
    }

    /**
     * Creates a supplier which caches the value obtained from the passed one.
     *
     * <p>This creates an adapter for a {@linkplain
     * Suppliers#memoize(com.google.common.base.Supplier) memoizing supplier} provided by Guava.
     */
    public static <T> Supplier<T> memoize(Supplier<T> supplier) {
        checkNotNull(supplier);
        return adapt(Suppliers.memoize(reverse(supplier)));
    }

    /**
     * Creates a standard {@code Supplier} which adapts Guava's.
     */
    public static <T> Supplier<T> adapt(com.google.common.base.Supplier<T> supplier) {
        checkNotNull(supplier);
        return new Adapter<>(supplier);
    }

    /**
     * Creates a Guava's {@code Supplier} which adapts a standard one.
     */
    public static <T> com.google.common.base.Supplier<T> reverse(Supplier<T> supplier) {
        checkNotNull(supplier);
        return new ReverseAdapter<>(supplier);
    }

    /**
     * Adapts the Guava supplier to math the {@linkplain java.util.function.Supplier standard
     * Java API}.
     *
     * @param <T> the type of the object to supply
     */
    private static final class Adapter<T> implements Supplier<T>, Serializable {

        private static final long serialVersionUID = 0L;

        /**
         * Guava's Supplier being adapted.
         *
         * <p>This field is serializable since Guava's Suppliers are
         * {@linkplain com.google.common.base.Suppliers serializable} if their
         * parameters are serializable.
         */
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final com.google.common.base.Supplier<T> adaptee;

        private Adapter(com.google.common.base.Supplier<T> adaptee) {
            this.adaptee = adaptee;
        }

        @Override
        public T get() {
            return adaptee.get();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Adapter)) {
                return false;
            }
            Adapter<?> adapter = (Adapter<?>) o;
            return Objects.equals(adaptee, adapter.adaptee);
        }

        @Override
        public int hashCode() {
            return Objects.hash(adaptee);
        }
    }

    /**
     * Adapts {@linkplain java.util.function.Supplier standard Supplier} to Guava's.
     *
     * <p>This class is {@code Serializable} if the adaptee is {@code Serializable}.
     *
     * @param <T> the type of the object to supply
     */
    private static final class ReverseAdapter<T>
            implements com.google.common.base.Supplier<T>, Serializable {

        private static final long serialVersionUID = 0L;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Supplier<T> adaptee;

        private ReverseAdapter(Supplier<T> adaptee) {
            this.adaptee = adaptee;
        }

        @CanIgnoreReturnValue
        @Override
        public T get() {
            return adaptee.get();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ReverseAdapter)) {
                return false;
            }
            ReverseAdapter<?> adapter = (ReverseAdapter<?>) o;
            return Objects.equals(adaptee, adapter.adaptee);
        }

        @Override
        public int hashCode() {
            return Objects.hash(adaptee);
        }
    }
}
