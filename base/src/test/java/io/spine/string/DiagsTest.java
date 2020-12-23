/*
 * Copyright 2020, TeamDev. All rights reserved.
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
import com.google.common.truth.StringSubject;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.string.Diags.COMMA_AND_SPACE;
import static io.spine.string.Diags.backtick;
import static io.spine.string.Diags.toEnumeration;
import static io.spine.string.Diags.toEnumerationBackticked;

@DisplayName("`Diags` utility should")
class DiagsTest extends UtilityClassTest<Diags> {

    DiagsTest() {
        super(Diags.class);
    }

    @Test
    @DisplayName("backtick string representation of an object")
    void backticks() {
        Object anObject = getClass();
        String backticked = backtick(anObject);

        StringSubject assertOutput = assertThat(backticked);
        assertOutput.startsWith("`");
        assertOutput.endsWith("`");
        assertOutput.contains(anObject.toString());
    }

    @Nested
    @DisplayName("join")
    class Joining {

        @Test
        @DisplayName("`Iterable`")
        void iterable() {
            List<String> items = ImmutableList.of("one", "two", "tree");
            String joined = Diags.join(items);

            StringSubject assertOutput = assertThat(joined);
            assertOutput.contains(COMMA_AND_SPACE);
            items.forEach(assertOutput::contains);
        }

        @Test
        @DisplayName("vararg")
        void varArg() {
            String joined = Diags.join("uno", "dos", "tres");

            StringSubject assertOutput = assertThat(joined);
            ImmutableList.of("uno", "dos", "tres")
                         .forEach(assertOutput::contains);
        }

        @Test
        @DisplayName("separating with comma followed by space char")
        void commaThenSpace() {
            String joined = Diags.join(100, 200, 300);

            StringSubject assertOutput = assertThat(joined);
            assertOutput.contains(COMMA_AND_SPACE);
            ImmutableList.of(100, 200, 300)
                         .forEach(item -> assertOutput.contains(item.toString()));
        }
    }

    @Nested
    @DisplayName("provide collector to comma-separated string")
    class Collectors {

        private final ImmutableList<String> list = ImmutableList.of("foo", "bar", "baz");
        private StringSubject assertOutput;

        @Test
        @DisplayName("with items")
        void stringEnumeration() {
            String output = list.stream()
                                .collect(toEnumeration());

            assertOutput = assertThat(output);

            assertOutput.contains(COMMA_AND_SPACE);
            list.forEach(assertOutput::contains);
        }

        @Test
        @DisplayName("with backticked items")
        void backtickedEnumeration() {
            String output = list.stream()
                                .collect(toEnumerationBackticked());

            assertOutput = assertThat(output);

            assertOutput.contains(COMMA_AND_SPACE);
            list.forEach(item -> assertOutput.contains(backtick(item)));
        }
    }
}
