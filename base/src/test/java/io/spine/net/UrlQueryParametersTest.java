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

package io.spine.net;

import io.spine.net.Uri.QueryParameter;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UrlQueryParametersTest extends UtilityClassTest<UrlQueryParameters> {

    UrlQueryParametersTest() {
        super(UrlQueryParameters.class);
    }

    @Nested
    @DisplayName("fail on")
    class Fail {

        @Test
        @DisplayName("parsing wrong query")
        void wrongQuery() {
            assertThrows(
                    IllegalArgumentException.class,
                    () ->  UrlQueryParameters.parse("123")
            );
        }

        @Test
        @DisplayName("missing key")
        void missingKey() {
            assertThrows(
                    IllegalArgumentException.class,
                    () ->  UrlQueryParameters.from("", "123")
            );
        }

        @Test
        @DisplayName("missing value")
        void fail_on_missing_value() {
            assertThrows(
                    IllegalArgumentException.class,
                    () ->  UrlQueryParameters.from("123", "")
            );
        }
    }

    @Test
    @DisplayName("convert parameters")
    void convertParams() {
        String key = "keyOne";
        String value = "valueTwo";

        String query = key + '=' + value;

        QueryParameter parameter1 = UrlQueryParameters.parse(query);
        QueryParameter parameter2 = UrlQueryParameters.from(key, value);

        assertEquals(key, parameter1.getKey());
        assertEquals(value, parameter1.getValue());

        assertEquals(query, UrlQueryParameters.toString(parameter1));
        assertEquals(query, UrlQueryParameters.toString(parameter2));
    }
}
