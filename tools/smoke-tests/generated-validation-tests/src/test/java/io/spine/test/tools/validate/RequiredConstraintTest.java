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

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import io.spine.protobuf.MessageWithConstraints;
import io.spine.test.tools.validate.command.CreateProject;
import io.spine.test.tools.validate.entity.Project;
import io.spine.test.tools.validate.entity.Task;
import io.spine.test.tools.validate.event.ProjectCreated;
import io.spine.validate.ConstraintViolation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.truth.Truth.assertThat;
import static io.spine.base.Identifier.newUuid;
import static java.util.stream.Collectors.toList;

@DisplayName("`(required)` constraint should be compiled so that")
class RequiredConstraintTest {

    @Test
    @DisplayName("numbers can have any value")
    void ignoreNumbers() {
        Singulars singulars = Singulars
                .newBuilder()
                .buildPartial();
        checkNoViolation(singulars, "required_numbers_are_not_validated");
    }

    @Test
    @DisplayName("empty strings are violations")
    void checkString() {
        Singulars singulars = Singulars
                .newBuilder()
                .buildPartial();
        checkViolation(singulars, "not_empty_string");
    }

    @Test
    @DisplayName("non-empty strings are accepted")
    void acceptNonEmptyString() {
        Singulars singulars = Singulars
                .newBuilder()
                .setNotEmptyString(" ")
                .buildPartial();
        checkNoViolation(singulars, "not_empty_string");
    }

    @Test
    @DisplayName("empty byte strings are violations")
    void checkBytes() {
        Singulars singulars = Singulars
                .newBuilder()
                .buildPartial();
        checkViolation(singulars, "one_or_more_bytes");
    }

    @Test
    @DisplayName("non-empty byte strings are accepted")
    void acceptNonEmptyByteString() {
        Singulars singulars = Singulars
                .newBuilder()
                .setOneOrMoreBytes(ByteString.copyFrom("non-empty", UTF_8))
                .buildPartial();
        checkNoViolation(singulars, "one_or_more_bytes");
    }

    @Test
    @DisplayName("zero-value enums are violations")
    void checkEnum() {
        Singulars singulars = Singulars
                .newBuilder()
                .setNotVegetable(UltimateChoice.VEGETABLE)
                .buildPartial();
        checkViolation(singulars, "not_vegetable");
    }

    @Test
    @DisplayName("not-default enums are accepted")
    void acceptNonDefaultEnum() {
        Singulars singulars = Singulars
                .newBuilder()
                .setNotVegetable(UltimateChoice.CHICKEN)
                .buildPartial();
        checkNoViolation(singulars, "not_vegetable");
    }

    @Test
    @DisplayName("default message instances are violations")
    void checkMessage() {
        Singulars singulars = Singulars
                .newBuilder()
                .buildPartial();
        checkViolation(singulars, "not_default");
    }

    @Test
    @DisplayName("not-default messages are accepted")
    void acceptNonDefaultMessage() {
        Singulars singulars = Singulars
                .newBuilder()
                .setNotDefault(Enclosed.newBuilder()
                                       .setValue(newUuid()))
                .buildPartial();
        checkNoViolation(singulars, "not_default");
    }

    @Test
    @DisplayName("google.protobuf.Empty cannot be a required field")
    void notAllowEmptyRequired() {
        AlwaysInvalid unset = AlwaysInvalid
                .newBuilder()
                .build();
        checkViolation(unset, "impossible");
        AlwaysInvalid set = AlwaysInvalid
                .newBuilder()
                .setImpossible(Empty.getDefaultInstance())
                .build();
        checkViolation(set, "impossible");
    }

    @Test
    @DisplayName("all violations on a single message are collected")
    void collectManyViolations() {
        Singulars instance = Singulars.getDefaultInstance();
        List<ConstraintViolation> violations = instance.validate();
        assertThat(violations).hasSize(4);
    }

    @Test
    @DisplayName("repeated number fields must have at least one value")
    void emptyRepeatedInt() {
        Collections instance = Collections.getDefaultInstance();
        checkViolation(instance, "not_empty_list_of_longs");
    }

    @Test
    @DisplayName("elements of repeated number fields do not matter")
    void repeatedInt() {
        Collections instance = Collections
                .newBuilder()
                .addNotEmptyListOfLongs(0L)
                .buildPartial();
        checkNoViolation(instance,"not_empty_list_of_longs");
    }

    @Test
    @DisplayName("map number fields must have at least one value")
    void emptyMapOfInts() {
        Collections instance = Collections.getDefaultInstance();
        checkViolation(instance, "not_empty_map_of_ints");
    }

    @Test
    @DisplayName("elements of map number fields do not matter")
    void mapOfInts() {
        Collections instance = Collections
                .newBuilder()
                .putNotEmptyMapOfInts(0, 0)
                .buildPartial();
        checkNoViolation(instance,"not_empty_map_of_ints");
    }

    @Test
    @DisplayName("map string fields must have at least one value")
    void emptyMapOfStrings() {
        Collections instance = Collections.getDefaultInstance();
        checkViolation(instance, "contains_a_non_empty_string_value");
    }

    @Test
    @DisplayName("map string fields must have at least one non-empty value")
    void mapOfEmptyStrings() {
        Collections instance = Collections
                .newBuilder()
                .putContainsANonEmptyStringValue("", "")
                .buildPartial();
        checkViolation(instance,"contains_a_non_empty_string_value");
    }

    @Test
    @DisplayName("a non-empty map of non-empty strings is accepted")
    void mapOfStrings() {
        Collections instance = Collections
                .newBuilder()
                .putContainsANonEmptyStringValue("", " ")
                .buildPartial();
        checkNoViolation(instance,"contains_a_non_empty_string_value");
    }

    @Test
    @DisplayName("an empty repeated field of enums is a violation")
    void emptyRepeatedEnum() {
        Collections instance = Collections.getDefaultInstance();
        checkViolation(instance, "at_least_one_piece_of_meat");
    }

    @Test
    @DisplayName("an repeated field of default enums is a violation")
    void repeatedDefaultEnum() {
        Collections instance = Collections
                .newBuilder()
                .addAtLeastOnePieceOfMeat(UltimateChoice.VEGETABLE)
                .addAtLeastOnePieceOfMeat(UltimateChoice.VEGETABLE)
                .buildPartial();
        checkViolation(instance, "at_least_one_piece_of_meat");
    }

    @Test
    @DisplayName("an repeated field of enums is accepted")
    void repeatedEnum() {
        Collections instance = Collections
                .newBuilder()
                .addAtLeastOnePieceOfMeat(UltimateChoice.FISH)
                .addAtLeastOnePieceOfMeat(UltimateChoice.VEGETABLE)
                .buildPartial();
        checkNoViolation(instance, "at_least_one_piece_of_meat");
    }

    @Nested
    @DisplayName("when checking an ID")
    class IdCheck {

        @Test
        @DisplayName("in an event, no violations if ID is not set")
        void event() {
            ProjectCreated msg = ProjectCreated
                    .newBuilder()
                    .build();
            assertThat(msg.validate()).isEmpty();
        }

        @Test
        @DisplayName("in a command, no violations if ID is present")
        void command() {
            CreateProject msg = CreateProject
                    .newBuilder()
                    .setId(newUuid())
                    .build();
            assertThat(msg.validate()).isEmpty();
        }

        @Test
        @DisplayName("in a command, a violation must be produced if ID is not set")
        void invalidCommand() {
            CreateProject msg = CreateProject
                    .newBuilder()
                    .buildPartial();
            checkViolation(msg, "id");
        }

        @Test
        @DisplayName("in a rejection, no violations if ID is not set")
        void rejection() {
            TestRejections.CannotCreateProject msg = TestRejections.CannotCreateProject
                    .newBuilder()
                    .build();
            assertThat(msg.validate()).isEmpty();
        }

        @Test
        @DisplayName("in an entity state, no violations if ID is present")
        void state() {
            Project msg = Project
                    .newBuilder()
                    .setId(newUuid())
                    .build();
            assertThat(msg.validate()).isEmpty();
        }

        @Test
        @DisplayName("in an entity state, a violation must be produced if ID is not set")
        void invalidState() {
            Project msg = Project
                    .newBuilder()
                    .buildPartial();
            checkViolation(msg, "id");
        }

        @Test
        @DisplayName("if stated explicitly, ID should not be required")
        void notRequired() {
            Task msg = Task
                    .newBuilder()
                    .build();
            assertThat(msg.validate()).isEmpty();
        }
    }

    private static void checkViolation(MessageWithConstraints message, String field) {
        checkViolation(message, field, "must be set");
    }

    private static void checkViolation(MessageWithConstraints message,
                                       String field,
                                       String errorMessagePart) {
        List<ConstraintViolation> violations = message.validate();
        List<ConstraintViolation> stringViolations = violationAtField(violations, field);
        assertThat(stringViolations).hasSize(1);
        ConstraintViolation violation = stringViolations.get(0);
        assertThat(violation.getMsgFormat()).contains(errorMessagePart);
    }

    private static void checkNoViolation(MessageWithConstraints message, String field) {
        List<ConstraintViolation> violations = message.validate();
        List<ConstraintViolation> stringViolations = violationAtField(violations, field);
        assertThat(stringViolations).isEmpty();
    }

    private static List<ConstraintViolation>
    violationAtField(List<ConstraintViolation> violations, String fieldName) {
        return violations
                .stream()
                .filter(violation -> violation.getFieldPath()
                                              .getFieldName(0)
                                              .equals(fieldName))
                .collect(toList());
    }
}
