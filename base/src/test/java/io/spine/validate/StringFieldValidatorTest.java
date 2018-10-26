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
import io.spine.test.validate.MessageWithMapStringField;
import io.spine.test.validate.MessageWithRepeatedRequiredValidatedStringField;
import io.spine.test.validate.MessageWithRepeatedUnchekedStringField;
import io.spine.test.validate.MessageWithRepeatedValidatedStringField;
import io.spine.test.validate.MessegeWithRepeatedRequiredStringField;
import org.junit.jupiter.api.DisplayName;

import java.util.Collections;
import java.util.List;

@DisplayName("StringFieldValidator should")
public class StringFieldValidatorTest extends FieldValidatorTest<String> {

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
    protected StringFieldValidator validatedRequiredRepeatedFieldValidator(List<String> values) {
        return getValidator(VALIDATED_REQUIRED_FIELD_DESC, values);
    }

    @Override
    protected StringFieldValidator requiredRepeatedFieldValidator(List<String> values) {
        return getValidator(REQUIRED_FIELD_DESC, values);
    }

    @Override
    protected StringFieldValidator validatedRepeatedFieldValidator(List<String> values) {
        return getValidator(VALIDATED_FIELD_DESC, values);
    }

    @Override
    protected StringFieldValidator uncheckedRepeatedFieldValidator(List<String> values) {
        return getValidator(UNCHECKED_FIELD_DESC, values);
    }

    @Override
    protected FieldValidator<String> emptyMapFieldValidator() {
        return getValidator(MAP_FIELD_DESC, Collections.emptyMap());
    }

    @Override
    protected String newValue() {
        return "A";
    }

    @Override
    protected String defaultValue() {
        return "";
    }

    private static StringFieldValidator getValidator(FieldDescriptor field,
                                                     Object rawValue) {
        FieldContext fieldContext = FieldContext.create(field);
        return new StringFieldValidator(FieldValue.of(rawValue, fieldContext),
                                        false);
    }
}
