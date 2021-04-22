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

package io.spine.tools.code.proto;

import com.google.common.collect.ImmutableList;
import io.spine.testing.UtilityClassTest;
import io.spine.value.StringTypeValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.tools.code.proto.CamelCase.convert;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("CamelCase utility class should")
class CamelCaseTest extends UtilityClassTest<CamelCase> {

    CamelCaseTest() {
        super(CamelCase.class);
    }

    @Test
    @DisplayName("capitalize words")
    void capitalizeWords() {
        assertConverted("CapitalizeWords", "capitalize_words");
    }

    @Test
    @DisplayName("not lowercase words")
    void doNotLowercaseWords() {
        assertConverted("TestHTTPRequest", "test_HTTP_request");
    }

    private static void assertConverted(String expectedCamelCase, String underscoredName) {
        UnderscoredName name = new UnderName(underscoredName);
        assertEquals(expectedCamelCase, convert(name));
    }

    /**
     * A test value object.
     */
    private static class UnderName extends StringTypeValue implements UnderscoredName {

        private static final long serialVersionUID = 0L;

        private UnderName(String value) {
            super(value);
        }

        @Override
        public List<String> words() {
            return ImmutableList.copyOf(value().split(WORD_SEPARATOR));
        }
    }
}
