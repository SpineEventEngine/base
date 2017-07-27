package io.spine.validate;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import io.spine.test.validate.msg.InvalidMessage;
import io.spine.test.validate.msg.MessageWithMapMessageField;
import io.spine.test.validate.msg.MessageWithRepeatedRequiredValidatedMessageField;
import io.spine.test.validate.msg.MessageWithRepeatedUnchekedMessageField;
import io.spine.test.validate.msg.MessageWithRepeatedValidatedMessageField;
import io.spine.test.validate.msg.MessegeWithRepeatedRequiredMessageField;

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
        return getValidator(MAP_FIELD_DESC, Collections.<Message>emptyList());
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
        final DescriptorPath path = DescriptorPath.createRoot(field);
        return new MessageFieldValidator(path, values, false);
    }
}
