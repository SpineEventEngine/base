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

package io.spine.test.tools.validate;

import io.spine.people.PersonName;
import io.spine.validate.ConstraintViolation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;

@DisplayName("`(validate)` constraint should be compiled so that")
class ValidateConstraintTest {

    @Test
    @DisplayName("message fields are validated")
    void checkEnclosedFields() {
        DeliveryReceiver wrongAddress = DeliveryReceiver
                .newBuilder()
                .setName(PersonName.newBuilder()
                                   .setGivenName("Adam"))
                .setAddress(Address.newBuilder()
                                   .setSecondLine("Wall St. 1"))
                .buildPartial();
        List<ConstraintViolation> violations = wrongAddress.validate();
        assertThat(violations)
                .hasSize(1);
        ConstraintViolation wrapperViolation = violations.get(0);
        assertThat(wrapperViolation.getFieldPath()
                                   .getFieldName(0))
                .isEqualTo("address");
        List<ConstraintViolation> nestedViolations = wrapperViolation.getViolationList();
        assertThat(nestedViolations)
                .hasSize(2);
        assertThat(nestedViolations.get(0)
                                   .getFieldPath()
                                   .getFieldName(0))
                .isEqualTo("first_line");
        assertThat(nestedViolations.get(1)
                                   .getFieldPath()
                                   .getFieldName(0))
                .isEqualTo("town");
    }
}
