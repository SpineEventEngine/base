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

package io.spine.query;

import io.spine.testing.TestValues;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.TestValues.nullRef;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("`RecordColumn` should")
class RecordColumnTest {

    @Test
    @DisplayName("allow creating new instances")
    void allowCreation() {
        var name = "description";
        var description = "some description";
        var column = new RecordColumn<Manufacturer, String>(name, String.class, (r) -> description);

        assertThat(column).isNotNull();
        assertThat(column.name().value()).isEqualTo(name);
        assertThat(column.type()).isEqualTo(String.class);
        assertThat(column.valueIn(Manufacturer.getDefaultInstance())).isEqualTo(description);
    }

    @Nested
    @DisplayName("prevent from passing")
    final class Prevent {

        @Test
        @DisplayName("empty or `null` column name into ctor")
        void emptyOrNullColumnName() {
            assertThrows(IllegalArgumentException.class,
                         () -> new RecordColumn<>("", String.class, (r) -> ""));

            assertThrows(NullPointerException.class,
                         () -> new RecordColumn<Manufacturer, String>(TestValues.<String>nullRef(),
                                                                      String.class,
                                                                      (r) -> ""));
        }

        @Test
        @DisplayName("`null` value type into ctor")
        void nullValueType() {
            assertThrows(NullPointerException.class,
                         () -> new RecordColumn<Manufacturer, String>("isin",
                                                                      nullRef(),
                                                                      (r) -> ""));
        }

        @Test
        @DisplayName("`null` getter into ctor")
        void nullGetter() {
            assertThrows(NullPointerException.class,
                         () -> new RecordColumn<Manufacturer, String>("isin",
                                                                      String.class,
                                                                      nullRef()));
        }
    }
}
