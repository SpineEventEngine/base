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

import io.spine.test.validate.DistinctValues;
import io.spine.test.validate.DistinctValuesWithExternalConstraint;
import io.spine.validate.MessageValidatorTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.validate.MessageValidatorTest.MESSAGE_VALIDATOR_SHOULD;

@DisplayName(MESSAGE_VALIDATOR_SHOULD + "analyze (distinct) option and")
final class DistinctTest extends MessageValidatorTest {

    @DisplayName("find out that empty message does not violate the contract")
    @Test
    void findOutThatEmptyMessageDoesNotViolateContract() {
        assertValid(DistinctValues.getDefaultInstance());
    }

    @Nested
    @DisplayName("find out that no duplicates do not violate contract")
    final class NotViolates {

        @Test
        void enums() {
            DistinctValues msg = DistinctValues
                    .newBuilder()
                    .addEnums(DistinctValues.Planet.EARTH)
                    .addEnums(DistinctValues.Planet.MARS)
                    .addEnums(DistinctValues.Planet.JUPITER)
                    .build();
            assertValid(msg);
        }

        @Test
        void ints() {
            DistinctValues msg = DistinctValues
                    .newBuilder()
                    .addInts(1)
                    .addInts(2)
                    .addInts(3)
                    .build();
            assertValid(msg);
        }

        @Test
        void strings() {
            DistinctValues msg = DistinctValues
                    .newBuilder()
                    .addStrings("First")
                    .addStrings("Second")
                    .addStrings("Third")
                    .build();
            assertValid(msg);
        }

        @Test
        void messages() {
            DistinctValues msg = DistinctValues
                    .newBuilder()
                    .addMessages(customMessageOf(1))
                    .addMessages(customMessageOf(2))
                    .addMessages(customMessageOf(3))
                    .build();
            assertValid(msg);
        }
    }

    @Nested
    @DisplayName("find out that duplicate value violates contract")
    final class DuplicateViolates {

        @Disabled("See https://github.com/SpineEventEngine/base/issues/437")
        @Test
        void enums() {
            DistinctValues msg = DistinctValues
                    .newBuilder()
                    .addEnums(DistinctValues.Planet.EARTH)
                    .addEnums(DistinctValues.Planet.EARTH)
                    .addEnums(DistinctValues.Planet.JUPITER)
                    .build();
            assertNotValid(msg);
        }

        @Test
        void ints() {
            DistinctValues msg = DistinctValues
                    .newBuilder()
                    .addInts(1)
                    .addInts(2)
                    .addInts(1)
                    .build();
            assertNotValid(msg);
        }

        @Test
        void strings() {
            DistinctValues msg = DistinctValues
                    .newBuilder()
                    .addStrings("First")
                    .addStrings("Second")
                    .addStrings("First")
                    .build();
            assertNotValid(msg);
        }

        @Test
        void messages() {
            DistinctValues msg = DistinctValues
                    .newBuilder()
                    .addMessages(customMessageOf(1))
                    .addMessages(customMessageOf(2))
                    .addMessages(customMessageOf(1))
                    .build();
            assertNotValid(msg);
        }
    }

    @Nested
    @DisplayName("find out that duplicate value does not violate external constraint contract")
    final class DuplicateDoesNotViolatesExternalConstraint {

        @Test
        void enums() {
            DistinctValues msg = DistinctValues
                    .newBuilder()
                    .addEnums(DistinctValues.Planet.EARTH)
                    .addEnums(DistinctValues.Planet.EARTH)
                    .addEnums(DistinctValues.Planet.JUPITER)
                    .build();
            assertValid(withExternalConstraint(msg));
        }

        @Test
        void ints() {
            DistinctValues msg = DistinctValues
                    .newBuilder()
                    .addInts(1)
                    .addInts(2)
                    .addInts(1)
                    .build();
            assertValid(withExternalConstraint(msg));
        }

        @Test
        void strings() {
            DistinctValues msg = DistinctValues
                    .newBuilder()
                    .addStrings("First")
                    .addStrings("Second")
                    .addStrings("First")
                    .build();
            assertValid(withExternalConstraint(msg));
        }

        @Test
        void messages() {
            DistinctValues msg = DistinctValues
                    .newBuilder()
                    .addMessages(customMessageOf(1))
                    .addMessages(customMessageOf(2))
                    .addMessages(customMessageOf(1))
                    .build();
            assertValid(withExternalConstraint(msg));
        }

        private DistinctValuesWithExternalConstraint withExternalConstraint(DistinctValues value) {
            return DistinctValuesWithExternalConstraint
                    .newBuilder()
                    .setDistinctValues(value)
                    .build();
        }
    }

    private static DistinctValues.CustomMessage customMessageOf(long value) {
        return DistinctValues.CustomMessage
                .newBuilder()
                .setValue(value)
                .build();
    }
}
