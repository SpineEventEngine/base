/*
 * Copyright 2022, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import io.spine.code.proto.FieldContext;
import io.spine.option.OptionsProto;
import io.spine.test.validation.AField;
import io.spine.test.validation.AMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth8.assertThat;
import static io.spine.validate.ExternalConstraintOptions.getOptionValue;

@DisplayName("`ExternalConstraintOptions` should")
final class ExternalConstraintOptionsTest {

    @DisplayName("retrieve option value from context")
    @Test
    void retrieveOptionValue() {
        var targetContext = FieldContext.create(AMessage.getDescriptor()
                                                        .getFields()
                                                        .get(0));
        var nameField = AField.getDescriptor()
                              .getFields()
                              .get(0);
        var context = targetContext.forChild(nameField);
        var subject = assertThat(getOptionValue(context, OptionsProto.required));
        subject.isPresent();
        subject.hasValue(true);
    }

    @DisplayName("return empty for field without options")
    @Test
    void returnEmptyForNonExistingRuleOptions() {
        var targetContext = FieldContext.create(AMessage.getDescriptor()
                                                        .getFields()
                                                        .get(0));
        var addressField = AField.getDescriptor()
                                 .getFields()
                                 .get(3);
        var context = targetContext.forChild(addressField);
        assertThat(getOptionValue(context, OptionsProto.required)).isEmpty();
    }

    @DisplayName("not return `default` option value if the option is not present" +
            " while the field context does match")
    @Test
    void notReturnOptionIfItIsNotPresentButContextMatch() {
        var targetContext = FieldContext.create(AMessage.getDescriptor()
                                                        .getFields()
                                                        .get(0));
        var nameField = AField.getDescriptor()
                              .getFields()
                              .get(0);
        var context = targetContext.forChild(nameField);
        assertThat(getOptionValue(context, OptionsProto.goes)).isEmpty();
    }
}
