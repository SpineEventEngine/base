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
import io.spine.logging.Logging;
import io.spine.test.validate.RequiredBooleanFieldValue;
import io.spine.validate.option.Required;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.event.SubstituteLoggingEvent;
import org.slf4j.helpers.SubstituteLogger;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.slf4j.event.Level.WARN;

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

    @Test
    @DisplayName("produce a warning upon finding a required boolean field")
    void testRequiredBooleanFieldWarning() {
        FieldDescriptor descriptor = RequiredBooleanFieldValue
                .getDescriptor()
                .getFields()
                .get(0);
        FieldContext context = FieldContext.create(descriptor);
        FieldValue<Boolean> fieldValue = FieldValue.of(true, context);
        BooleanFieldValidator validator = new BooleanFieldValidator(fieldValue);

        Queue<SubstituteLoggingEvent> loggedMessages = new ArrayDeque<>();
        Logging.redirect((SubstituteLogger) Logging.get(Required.class), loggedMessages);
        List<ConstraintViolation> violations = validator.validate();

        assertTrue(violations.isEmpty());
        assertEquals(1, loggedMessages.size());
        assertEquals(WARN, loggedMessages.peek().getLevel());
    }
}
