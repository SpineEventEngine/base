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

package io.spine.validate.option;

import com.google.protobuf.ByteString;
import com.google.protobuf.util.Timestamps;
import io.spine.test.validate.Payment;
import io.spine.test.validate.PaymentData;
import io.spine.test.validate.PaymentId;
import io.spine.test.validate.PaymentWithExternalConstraint;
import io.spine.test.validate.WithFieldNotFound;
import io.spine.validate.ValidationOfConstraintTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.base.Identifier.newUuid;
import static io.spine.validate.ValidationOfConstraintTest.VALIDATION_SHOULD;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName(VALIDATION_SHOULD + "analyze (goes) option and find out that ")
final class GoesTest extends ValidationOfConstraintTest {

    @DisplayName("(goes).with fields are both optional")
    @Test
    void goesWithFieldsAreBothOptional() {
        Payment msg = Payment
                .newBuilder()
                .setDescription("Scheduled payment")
                .build();
        assertValid(msg);
    }

    @DisplayName("(goes).with fields are not filled simultaneously")
    @Test
    void goesWithFieldsShouldBeFilledSimultaneously() {
        PaymentId id = PaymentId
                .newBuilder()
                .setUuid(newUuid())
                .build();
        Payment withId = Payment
                .newBuilder()
                .setId(id)
                .build();
        assertNotValid(withId);

        Payment withTimestamp = Payment
                .newBuilder()
                .setTimestamp(Timestamps.MAX_VALUE)
                .build();
        assertNotValid(withTimestamp);
    }

    @DisplayName("(goes).with fields are filled simultaneously")
    @Test
    void goesWithFieldsAreFilledSimultaneously() {
        PaymentId id = PaymentId
                .newBuilder()
                .setUuid(newUuid())
                .build();
        Payment msg = Payment
                .newBuilder()
                .setId(id)
                .setTimestamp(Timestamps.MAX_VALUE)
                .build();
        assertValid(msg);
    }

    @DisplayName("(goes).with field is not found")
    @Test
    void findOutThatGoesWithFieldIsNotFound() {
        WithFieldNotFound msg = WithFieldNotFound
                .newBuilder()
                .setId(newUuid())
                .setAvatar(ByteString.copyFrom(new byte[]{0, 1, 2}))
                .build();
        Exception exception = assertThrows(IllegalStateException.class, () -> validate(msg));
        assertThat(exception)
                .hasCauseThat()
                .hasMessageThat()
                .contains("user_id");
    }

    @DisplayName("(goes).with is set as external constraint")
    @Test
    void findOutThatGoesWithIsSetAsExternalConstraint() {
        PaymentId id = PaymentId
                .newBuilder()
                .setUuid(newUuid())
                .build();
        PaymentData data = PaymentData
                .newBuilder()
                .setTimestamp(Timestamps.MAX_VALUE)
                .build();
        PaymentWithExternalConstraint msg = PaymentWithExternalConstraint
                .newBuilder()
                .setId(id)
                .setData(data)
                .build();
        assertNotValid(msg);
    }
}
