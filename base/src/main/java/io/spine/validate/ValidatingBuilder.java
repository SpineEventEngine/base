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

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import io.spine.annotation.Internal;

import java.lang.reflect.Method;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static io.spine.validate.AbstractValidatingBuilder.getNewBuilderMethod;

/**
 * An interface for all validating builders.
 *
 * <p>Validating builder is used to validate messages according to their Protobuf definition
 * during the {@code Message} creation.
 *
 * <p>Non-abstract implementations must declare {@code public static TYPE newBuilder()} method,
 * returning an instance of the implementation class.
 *
 * @param <T> the type of the message to build
 * @param <B> the type of the message builder
 *
 * @deprecated Use {@link io.spine.protobuf.ValidatingBuilder#vBuild()} instead.
 */
@Deprecated
@SuppressWarnings("DeprecatedIsStillUsed") // To be removed gradually.
public interface ValidatingBuilder<T extends Message, B extends Message.Builder> {

    /**
     * Validates the field according to the Protobuf message declaration.
     *
     * @param descriptor the {@code FieldDescriptor} of the field
     * @param fieldValue the value of the field
     * @param fieldName  the name of the field
     * @param <V>        the type of the field value
     * @throws ValidationException if there are any constraint violations
     */
    <V> void validate(FieldDescriptor descriptor, V fieldValue, String fieldName)
            throws ValidationException;

    /**
     * Validates and builds {@code Message}.
     *
     * @return the {@code Message} instance
     * @throws ValidationException if there are any constraint violations
     */
    T build() throws ValidationException;

    /**
     * Merges the message currently built with the fields of a given {@code message}.
     *
     * @param message the message to merge into the state this builder
     * @return the builder instance
     */
    @CanIgnoreReturnValue
    ValidatingBuilder<T, B> mergeFrom(T message);

    /**
     * Sets an original state for this builder to allow building the new value on top of some
     * pre-defined value.
     *
     * <p>The validation of the passed {@code state} is NOT performed.
     *
     * @param state the new state
     */
    @Internal
    void setOriginalState(T state);

    /**
     * Determines if the current message state has been modified comparing to its original state.
     *
     * @return {@code true} if it is modified, {@code false} otherwise
     */
    @Internal
    boolean isDirty();

    /**
     * Clears the state of this builder.
     *
     * <p>In particular, the state of the message being built is reset to the default.
     * Also the original state (if it {@linkplain #setOriginalState(Message) has been set}
     * previously) is cleared.
     */
    @Internal
    void clear();

    /**
     * Creates an instance of {@code ValidatingBuilder} by its type.
     *
     * @param builderClass the type of the {@code ValidatingBuilder} to instantiate
     * @param <B>          the generic type of returned value
     * @return the new instance of the builder
     */
    @Internal
    @SuppressWarnings("OverlyBroadCatchBlock")   // OK, as the exception handling is the same.
    static <B extends ValidatingBuilder<?, ?>> B newInstance(Class<B> builderClass) {
        checkNotNull(builderClass);

        try {
            Method newBuilderMethod = getNewBuilderMethod(builderClass);
            Object raw = newBuilderMethod.invoke(null);

            // By convention, `newBuilder()` always returns instances of `B`.
            @SuppressWarnings("unchecked") B builder = (B) raw;
            return builder;
        } catch (Exception e) {
            throw illegalStateWithCauseOf(e);
        }
    }
}
