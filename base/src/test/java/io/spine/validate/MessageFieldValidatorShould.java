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
import io.spine.test.validate.InvalidMessage;
import io.spine.test.validate.MessageWithMapMessageField;
import io.spine.test.validate.MessageWithRepeatedRequiredValidatedMessageField;
import io.spine.test.validate.MessageWithRepeatedUnchekedMessageField;
import io.spine.test.validate.MessageWithRepeatedValidatedMessageField;
import io.spine.test.validate.MessegeWithRepeatedRequiredMessageField;

import java.util.Collections;
import java.util.List;

/**
 * @author Dmytro Dashenkov
 */
public class MessageFieldValidatorShould extends FieldValidatorShould<Message> {

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
        return getValidator(MAP_FIELD_DESC, Collections.emptyList());
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
                                                      List<? extends Message> values) {
        FieldContext context = FieldContext.create(field);
        return new MessageFieldValidator(context, values, false);
    }
}
