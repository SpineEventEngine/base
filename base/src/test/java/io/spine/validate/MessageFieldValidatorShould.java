package io.spine.validate;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import io.spine.base.FieldPath;
import io.spine.test.validate.msg.InvalidMessage;
import io.spine.test.validate.msg.MessageWithRepeatedUnchekedMessageField;
import io.spine.test.validate.msg.MessageWithRepeatedValidatedMessageField;

/**
 * @author Dmytro Dashenkov
 */
public class MessageFieldValidatorShould extends FieldValidatorShould<Message> {

    private static final FieldDescriptor VALIDATED_FIELD_DESC =
            MessageWithRepeatedValidatedMessageField.getDescriptor()
                                                    .getFields()
                                                    .get(0);

    private static final FieldDescriptor NOT_VALIDATED_FIELD_DESC =
            MessageWithRepeatedUnchekedMessageField.getDescriptor()
                                                   .getFields()
                                                   .get(0);

    @Override
    protected FieldValidator<Message> getValidatorForValidatedField(ImmutableList<Message> values) {
        return new MessageFieldValidator(VALIDATED_FIELD_DESC,
                                         values,
                                         false,
                                         FieldPath.getDefaultInstance());
    }

    @Override
    protected FieldValidator<Message> getValidatorForUncheckedField(ImmutableList<Message> values) {
        return new MessageFieldValidator(NOT_VALIDATED_FIELD_DESC,
                                         values,
                                         false,
                                         FieldPath.getDefaultInstance());
    }

    @Override
    protected InvalidMessage newValue() {
        return InvalidMessage.newBuilder()
                             .setS("some non-empty string")
                             .build();
    }

    @Override
    protected InvalidMessage defaultValue() {
        return InvalidMessage.getDefaultInstance();
    }
}
