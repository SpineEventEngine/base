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

import com.google.protobuf.Message;
import io.spine.test.validate.altfields.MessageWithMissingField;
import io.spine.test.validate.altfields.PersonName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests handling of {@code required_field} proto option.
 */
@DisplayName("AlternativeFieldValidator should")
class AlternativeFieldValidatorTest {

    @Test
    @DisplayName("pass if one field populated")
    void oneFieldPopulated() {
        PersonName fieldPopulated = PersonName
                .newBuilder()
                .setFirstName("Alexander")
                .build();
        assertValid(fieldPopulated);
    }

    @Test
    @DisplayName("pass if combination defined")
    void combinationDefined() {
        PersonName combinationDefined = PersonName
                .newBuilder()
                .setHonorificPrefix("Mr.")
                .setLastName("Yevsyukov")
                .build();
        assertValid(combinationDefined);
    }

    @Test
    @DisplayName("fail if nothing defined")
    void nothingDefined() {
        PersonName empty = PersonName.getDefaultInstance();
        assertNotValid(empty);
    }

    @Test
    @DisplayName("fail if defined is not required")
    void definedNotRequired() {
        PersonName notRequiredPopulated = PersonName
                .newBuilder()
                .setHonorificSuffix("I")
                .build();
        assertNotValid(notRequiredPopulated);
    }

    @Test
    @DisplayName("report missing fields")
    void missingFields() {
        MessageWithMissingField msg = MessageWithMissingField
                .newBuilder()
                .setPresent(true)
                .build();
        assertNotValid(msg);
    }

    private static void assertValid(Message message) {
        assertValid(message, true);
    }

    private static void assertNotValid(Message message) {
        assertValid(message, false);
    }

    private static void assertValid(Message message, boolean valid) {
        MessageValue value = MessageValue.atTopLevel(message);
        AlternativeFieldValidator validator = new AlternativeFieldValidator(value);
        List<ConstraintViolation> violations = validator.validate();
        assertEquals(valid, violations.isEmpty());
    }
}
