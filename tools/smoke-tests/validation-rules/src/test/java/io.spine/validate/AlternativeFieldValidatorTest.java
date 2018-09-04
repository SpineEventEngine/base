/*
 * Copyright 2018, TeamDev. All rights reserved.
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

import com.google.protobuf.Descriptors.Descriptor;
import io.spine.test.validate.msg.altfields.MessageWithMissingField;
import io.spine.test.validate.msg.altfields.PersonName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("AlternativeFieldValidator should")
public class AlternativeFieldValidatorTest {

    private static final FieldContext EMPTY_CONTEXT = FieldContext.empty();

    private AlternativeFieldValidator validator;

    @BeforeEach
    public void setUp() {
        Descriptor descriptor = PersonName.getDescriptor();
        validator = new AlternativeFieldValidator(descriptor, EMPTY_CONTEXT);
    }

    @Test
    @DisplayName("pass if one field populated")
    public void pass_if_one_field_populated() {
        PersonName fieldPopulated = PersonName.newBuilder()
                                              .setFirstName("Alexander")
                                              .build();
        List<? extends ConstraintViolation> violations = validator.validate(fieldPopulated);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("pass if the combination is defined")
    public void pass_if_combination_defined() {
        PersonName combinationDefined = PersonName.newBuilder()
                                                  .setHonorificPrefix("Mr.")
                                                  .setLastName("Yevsyukov")
                                                  .build();
        List<? extends ConstraintViolation> violations = validator.validate(combinationDefined);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("fail if nothing is defined")
    public void fail_if_nothing_defined() {
        PersonName empty = PersonName.getDefaultInstance();
        List<? extends ConstraintViolation> violations = validator.validate(empty);
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("fail if defined is not required")
    public void fail_if_defined_not_required() {
        PersonName notRequiredPopulated = PersonName.newBuilder()
                                                    .setHonorificSuffix("I")
                                                    .build();
        List<? extends ConstraintViolation> violations = validator.validate(notRequiredPopulated);
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("report the missing field")
    public void report_missing_field() {
        AlternativeFieldValidator testee =
                new AlternativeFieldValidator(MessageWithMissingField.getDescriptor(),
                                              EMPTY_CONTEXT);
        MessageWithMissingField msg = MessageWithMissingField.newBuilder()
                                                             .setPresent(true)
                                                             .build();
        List<? extends ConstraintViolation> violations = testee.validate(msg);
        assertFalse(violations.isEmpty());
    }
}
