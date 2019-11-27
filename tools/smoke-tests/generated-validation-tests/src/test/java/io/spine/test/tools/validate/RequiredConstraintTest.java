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

import com.google.protobuf.ByteString;
import io.spine.base.Identifier;
import io.spine.protobuf.MessageWithConstraints;
import io.spine.validate.ConstraintViolation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.truth.Truth.assertThat;
import static java.util.stream.Collectors.toList;

@DisplayName("`(required)` constrained should be compiled so that")
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
                                       .setValue(Identifier.newUuid()))
                .buildPartial();
        checkNoViolation(singulars, "not_default");
    }

    private static void checkViolation(MessageWithConstraints message, String field) {
        List<ConstraintViolation> violations = message.validate();
        List<ConstraintViolation> stringViolations = violationAtField(violations, field);
        assertThat(stringViolations).hasSize(1);
        ConstraintViolation violation = stringViolations.get(0);
        assertThat(violation.getMsgFormat()).contains("must be set");
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
