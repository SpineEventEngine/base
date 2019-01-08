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
import io.spine.test.validate.FirstRuleTarget;
import io.spine.test.validate.InvalidMessage;
import io.spine.test.validate.RuleTargetAggregate;
import io.spine.test.validate.SecondRuleTarget;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("MessageValidator should")
class MessageValidatorTest {

    private List<ConstraintViolation> violations;

    @Test
    @DisplayName("validate according to validation rules")
    void validate_according_to_validation_rule() {
        String validValue = "any text";
        InvalidMessage invalidMessage = InvalidMessage
                .newBuilder()
                .setInvalidField(validValue)
                .build();
        FirstRuleTarget first = FirstRuleTarget
                .newBuilder()
                .setCanBeValid(invalidMessage)
                .build();
        SecondRuleTarget second = SecondRuleTarget
                .newBuilder()
                .setCanBeValid(invalidMessage)
                .build();
        RuleTargetAggregate aggregateState = RuleTargetAggregate
                .newBuilder()
                .setFirst(first)
                .setSecond(second)
                .build();
        validate(aggregateState);
        assertIsValid();
    }

    private void validate(Message msg) {
        MessageValidator validator = MessageValidator.newInstance(msg);
        violations = validator.validate();
    }

    private void assertIsValid() {
        assertTrue(violations.isEmpty());
    }
}
