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

package io.spine.test.tools.validate;

import com.google.protobuf.Message;
import io.spine.validate.ConstraintViolation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.base.Identifier.newUuid;
import static io.spine.validate.Validate.violationsOf;

@DisplayName("`(is_required)` constraint should be compiled so that")
class IsRequiredTest {

    @Test
    @DisplayName("throw if required field group is not set")
    void required() {
        Meal message = Meal
                .newBuilder()
                .setCheese(Sauce.getDefaultInstance())
                .buildPartial();
        List<ConstraintViolation> violations = violationsOf(message);
        assertThat(violations)
                .hasSize(1);
        assertThat(violations.get(0).getMsgFormat())
                .contains("choice");
    }

    @Test
    @DisplayName("not throw if required field group is set")
    void requiredSet() {
        Fish fish = Fish
                .newBuilder()
                .setDescription(newUuid())
                .build();
        Meal message = Meal
                .newBuilder()
                .setCheese(Sauce.getDefaultInstance())
                .setFish(fish)
                .buildPartial();
        assertValid(message);
    }

    @Test
    @DisplayName("ignore non-required field groups")
    void notRequired() {
        Fish fish = Fish
                .newBuilder()
                .setDescription(newUuid())
                .build();
        Meal message = Meal
                .newBuilder()
                .setFish(fish)
                .buildPartial();
        assertValid(message);
    }

    private static void assertValid(Message message) {
        assertThat(violationsOf(message))
                .isEmpty();
    }
}
