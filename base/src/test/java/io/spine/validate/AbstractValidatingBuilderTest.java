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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.truth.MapSubject;
import com.google.protobuf.BoolValue;
import com.google.protobuf.FloatValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Timestamp;
import com.google.protobuf.UInt32Value;
import com.google.protobuf.util.Timestamps;
import io.spine.base.ConversionException;
import io.spine.base.Time;
import io.spine.protobuf.Durations2;
import io.spine.string.Stringifiers;
import io.spine.validate.builders.StringValueVBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.truth.Truth.assertThat;
import static io.spine.validate.AbstractValidatingBuilder.convertToList;
import static io.spine.validate.AbstractValidatingBuilder.convertToMap;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("AbstractValidatingBuilder should")
class AbstractValidatingBuilderTest {

    @Nested
    @DisplayName("Convert a raw String to")
    class Convert {

        private final Joiner joiner = Joiner.on(',');

        @Nested
        @DisplayName("Map")
        class OfMap {

            /** Creates string representation of a map entry for the the passed key-value pair. */
            String entry(Object key, Object value) {
                return format("\"%s\":\"%s\"",
                              Stringifiers.toString(key), Stringifiers.toString(value));
            }

            @Test
            @DisplayName("from string key-value")
            void stringKeyValue() {
                String input = joiner.join(entry("key1", "123"), entry("key2", "234"));

                Map<String, UInt32Value> map =
                        convertToMap(input, String.class, UInt32Value.class);

                MapSubject assertMap = assertThat(map);

                assertMap.containsEntry(
                        "key1", UInt32Value
                                .newBuilder()
                                .setValue(123)
                                .build()
                );

                assertMap.containsEntry(
                        "key2", UInt32Value
                                .newBuilder()
                                .setValue(234)
                                .build()
                );
            }

            @Test
            @DisplayName("from message-message key-value")
            void messageKeyValue() {
                FloatValue k1 = FloatValue
                        .newBuilder()
                        .setValue(3.14f)
                        .build();
                FloatValue k2 = FloatValue
                        .newBuilder()
                        .setValue(2.54f)
                        .build();
                Map<FloatValue, Boolean> inputMap = ImmutableMap.of(
                        k1, Boolean.TRUE,
                        k2, Boolean.FALSE
                );

                List<String> entries =
                        inputMap.entrySet()
                                .stream()
                                .map(e -> entry(e.getKey(), e.getValue()))
                                .collect(Collectors.toList());

                String input = joiner.join(entries);

                Map<FloatValue, BoolValue> output =
                        convertToMap(input, FloatValue.class, BoolValue.class);

                MapSubject assertOutput = assertThat(output);
                assertOutput.containsEntry(k1, BoolValue.newBuilder()
                                                        .setValue(true)
                                                        .build());
                assertOutput.containsEntry(k2, BoolValue.newBuilder()
                                                        .setValue(false)
                                                        .build());
            }
        }

        @Nested
        @DisplayName("List")
        class ToList {

            /**
             * Wraps string representation of the passed object into quotes.
             */
            String item(Object o) {
                return format("\"%s\"", Stringifiers.toString(o));
            }

            /**
             * Creates a test input string from the list of objects.
             */
            private String createInput(ImmutableList<?> items) {
                ImmutableList<String> strItems =
                        items.stream()
                             .map(this::item)
                             .collect(toImmutableList());
                return joiner.join(strItems);
            }

            @Test
            @DisplayName("of strings")
            void toList() {
                ImmutableList<String> items =
                        ImmutableList.of("something", "entry1", "anything", "entry2");
                String str = createInput(items);

                List<String> list = convertToList(str, String.class);

                assertThat(list).containsAllIn(items);
            }

            @Test
            @DisplayName("of messages")
            void toMessageList() {
                Timestamp now = Time.getCurrentTime();
                Timestamp soon = Timestamps.add(now, Durations2.minutes(5));
                ImmutableList<Timestamp> times = ImmutableList.of(now, soon);
                String str = createInput(times);

                List<Timestamp> list = convertToList(str, Timestamp.class);

                assertThat(list).containsAllIn(times);
            }
        }

        @Test
        @DisplayName("Instance of a class")
        void toInstance() throws ConversionException {
            String value = "123";
            StringValueVBuilder builder = StringValueVBuilder.newBuilder();
            Int32Value convertedValue = builder.convert(value, Int32Value.class);
            assertEquals(Integer.parseInt(value), convertedValue.getValue());
        }
    }

    @Test
    @DisplayName("Fail conversion of a string if non-matching class passed")
    void toWrongClass() {
        String notInt = "notInt";
        StringValueVBuilder builder = StringValueVBuilder.newBuilder();
        assertThrows(ConversionException.class,
                     () -> builder.convert(notInt, Int32Value.class));
    }

    @Test
    @DisplayName("Have public internalBuild() method")
    void publicInternalBuild() throws NoSuchMethodException {
        Method method = AbstractValidatingBuilder.class.getMethod("internalBuild");
        assertThat(Modifier.isPublic(method.getModifiers())).isTrue();
    }

    @Test
    @DisplayName("Verify if became 'dirty' on modification")
    void dirty() {
        StringValueVBuilder builder = StringValueVBuilder.newBuilder();
        assertThat(builder.isDirty())
                .isFalse();
        assertThat(builder.setValue("get dirty")
                          .isDirty())
                .isTrue();
    }
}
