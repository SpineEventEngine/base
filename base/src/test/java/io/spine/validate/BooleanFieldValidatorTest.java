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

import com.google.protobuf.Any;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.code.proto.FieldContext;
import io.spine.test.validate.RequiredBooleanFieldValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.logging.Level;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DisplayName("BooleanFieldValidator should")
class BooleanFieldValidatorTest {

    private final FieldDescriptor fieldDescriptor = Any.getDescriptor()
                                                       .getFields()
                                                       .get(0);
    private final FieldContext fieldContext = FieldContext.create(fieldDescriptor);
    private final BooleanFieldValidator validator =
            new BooleanFieldValidator(FieldValue.of(false, fieldContext));

    @Test
    @DisplayName("convert string to number")
    void convertStringToNumber() {
        assertFalse(validator.isNotSet(false));
    }

    @Nested
    class RequiredBooleanFieldWarning extends RequiredFieldWarningTest {

        @BeforeEach
        void validate() {
            FieldDescriptor descriptor = RequiredBooleanFieldValue
                    .getDescriptor()
                    .getFields()
                    .get(0);
            FieldContext context = FieldContext.create(descriptor);
            FieldValue<Boolean> fieldValue = FieldValue.of(true, context);
            BooleanFieldValidator validator = new BooleanFieldValidator(fieldValue);

            List<ConstraintViolation> violations = validator.validate();
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("produce a warning upon finding a required boolean field")
        void warning() {
            assertLog().record()
                       .hasLevelThat()
                       .isEqualTo(Level.WARNING);
        }
    }
}
