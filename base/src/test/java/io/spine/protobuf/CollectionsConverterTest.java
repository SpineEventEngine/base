/*
 * Copyright 2025, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.spine.base.ListOfAnys;
import io.spine.base.MapOfAnys;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;
import static io.spine.protobuf.TypeConverter.toObject;

@SuppressWarnings("deprecation") // We still tell until `CollectionsConverter` is removed.
@DisplayName("`CollectionsConverter` should")
class CollectionsConverterTest extends UtilityClassTest<CollectionsConverter> {

    CollectionsConverterTest() {
        super(CollectionsConverter.class);
    }

    @Test
    @DisplayName("convert `Iterable`s to `Any`")
    void convertLists() {
        var source = ImmutableList.of(2, 12, 85, 6);
        var result = CollectionsConverter.toAny(source);
        assertThat(result)
                .isNotNull();
        var unpacked = AnyPacker.unpack(result);
        assertThat(unpacked)
                .isInstanceOf(ListOfAnys.class);
        var listOfAnys = (ListOfAnys) unpacked;
        var values = listOfAnys.getValueList();
        for (var index = 0; index < values.size(); index++) {
            var element = values.get(index);
            var unpackedElement = toObject(element, Integer.class);
            assertThat(unpackedElement)
                    .isEqualTo(source.get(index));
        }
    }

    @Test
    @DisplayName("convert `Map`s to `Any`")
    void convertMaps() {
        var source = ImmutableMap.of("first", 1,
                                     "second", 2,
                                     "third", 3);
        var result = CollectionsConverter.toAny(source);
        assertThat(result)
                .isNotNull();
        var unpacked = AnyPacker.unpack(result);
        assertThat(unpacked)
                .isInstanceOf(MapOfAnys.class);
        var mapOfAnys = (MapOfAnys) unpacked;
        var entries = mapOfAnys.getEntryList();
        assertThat(entries.size())
                .isEqualTo(source.size());
        for (var entry : entries) {
            var packedKey = entry.getKey();
            var key = toObject(packedKey, String.class);
            var packedValue = entry.getValue();
            var value = toObject(packedValue, Integer.class);
            assertThat(source.keySet())
                    .contains(key);
            assertThat(value)
                    .isEqualTo(source.get(key));
        }
    }
}
