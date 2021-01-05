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

package io.spine.value;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.testing.SerializableTester.reserializeAndAssert;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("StringTypeValue should")
class StringTypeValueTest {

    @SuppressWarnings("SerializableInnerClassWithNonSerializableOuterClass")
    @Test
    @DisplayName("return value")
    void getValue() {
        String expected = "return_value_in_toString";

        StringTypeValue value = new StringTypeValue(expected) {
            private static final long serialVersionUID = 0L;
        };

        assertEquals(expected, value.toString());
    }

    @Test
    @DisplayName("have hashCode() and equals()")
    void hashCodeAndEquals() {
        new EqualsTester().addEqualityGroup(new StrVal("uno"), new StrVal("uno"))
                          .addEqualityGroup(new StrVal("dos"))
                          .testEquals();
    }

    @Test
    @DisplayName("tell if empty")
    void isEmpty() {
        assertTrue(new StrVal("").isEmpty());
        assertFalse(new StrVal(" ").isEmpty());
    }

    @Test
    @DisplayName("be Serializable")
    void serialize() {
        reserializeAndAssert(new StrVal(getClass().getName()));
    }

    /** Simple descendant for testing. */
    private static class StrVal extends StringTypeValue {

        private static final long serialVersionUID = 0L;

        StrVal(String value) {
            super(value);
        }
    }
}
