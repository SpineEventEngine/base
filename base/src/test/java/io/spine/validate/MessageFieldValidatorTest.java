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

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import io.spine.protobuf.Timestamps2;
import io.spine.test.validate.CustomMessageFieldSetOnceExplicitlyFalse;
import io.spine.test.validate.CustomMessageFieldSetOncePresent;
import io.spine.test.validate.InvalidMessage;
import io.spine.test.validate.MessageWithMapMessageField;
import io.spine.test.validate.MessageWithRepeatedRequiredValidatedMessageField;
import io.spine.test.validate.MessageWithRepeatedUnchekedMessageField;
import io.spine.test.validate.MessageWithRepeatedValidatedMessageField;
import io.spine.test.validate.MessegeWithRepeatedRequiredMessageField;
import org.junit.jupiter.api.DisplayName;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@DisplayName("MessageFieldValidator should")
public class MessageFieldValidatorTest extends FieldValidatorTest<Message> {

    private static final FieldDescriptor SET_ONCE_FALSE_TIMESTAMP_FIELD_DESC =
            CustomMessageFieldSetOnceExplicitlyFalse.getDescriptor()
                                                    .getFields()
                                                    .get(0);

    private static final FieldDescriptor SET_ONCE_TIMESTAMP_FIELD_DESC =
            CustomMessageFieldSetOncePresent.getDescriptor()
                                            .getFields()
                                            .get(0);

    private static final FieldDescriptor UNCHECKED_FIELD_DESC =
            MessageWithRepeatedUnchekedMessageField.getDescriptor()
                                                   .getFields()
                                                   .get(0);

    private static final FieldDescriptor VALIDATED_FIELD_DESC =
            MessageWithRepeatedValidatedMessageField.getDescriptor()
                                                    .getFields()
                                                    .get(0);

    private static final FieldDescriptor REQUIRED_FIELD_DESC =
            MessegeWithRepeatedRequiredMessageField.getDescriptor()
                                                   .getFields()
                                                   .get(0);

    private static final FieldDescriptor VALIDATED_REQUIRED_FIELD_DESC =
            MessageWithRepeatedRequiredValidatedMessageField.getDescriptor()
                                                            .getFields()
                                                            .get(0);

    private static final FieldDescriptor MAP_FIELD_DESC = MessageWithMapMessageField.getDescriptor()
                                                                                    .getFields()
                                                                                    .get(0);

    @Override
    protected MessageFieldValidator validatedRequiredRepeatedFieldValidator(List<Message> values) {
        return getValidator(VALIDATED_REQUIRED_FIELD_DESC, values);
    }

    @Override
    protected MessageFieldValidator requiredRepeatedFieldValidator(List<Message> values) {
        return getValidator(REQUIRED_FIELD_DESC, values);
    }

    @Override
    protected MessageFieldValidator validatedRepeatedFieldValidator(List<Message> values) {
        return getValidator(VALIDATED_FIELD_DESC, values);
    }

    @Override
    protected MessageFieldValidator uncheckedRepeatedFieldValidator(List<Message> values) {
        return getValidator(UNCHECKED_FIELD_DESC, values);
    }

    @Override
    protected FieldValidator<Message> emptyMapFieldValidator() {
        return getValidator(MAP_FIELD_DESC, Collections.emptyMap());
    }

    @Override
    protected FieldValidator<Message> setOnceViolatedValidator() {
        Timestamp previousValue = now();
        Timestamp desiredValue = previousValue.toBuilder()
                                              .setNanos(60)
                                              .build();
        return getValidatorForChangedField(SET_ONCE_TIMESTAMP_FIELD_DESC,
                                           previousValue,
                                           desiredValue);
    }

    @Override
    protected FieldValidator<?> validSetOnceValidator() {
        Timestamp now = now();
        return getValidator(SET_ONCE_TIMESTAMP_FIELD_DESC, now);
    }

    @Override
    protected FieldValidator<?> setOnceFalseValidator() {
        Timestamp now = now();
        Timestamp later = now.toBuilder()
                             .setNanos(60)
                             .build();
        return getValidatorForChangedField(SET_ONCE_FALSE_TIMESTAMP_FIELD_DESC, now, later);
    }

    @Override
    protected FieldValidator<?> setOnceChangeToSameValueValidator() {
        Timestamp now = now();
        return getValidatorForChangedField(SET_ONCE_TIMESTAMP_FIELD_DESC, now, now);
    }

    @Override
    protected InvalidMessage newValue() {
        return InvalidMessage.newBuilder()
                             .setInvalidField("some non-empty string")
                             .build();
    }

    @Override
    protected InvalidMessage defaultValue() {
        return InvalidMessage.getDefaultInstance();
    }

    private static MessageFieldValidator getValidator(FieldDescriptor field,
                                                      Object rawValue) {
        FieldContext context = FieldContext.create(field);
        return new MessageFieldValidator(FieldValue.of(rawValue, context), false);
    }

    private static MessageFieldValidator getValidatorForChangedField(FieldDescriptor field,
                                                                     Object previousValue,
                                                                     Object desiredValue) {
        FieldContext fieldContext = FieldContext.create(field);
        FieldValue previous = FieldValue.of(previousValue, fieldContext);
        FieldValue desired = FieldValue.of(desiredValue, fieldContext);
        FieldValueChange change = FieldValueChange.of(previous, desired);
        return new MessageFieldValidator(change, false);
    }

    private Timestamp now() {
        return Timestamps2.fromInstant(Instant.now());
    }
}
