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

import io.spine.test.validate.oneof.EveryOptional;
import io.spine.test.validate.oneof.EveryRequired;
import io.spine.test.validate.oneof.OneRequired;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.base.Identifier.newUuid;
import static io.spine.validate.MessageValidatorTest.MESSAGE_VALIDATOR_SHOULD;

@DisplayName(MESSAGE_VALIDATOR_SHOULD + "consider Oneof")
class OneofTest extends MessageValidatorTest {

    @Test
    @DisplayName("valid if a required field is set to a non-default value")
    void validIfRequireFieldIsNotDefault() {
        EveryRequired requiredIsNotDefault = EveryRequired
                .newBuilder()
                .setFirst(newUuid())
                .build();
        assertValid(requiredIsNotDefault);
    }

    @Test
    @DisplayName("invalid if a required field is set to the default value")
    void invalidIfRequireFieldIsDefault() {
        EveryRequired requiredIsDefault = EveryRequired
                .newBuilder()
                .setFirst("")
                .build();
        assertNotValid(requiredIsDefault);
    }

    @Test
    @DisplayName("valid if a non-required field is set to the default value")
    void validIfOptionalIsDefault() {
        OneRequired optionalIsDefault = OneRequired
                .newBuilder()
                .setOptional("")
                .build();
        assertValid(optionalIsDefault);
    }

    @Test
    @DisplayName("invalid if all fields are optional, but none is set")
    void invalidIfNoneIsSet() {
        EveryOptional noneIsSet = EveryOptional
                .newBuilder()
                .build();
        assertNotValid(noneIsSet);
    }
}
