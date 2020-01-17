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

package io.spine.validate.option;

import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.Immutable;
import io.spine.annotation.SPI;

import java.util.ServiceLoader;
import java.util.Set;

/**
 * A factory of validation options for message fields.
 *
 * <p>This interface has no abstract methods. All the overridable methods are optional for
 * implementation. The default implementation retrieves empty sets.
 *
 * <p>This interface is designed as a Service Provider Interface. The implementations are
 * {@linkplain ValidatingOptionsLoader loaded} via the {@link ServiceLoader} mechanism.
 *
 * @see FieldValidatingOption
 * @see io.spine.validate.CustomConstraint
 */
@SPI
@Immutable
public interface ValidatingOptionFactory {

    /**
     * Obtains additional options for {@code bool} fields validation.
     *
     * @return the set of additional options
     * @implSpec By default, obtains an empty set.
     */
    default Set<FieldValidatingOption<?>> forBoolean() {
        return ImmutableSet.of();
    }

    /**
     * Obtains additional options for {@code bytes} fields validation.
     *
     * @return the set of additional options
     * @implSpec By default, obtains an empty set.
     */
    default Set<FieldValidatingOption<?>> forByteString() {
        return ImmutableSet.of();
    }

    /**
     * Obtains additional options for {@code double} fields validation.
     *
     * @return the set of additional options
     * @implSpec By default, obtains an empty set.
     */
    default Set<FieldValidatingOption<?>> forDouble() {
        return ImmutableSet.of();
    }

    /**
     * Obtains additional options for enum fields validation.
     *
     * @return the set of additional options
     * @implSpec By default, obtains an empty set.
     */
    default Set<FieldValidatingOption<?>> forEnum() {
        return ImmutableSet.of();
    }

    /**
     * Obtains additional options for {@code float} fields validation.
     *
     * @return the set of additional options
     * @implSpec By default, obtains an empty set.
     */
    default Set<FieldValidatingOption<?>> forFloat() {
        return ImmutableSet.of();
    }

    /**
     * Obtains additional options for {@code int64}, {@code sint64}, {@code uint64},
     * {@code fixed64}, and {@code sfixed64} fields validation.
     *
     * @return the set of additional options
     * @implSpec By default, obtains an empty set.
     */
    default Set<FieldValidatingOption<?>> forInt() {
        return ImmutableSet.of();
    }

    /**
     * Obtains additional options for {@code int32}, {@code sint32}, {@code uint32},
     * {@code fixed32}, and {@code sfixed32} fields validation.
     *
     * @return the set of additional options
     * @implSpec By default, obtains an empty set.
     */
    default Set<FieldValidatingOption<?>> forLong() {
        return ImmutableSet.of();
    }

    /**
     * Obtains additional options for message fields validation.
     *
     * @return the set of additional options
     * @implSpec By default, obtains an empty set.
     */
    default Set<FieldValidatingOption<?>> forMessage() {
        return ImmutableSet.of();
    }

    /**
     * Obtains additional options for {@code string} fields validation.
     *
     * @return the set of additional options
     * @implSpec By default, obtains an empty set.
     */
    default Set<FieldValidatingOption<?>> forString() {
        return ImmutableSet.of();
    }

    /**
     * Obtains all the options declared by this factory.
     *
     * @return the set of all additional options
     */
    default Set<FieldValidatingOption<?>> all() {
        return ImmutableSet
                .<FieldValidatingOption<?>>builder()
                .addAll(forBoolean())
                .addAll(forByteString())
                .addAll(forDouble())
                .addAll(forEnum())
                .addAll(forFloat())
                .addAll(forInt())
                .addAll(forLong())
                .addAll(forMessage())
                .addAll(forString())
                .build();
    }
}
