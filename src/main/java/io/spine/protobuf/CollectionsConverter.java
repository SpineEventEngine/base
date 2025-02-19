/*
 * Copyright 2022, TeamDev. All rights reserved.
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

package io.spine.protobuf;

import com.google.protobuf.Any;
import io.spine.annotation.Internal;
import io.spine.base.ListOfAnys;
import io.spine.base.MapOfAnys;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Converts instances of {@code List} and {@code Map} into {@link Any}.
 */
@Internal
public final class CollectionsConverter {

    /**
     * Prevents this utility from instantiation.
     */
    private CollectionsConverter() {
    }

    /**
     * Converts the passed items to {@code Any}.
     *
     * <p>This conversion is designed to preserve the ordering
     * and essence of {@code Iterable} values in case this {@code Iterable}
     * is required to be transferred outside JVM.
     *
     * <p>Each of the values are converted to {@code Any} one-by-one
     * via {@link TypeConverter}, and then packed into a wrapping instance
     * of {@link ListOfAnys}. In turn, {@code ListOfAnys} instance is packed
     * into {@code Any} and returned as a result.
     *
     * <p>Please note that the types of {@code Iterable}'s values
     * should be supported by {@code TypeConverter}.
     *
     * @param items
     *         the items to convert
     * @return new {@code Any} instance
     */
    public static Any toAny(Iterable<?> items) {
        checkNotNull(items);
        var asProto = toProto(items);
        var result = TypeConverter.toAny(asProto);
        return result;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")  /* Calling `builder` methods. */
    private static ListOfAnys toProto(Iterable<?> values) {
        var builder = ListOfAnys.newBuilder();
        for (var value : values) {
            builder.addValue(TypeConverter.toAny(value));
        }
        return builder.build();
    }

    /**
     * Converts the passed map to {@code Any}.
     *
     * <p>This conversion is designed to preserve the essence
     * of the passed {@code Map} values, in case this {@code Iterable} is required
     * to be transferred outside JVM.
     *
     * <p>Each of {@code Map}'s keys and values are converted one-by-one
     * via {@link TypeConverter}, and then packed into a wrapping instance
     * of {@link MapOfAnys}. In turn, {@code MapOfAnys} instance is packed
     * into {@code Any} and returned as a result.
     *
     * <p>Please note that the types of {@code Map}'s keys and values
     * should be supported by {@code TypeConverter}.
     */
    public static Any toAny(Map<?, ?> map) {
        checkNotNull(map);
        var asProto = toProto(map);
        var result = TypeConverter.toAny(asProto);
        return result;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")  /* Calling `builder` methods. */
    private static MapOfAnys toProto(Map<?, ?> map) {
        var builder = MapOfAnys.newBuilder();
        map.forEach((key, value) -> {
            var entry = MapOfAnys.Entry.newBuilder()
                    .setKey(TypeConverter.toAny(key))
                    .setValue(TypeConverter.toAny(value));
            builder.addEntry(entry);
        });
        return builder.build();
    }
}
