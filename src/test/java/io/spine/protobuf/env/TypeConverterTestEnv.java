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

package io.spine.protobuf.env;

import com.google.protobuf.StringValue;
import io.spine.base.ListOfAnys;
import io.spine.base.MapOfAnys;
import io.spine.protobuf.AnyPacker;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Test environment for {@link io.spine.protobuf.TypeConverterTest TypeConverterTest}.
 */
public class TypeConverterTestEnv {

    /**
     * Prevents instantiation of this utility class.
     */
    private TypeConverterTestEnv() {
    }

    /**
     * Converts the given list of {@link String} to {@link ListOfAnys}.
     */
    public static ListOfAnys toProtoList(List<String> list) {
        var anys = list.stream()
                .map(s -> AnyPacker.pack(StringValue.of(s)))
                .collect(toList());
        return ListOfAnys.newBuilder()
                .addAllValue(anys)
                .build();
    }

    /**
     * Converts the given map of {@link String} to {@link MapOfAnys}.
     */
    public static MapOfAnys toProtoMap(Map<String, String> map) {
        var entries = map.entrySet()
                .stream()
                .map(e -> {
                    var key = AnyPacker.pack(StringValue.of(e.getKey()));
                    var value = AnyPacker.pack(StringValue.of(e.getValue()));
                    return MapOfAnys.Entry.newBuilder()
                            .setKey(key)
                            .setValue(value)
                            .build();
                })
                .collect(toList());
        return MapOfAnys.newBuilder()
                .addAllEntry(entries)
                .build();
    }
}
