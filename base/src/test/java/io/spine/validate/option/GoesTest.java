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

package io.spine.validate.option;

import com.google.protobuf.util.Timestamps;
import io.spine.test.validate.Payment;
import io.spine.test.validate.PaymentId;
import io.spine.validate.MessageValidatorTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.base.Identifier.newUuid;
import static io.spine.validate.MessageValidatorTest.MESSAGE_VALIDATOR_SHOULD;

@DisplayName(MESSAGE_VALIDATOR_SHOULD + "analyze (goes) option and")
final class GoesTest extends MessageValidatorTest {

    @DisplayName("(goes).with fields are both optional")
    @Test
    void findOutThatGoesWithFieldsAreBothOptional() {
        Payment msg = Payment
                .newBuilder()
                .setDescription("Scheduled payment")
                .build();
        assertValid(msg);
    }

    @Disabled
    @DisplayName("(goes).with fields should be filled simultaneously")
    @Test
    void findOutThatGoesWithFieldsShouldBeFilledSimultaneously() {
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
    void findOutThatGoesWithFieldsAreFilledSimultaneously() {
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
}
