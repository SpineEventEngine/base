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

import com.google.protobuf.util.Timestamps;
import io.spine.base.Time;
import io.spine.test.validate.Payment;
import io.spine.test.validate.PaymentData;
import io.spine.test.validate.PaymentId;
import io.spine.testing.TestValues;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.base.Identifier.newUuid;

@DisplayName("GoesWithValidator should")
final class GoesWithValidatorTest {

    @DisplayName("ignore messages without (goes) option")
    @Test
    void ignoreMessagesWithoutGoesOption() {
        PaymentData msg = PaymentData
                .newBuilder()
                .setDescription(TestValues.randomString())
                .setTimestamp(Time.currentTime())
                .build();
        GoesWithValidator validator = new GoesWithValidator(MessageValue.atTopLevel(msg));
        assertThat(validator.validate()).isEmpty();
    }

    @DisplayName("validate all (goes) field options and produce no constraint violationsOf")
    @Test
    void validateAllGoesFieldOptionsAndProduceNoViolations() {
        PaymentId id = PaymentId
                .newBuilder()
                .setUuid(newUuid())
                .build();
        Payment msg = Payment
                .newBuilder()
                .setId(id)
                .setTimestamp(Timestamps.MAX_VALUE)
                .build();
        GoesWithValidator validator = new GoesWithValidator(MessageValue.atTopLevel(msg));
        assertThat(validator.validate()).isEmpty();
    }

    @DisplayName("validate all (goes) field options and produce violationsOf")
    @Test
    void validateAllGoesFieldOptionsAndProduceViolations() {
        PaymentId id = PaymentId
                .newBuilder()
                .setUuid(newUuid())
                .build();
        Payment msg = Payment
                .newBuilder()
                .setId(id)
                .build();
        GoesWithValidator validator = new GoesWithValidator(MessageValue.atTopLevel(msg));
        assertThat(validator.validate()).hasSize(1);
    }
}
