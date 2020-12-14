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

package io.spine.validate;

import com.google.common.collect.ImmutableList;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Allows to determine safe variants of a number conversions without losing precision.
 *
 * <p>Mimics the actual automatic conversions that are applied to primitive number types.
 */
final class NumberConversion {

    private static final ImmutableList<ConversionChecker<?>> CHECKERS = ImmutableList.of(
            new ByteChecker(), new ShortChecker(), new IntegerChecker(), new LongChecker(),
            new FloatChecker(), new DoubleChecker()
    );

    /** Prevents direct instantiation. */
    private NumberConversion() {
    }

    /**
     * Determines if the supplied {@code number} can be safely converted to
     * the {@code anotherNumber}.
     */
    static boolean check(Number number, Number anotherNumber) {
        checkNotNull(number);
        checkNotNull(anotherNumber);
        Number unwrappedNumber = unwrap(number);
        Number unwrappedAnotherNumber = unwrap(anotherNumber);
        for (ConversionChecker<?> caster : CHECKERS) {
            if (caster.supports(unwrappedNumber)) {
                return caster.isConvertible(unwrappedAnotherNumber);
            }
        }
        return false;
    }

    /**
     * Unwraps the actual {@code value} from the {@link ComparableNumber}.
     */
    private static Number unwrap(Number number) {
        if (number instanceof ComparableNumber) {
            return ((ComparableNumber) number).value();
        }
        return number;
    }

    /**
     * Allows to determine which types a number of {@code <T>} type can be converted to.
     *
     * @param <T>
     *         type of a number for which conversions should be checked
     */
    private interface ConversionChecker<T extends Number> {

        /**
         * Determines if the supplied {@code number} can be safely converted to the type of
         * the caster.
         */
        default boolean isConvertible(Number number) {
            Class<? extends Number> numberClass = number.getClass();
            for (Class<? extends Number> type : convertibleTypes()) {
                if (type.equals(numberClass)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Determines if the supplied {@code number} is supported by the caster.
         */
        default boolean supports(Number number) {
            return casterType().isInstance(number);
        }

        /**
         * Returns {@code T} type class instance.
         */
        Class<T> casterType();

        /**
         * Returns types which {@code T} type can be safely converted to.
         */
        ImmutableList<Class<? extends Number>> convertibleTypes();
    }

    private static class ByteChecker implements ConversionChecker<Byte> {

        @Override
        public Class<Byte> casterType() {
            return Byte.class;
        }

        @Override
        public ImmutableList<Class<? extends Number>> convertibleTypes() {
            return ImmutableList.of(Byte.class);
        }
    }

    private static class ShortChecker implements ConversionChecker<Short> {

        @Override
        public Class<Short> casterType() {
            return Short.class;
        }

        @Override
        public ImmutableList<Class<? extends Number>> convertibleTypes() {
            return ImmutableList.of(Byte.class, Short.class);
        }
    }

    private static class IntegerChecker implements ConversionChecker<Integer> {

        @Override
        public Class<Integer> casterType() {
            return Integer.class;
        }

        @Override
        public ImmutableList<Class<? extends Number>> convertibleTypes() {
            return ImmutableList.of(Byte.class, Short.class, Integer.class);
        }
    }

    private static class LongChecker implements ConversionChecker<Long> {

        @Override
        public Class<Long> casterType() {
            return Long.class;
        }

        @Override
        public ImmutableList<Class<? extends Number>> convertibleTypes() {
            return ImmutableList.of(Byte.class, Short.class, Integer.class, Long.class);
        }
    }

    private static class FloatChecker implements ConversionChecker<Float> {

        @Override
        public Class<Float> casterType() {
            return Float.class;
        }

        @Override
        public ImmutableList<Class<? extends Number>> convertibleTypes() {
            return ImmutableList.of(Float.class);
        }
    }

    private static class DoubleChecker implements ConversionChecker<Double> {

        @Override
        public Class<Double> casterType() {
            return Double.class;
        }

        @Override
        public ImmutableList<Class<? extends Number>> convertibleTypes() {
            return ImmutableList.of(Float.class, Double.class);
        }
    }
}
