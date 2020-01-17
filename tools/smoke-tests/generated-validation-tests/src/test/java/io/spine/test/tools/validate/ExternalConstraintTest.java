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

package io.spine.test.tools.validate;

import com.google.common.collect.ImmutableList;
import io.spine.validate.ConstraintViolation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

@DisplayName("Generated code should")
class ExternalConstraintTest {

    @Test
    @DisplayName("call external validation")
    void validateExternal() {
        User user = User.newBuilder()
                        .addContact(Email.newBuilder()
                                         .setValue("not an email"))
                        .buildPartial();
        ImmutableList<ConstraintViolation> violations = user.validate();
        assertThat(violations)
                .hasSize(1);
        assertThat(violations.get(0)
                             .getFieldPath()
                             .getFieldName(0))
                .isEqualTo("contact");
    }

    @Test
    @DisplayName("invoke generated validation if no external validation is defined")
    void noExternal() {
        ShippingAddress address = ShippingAddress
                .newBuilder()
                .setSecondLine("first line is required and not set")
                .buildPartial();
        User user = User.newBuilder()
                        .addShippingAddress(address)
                        .buildPartial();
        ImmutableList<ConstraintViolation> violations = user.validate();
        assertThat(violations)
                .hasSize(1);
        assertThat(violations.get(0)
                             .getFieldPath()
                             .getFieldName(0))
                .isEqualTo("shipping_address");
    }

    @Test
    @DisplayName("ignore external constraints if `(validate)` is not set")
    void noValidate() {
        PersonName name = PersonName.newBuilder()
                                 .setValue("A")
                                 .buildPartial();
        User user = User.newBuilder()
                        .setName(name)
                        .buildPartial();
        assertThat(user.validate())
                .isEmpty();
    }
}
