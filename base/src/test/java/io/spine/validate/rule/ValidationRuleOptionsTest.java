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

package io.spine.validate.rule;

import com.google.common.truth.OptionalSubject;
import com.google.protobuf.Descriptors;
import io.spine.code.proto.FieldContext;
import io.spine.option.OptionsProto;
import io.spine.test.validate.rule.AField;
import io.spine.test.validate.rule.AMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth8.assertThat;

@DisplayName("ValidationRuleOptions should")
final class ValidationRuleOptionsTest {

    @DisplayName("retrieve option value from context")
    @Test
    void retrieveOptionValue() {
        FieldContext targetContext = FieldContext.create(AMessage.getDescriptor()
                                                                 .getFields()
                                                                 .get(0));
        Descriptors.FieldDescriptor nameField = AField.getDescriptor()
                                                      .getFields()
                                                      .get(0);
        FieldContext context = targetContext.forChild(nameField);
        OptionalSubject subject =
                assertThat(ValidationRuleOptions.getOptionValue(context, OptionsProto.required));
        subject.isPresent();
        subject.hasValue(true);
    }

    @DisplayName("return empty for field without options")
    @Test
    void returnEmptyForNonExistingRuleOptions() {

        FieldContext targetContext = FieldContext.create(AMessage.getDescriptor()
                                                                 .getFields()
                                                                 .get(0));
        Descriptors.FieldDescriptor addressField = AField.getDescriptor()
                                                         .getFields()
                                                         .get(3);
        FieldContext context = targetContext.forChild(addressField);
        assertThat(ValidationRuleOptions.getOptionValue(context, OptionsProto.required)).isEmpty();
    }
}
