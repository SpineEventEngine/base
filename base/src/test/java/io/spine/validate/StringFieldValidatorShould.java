package io.spine.validate;

import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.base.FieldPath;
import io.spine.test.validate.msg.MessageWithMapStringField;
import io.spine.test.validate.msg.MessageWithRepeatedRequiredValidatedStringField;
import io.spine.test.validate.msg.MessageWithRepeatedUnchekedStringField;
import io.spine.test.validate.msg.MessageWithRepeatedValidatedStringField;
import io.spine.test.validate.msg.MessegeWithRepeatedRequiredStringField;

import java.util.Collections;
import java.util.List;

/**
 * @author Dmytro Dashenkov
 */
public class StringFieldValidatorShould extends FieldValidatorShould<String> {

    private static final FieldDescriptor UNCHECKED_FIELD_DESC =
            MessageWithRepeatedUnchekedStringField.getDescriptor()
                                                  .getFields()
                                                  .get(0);

    private static final FieldDescriptor VALIDATED_FIELD_DESC =
            MessageWithRepeatedValidatedStringField.getDescriptor()
                                                   .getFields()
                                                   .get(0);

    private static final FieldDescriptor REQUIRED_FIELD_DESC =
            MessegeWithRepeatedRequiredStringField.getDescriptor()
                                                  .getFields()
                                                  .get(0);

    private static final FieldDescriptor VALIDATED_REQUIRED_FIELD_DESC =
            MessageWithRepeatedRequiredValidatedStringField.getDescriptor()
                                                           .getFields()
                                                           .get(0);

    private static final FieldDescriptor MAP_FIELD_DESC = MessageWithMapStringField.getDescriptor()
                                                                                  .getFields()
                                                                                  .get(0);

    @Override
    protected StringFieldValidator validatedRequiredRepeatedField(List<String> values) {
        return getValidator(VALIDATED_REQUIRED_FIELD_DESC, values);
    }

    @Override
    protected StringFieldValidator requiredRepeatedField(List<String> values) {
        return getValidator(REQUIRED_FIELD_DESC, values);
    }

    @Override
    protected StringFieldValidator validatedRepeatedField(List<String> values) {
        return getValidator(VALIDATED_FIELD_DESC, values);
    }

    @Override
    protected StringFieldValidator uncheckedRepeatedField(List<String> values) {
        return getValidator(UNCHECKED_FIELD_DESC, values);
    }

    @Override
    protected FieldValidator<String> mapField() {
        return getValidator(MAP_FIELD_DESC, Collections.<String>emptyList());
    }

    @Override
    protected String newValue() {
        return "A";
    }

    @Override
    protected String defaultValue() {
        return "";
    }

    private StringFieldValidator getValidator(FieldDescriptor field,
                                              List<? extends String> values) {
        return new StringFieldValidator(field,
                                        values,
                                        FieldPath.getDefaultInstance(),
                                        false);
    }
}
