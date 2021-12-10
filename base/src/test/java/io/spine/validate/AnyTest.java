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

package io.spine.validate;

import io.spine.protobuf.AnyPacker;
import io.spine.test.validate.RequiredMsgFieldValue;
import io.spine.test.validate.anyfields.AnyContainer;
import io.spine.test.validate.anyfields.UncheckedAnyContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.validate.ValidationOfConstraintTest.VALIDATION_SHOULD;
import static io.spine.validate.given.MessageValidatorTestEnv.newStringValue;

@DisplayName(VALIDATION_SHOULD + "when validating `google.protobuf.Any`")
class AnyTest extends ValidationOfConstraintTest {

    @Test
    @DisplayName("consider `Any` valid if content is valid")
    void considerAnyValidIfContentIsValid() {
        var value = RequiredMsgFieldValue.newBuilder()
                .setValue(newStringValue())
                .build();
        var content = AnyPacker.pack(value);
        var container = AnyContainer.newBuilder()
                .setAny(content)
                .build();
        assertValid(container);
    }

    @Test
    @DisplayName("consider `Any` not valid if content is not valid")
    void considerAnyNotValidIfContentIsNotValid() {
        var value = RequiredMsgFieldValue.getDefaultInstance();
        var content = AnyPacker.pack(value);
        var container = AnyContainer.newBuilder()
                .setAny(content)
                .build();
        assertNotValid(container);
    }

    @Test
    @DisplayName("consider `Any` valid if validation is not required")
    void considerAnyValidIfValidationIsNotRequired() {
        var value = RequiredMsgFieldValue.getDefaultInstance();
        var content = AnyPacker.pack(value);
        var container = UncheckedAnyContainer.newBuilder()
                .setAny(content)
                .build();
        assertValid(container);
    }

    @Test
    @DisplayName("validate recursive messages")
    void validateRecursiveMessages() {
        var value = RequiredMsgFieldValue.getDefaultInstance();
        var internalAny = AnyPacker.pack(value);
        var internal = AnyContainer.newBuilder()
                .setAny(internalAny)
                .build();
        var externalAny = AnyPacker.pack(internal);
        var external = AnyContainer.newBuilder()
                .setAny(externalAny)
                .build();
        assertNotValid(external);
    }
}
