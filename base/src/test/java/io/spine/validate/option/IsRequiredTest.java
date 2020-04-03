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

package io.spine.validate.option;

import io.spine.test.validate.Fish;
import io.spine.test.validate.Meal;
import io.spine.test.validate.Sauce;
import io.spine.validate.ValidationOfConstraintTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.base.Identifier.newUuid;
import static io.spine.validate.ValidationOfConstraintTest.VALIDATION_SHOULD;

@DisplayName(VALIDATION_SHOULD + "analyze (is_required) oneof option and")
class IsRequiredTest extends ValidationOfConstraintTest {

    @Test
    @DisplayName("throw if required field group is not set")
    void required() {
        Meal message = Meal
                .newBuilder()
                .setCheese(Sauce.getDefaultInstance())
                .buildPartial();
        validate(message);
        assertThat(firstViolation().getFieldPath().getFieldName(0))
                .isEqualTo("choice");
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
}
