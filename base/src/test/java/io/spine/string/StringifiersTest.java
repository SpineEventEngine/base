/*
 * Copyright 2021, TeamDev. All rights reserved.
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

package io.spine.string;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.Duration;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Durations;
import com.google.protobuf.util.Timestamps;
import io.spine.base.Time;
import io.spine.json.Json;
import io.spine.test.string.STask;
import io.spine.test.string.STaskId;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.base.Identifier.newUuid;
import static io.spine.string.Stringifiers.newForListOf;
import static io.spine.string.Stringifiers.newForMapOf;
import static io.spine.test.string.STaskStatus.DONE;

@DisplayName("`Stringifiers` utility class should")
class StringifiersTest extends UtilityClassTest<Stringifiers> {

    StringifiersTest() {
        super(Stringifiers.class);
    }

    @Nested
    @DisplayName("stringify")
    class Stringify {

        @Test
        @DisplayName("a `boolean`")
        void aBoolean() {
            checkStringifies(false, "false");
        }

        @Test
        @DisplayName("an `int`")
        void anInteger() {
            checkStringifies(1, "1");
        }

        @Test
        @DisplayName(" a `long`")
        void aLong() {
            checkStringifies(1L, "1");
        }

        @Test
        @DisplayName("a `String`")
        void aString() {
            var theString = "some-string";

            checkStringifies(theString, theString);
        }

        @Test
        @DisplayName("a `Timestamp`")
        void aTimestamp() {
            var timestamp = Timestamp.getDefaultInstance();
            var expected = Timestamps.toString(timestamp);

            checkStringifies(timestamp, expected);
        }

        @Test
        @DisplayName("a `Duration`")
        void aDuration() {
            var duration = Duration.getDefaultInstance();
            var expected = Durations.toString(duration);

            checkStringifies(duration, expected);
        }

        @Test
        @DisplayName("an enum")
        void aEnum() {
            checkStringifies(DONE, "DONE");
        }

        @Test
        @DisplayName("a Protobuf `Message`")
        void aProtobufMessage() {
            var id = STaskId.newBuilder()
                    .setUuid(newUuid())
                    .build();
            var message = STask.newBuilder()
                    .setId(id)
                    .setStatus(DONE)
                    .build();

            var expected = Json.toCompactJson(message);
            checkStringifies(message, expected);
        }

        private void checkStringifies(Object value, String expected) {
            var conversionResult = Stringifiers.toString(value);
            assertThat(conversionResult).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("create `Stringifier` with a delimeter for")
    class Delimited {

        private static final char DELIMITER = '#';
        private static final int SIZE = 5;

        @Test
        @DisplayName("`List`")
        void list() {
            List<Timestamp> stamps = createList();
            var stringifier = newForListOf(Timestamp.class, DELIMITER);

            var out = stringifier.toString(stamps);

            var assertOut = assertThat(out);
            assertOut.contains(String.valueOf(DELIMITER));
            var quoter = Quoter.forLists();
            for (var stamp : stamps) {
                assertOut.contains(quoter.quote(Timestamps.toString(stamp)));
            }
        }

        @Test
        @DisplayName("`Map`")
        void map() {
            var stamps = createMap();
            var stringifier = newForMapOf(Long.class, Timestamp.class, DELIMITER);

            var out = stringifier.toString(stamps);
            var assertOut = assertThat(out);
            assertOut.contains(String.valueOf(DELIMITER));

            var quoter = Quoter.forMaps();
            for (var key : stamps.keySet()) {
                assertOut.contains(String.valueOf(key));
                assertOut.contains(quoter.quote(Timestamps.toString(stamps.get(key))));
            }
        }

        private ImmutableList<Timestamp> createList() {
            ImmutableList.Builder<Timestamp> builder = ImmutableList.builder();
            for (var i = 0; i < SIZE; i++) {
                builder.add(Time.currentTime());
            }
            return builder.build();
        }

        private ImmutableMap<Long, Timestamp> createMap() {
            ImmutableMap.Builder<Long, Timestamp> builder = ImmutableMap.builder();
            for (var i = 0; i < SIZE; i++) {
                var t = Time.currentTime();
                builder.put((long) i, t);
            }
            return builder.build();
        }
    }

    /**
     * This class covers only cases that are not touched by other tests that
     * involve parsing of string values.
     */
    @Nested
    @DisplayName("parse a string into")
    class Parsing {

        @Test
        @DisplayName("`Boolean`")
        void logical() {
            assertThat(Stringifiers.fromString("true", Boolean.class))
                    .isTrue();
        }

        @Test
        @DisplayName("`Integer`")
        void integerNumber() {
            assertThat(Stringifiers.fromString("-100500", Integer.class))
                    .isEqualTo(-100500);
        }

        @Test
        @DisplayName("`List`")
        void list() {
            List<Integer> numbers = ImmutableList.of(100, 200, -300);
            var stringifier = Stringifiers.newForListOf(Integer.class);
            var numString = stringifier.toString(numbers);

            var parsed = stringifier.fromString(numString);
            assertThat(parsed).containsExactlyElementsIn(numbers);
        }
    }
}
