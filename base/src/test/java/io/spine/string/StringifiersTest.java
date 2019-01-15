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

package io.spine.string;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.truth.StringSubject;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import io.spine.base.Time;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.string.Stringifiers.newForListOf;
import static io.spine.string.Stringifiers.newForMapOf;

@DisplayName("Stringifiers utility class should")
class StringifiersTest extends UtilityClassTest<Stringifiers> {

    StringifiersTest() {
        super(Stringifiers.class);
    }

    @Nested
    @DisplayName("Create Stringifier with a delimeter for")
    class Delimited {

        private static final char DELIMITER = '❤';
        private static final int SIZE = 5;

        @Test
        @DisplayName("List")
        void list() {
            List<Timestamp> stamps = createList();
            Stringifier<List<Timestamp>> stringifier = newForListOf(Timestamp.class, DELIMITER);

            String out = stringifier.toString(stamps);

            StringSubject assertOut = assertThat(out);
            assertOut.contains(String.valueOf(DELIMITER));
            Quoter quoter = Quoter.forLists();
            for (Timestamp stamp : stamps) {
                assertOut.contains(quoter.quote(Timestamps.toString(stamp)));
            }
        }

        @Test
        @DisplayName("Map")
        void map() {
            ImmutableMap<Long, Timestamp> stamps = createMap();
            Stringifier<Map<Long, Timestamp>> stringifier =
                    newForMapOf(Long.class, Timestamp.class, DELIMITER);

            String out = stringifier.toString(stamps);
            StringSubject assertOut = assertThat(out);
            assertOut.contains(String.valueOf(DELIMITER));

            Quoter quoter = Quoter.forMaps();
            for (Long key : stamps.keySet()) {
                assertOut.contains(String.valueOf(key));
                assertOut.contains(quoter.quote(Timestamps.toString(stamps.get(key))));
            }
        }

        private ImmutableList<Timestamp> createList() {
            ImmutableList.Builder<Timestamp> builder = ImmutableList.builder();
            for (int i = 0; i < SIZE; i++) {
                builder.add(Time.getCurrentTime());
            }
            return builder.build();
        }

        private ImmutableMap<Long, Timestamp> createMap() {
            ImmutableMap.Builder<Long, Timestamp> builder = ImmutableMap.builder();
            for (int i = 0; i < SIZE; i++) {
                Timestamp t = Time.getCurrentTime();
                builder.put((long) i, t);
            }
            return builder.build();
        }
    }
}
