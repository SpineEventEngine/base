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

package io.spine.protobuf;

import com.google.common.base.Converter;
import com.google.protobuf.Message;

/**
 * A converter handling the primitive types transformations.
 *
 * <p>It's sufficient to override methods {@link #pack(Object) pack(T)} and
 * {@link #unpack(Message) unpack(M)} when extending this class.
 *
 * <p>Since the Protobuf and Java primitives differ, there may be more then one
 * {@code PrimitiveHandler} for a Java primitive type. In this case, if the resulting Protobuf
 * value type is not specified explicitly, the closest type is selected as a target for
 * the conversion. The closeness of two types is determined by the lexicographic closeness.
 *
 * @param <M>
 *         the type of the Protobuf primitive wrapper
 * @param <T>
 *         the type of the Java primitive wrapper
 */
abstract class PrimitiveHandler<M extends Message, T> extends Converter<M, T> {

    @Override
    protected final T doForward(M input) {
        return unpack(input);
    }

    @Override
    protected final M doBackward(T input) {
        return pack(input);
    }

    /**
     * Unpacks a primitive value of type {@code T} from the given wrapper value.
     *
     * @param message
     *         packed value
     * @return unpacked value
     */
    protected abstract T unpack(M message);

    /**
     * Packs the given primitive value into a Protobuf wrapper of type {@code M}.
     *
     * @param value
     *         primitive value
     * @return packed value
     */
    protected abstract M pack(T value);
}
