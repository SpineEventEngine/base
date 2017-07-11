package io.spine.validate;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.base.FieldPath;
import io.spine.test.validate.msg.MessageWithRepeatedUnchekedStringField;
import io.spine.test.validate.msg.MessageWithRepeatedValidatedStringField;

/**
 * @author Dmytro Dashenkov
 */
public class StringFieldValidatorShould extends FieldValidatorShould<String> {

    private static final FieldDescriptor VALIDATED_FIELD_DESC =
            MessageWithRepeatedValidatedStringField.getDescriptor()
                                                   .getFields()
                                                   .get(0);

    private static final FieldDescriptor NOT_VALIDATED_FIELD_DESC =
            MessageWithRepeatedUnchekedStringField.getDescriptor()
                                                  .getFields()
                                                  .get(0);

    @Override
    protected FieldValidator<String> getValidatorForValidatedField(ImmutableList<String> values) {
        return new StringFieldValidator(VALIDATED_FIELD_DESC,
                                        values,
                                        FieldPath.getDefaultInstance(),
                                        false);
    }

    @Override
    protected FieldValidator<String> getValidatorForUncheckedField(ImmutableList<String> values) {
        return new StringFieldValidator(NOT_VALIDATED_FIELD_DESC,
                                        values,
                                        FieldPath.getDefaultInstance(),
                                        false);
    }

    @Override
    protected String newValue() {
        return "A";
    }

    @Override
    protected String defaultValue() {
        return "";
    }
}
