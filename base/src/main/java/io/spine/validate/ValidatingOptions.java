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

package io.spine.validate;

import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Message;
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
 */
@SPI
@Immutable
public interface ValidatingOptions {

    /**
     * Obtains additional options for {@code bool} fields validation.
     * 
     * @return the set of additional options
     * @implSpec By default, obtains an empty set.
     */
    default Set<FieldValidatingOption<?, Boolean>> forBoolean() {
        return ImmutableSet.of();
    }

    /**
     * Obtains additional options for {@code bytes} fields validation.
     *
     * @return the set of additional options
     * @implSpec By default, obtains an empty set.
     */
    default Set<FieldValidatingOption<?, ByteString>> forByteString() {
        return ImmutableSet.of();
    }

    /**
     * Obtains additional options for {@code double} fields validation.
     *
     * @return the set of additional options
     * @implSpec By default, obtains an empty set.
     */
    default Set<FieldValidatingOption<?, Double>> forDouble() {
        return ImmutableSet.of();
    }

    /**
     * Obtains additional options for enum fields validation.
     *
     * @return the set of additional options
     * @implSpec By default, obtains an empty set.
     */
    default Set<FieldValidatingOption<?, EnumValueDescriptor>> forEnum() {
        return ImmutableSet.of();
    }

    /**
     * Obtains additional options for {@code float} fields validation.
     *
     * @return the set of additional options
     * @implSpec By default, obtains an empty set.
     */
    default Set<FieldValidatingOption<?, Float>> forFloat() {
        return ImmutableSet.of();
    }

    /**
     * Obtains additional options for {@code int64}, {@code sint64}, {@code uint64},
     * {@code fixed64}, and {@code sfixed64} fields validation.
     *
     * @return the set of additional options
     * @implSpec By default, obtains an empty set.
     */
    default Set<FieldValidatingOption<?, Integer>> forInt() {
        return ImmutableSet.of();
    }

    /**
     * Obtains additional options for {@code int32}, {@code sint32}, {@code uint32},
     * {@code fixed32}, and {@code sfixed32} fields validation.
     *
     * @return the set of additional options
     * @implSpec By default, obtains an empty set.
     */
    default Set<FieldValidatingOption<?, Long>> forLong() {
        return ImmutableSet.of();
    }

    /**
     * Obtains additional options for message fields validation.
     *
     * @return the set of additional options
     * @implSpec By default, obtains an empty set.
     */
    default Set<FieldValidatingOption<?, Message>> forMessage() {
        return ImmutableSet.of();
    }

    /**
     * Obtains additional options for {@code string} fields validation.
     *
     * @return the set of additional options
     * @implSpec By default, obtains an empty set.
     */
    default Set<FieldValidatingOption<?, String>> forString() {
        return ImmutableSet.of();
    }
}
