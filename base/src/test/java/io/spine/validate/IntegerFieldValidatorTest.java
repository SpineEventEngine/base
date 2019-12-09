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

import com.google.protobuf.Descriptors;
import io.spine.code.proto.FieldContext;
import io.spine.test.validate.RequiredIntFieldValue;
import org.junit.jupiter.api.DisplayName;

@DisplayName("IntegerFieldValidator should")
class IntegerFieldValidatorTest extends NumberFieldValidatorTest<Integer, IntegerFieldValidator> {

    private static final int DOS = 2;

    IntegerFieldValidatorTest() {
        super(DOS,
              -DOS,
              new IntegerFieldValidator(FieldValue.of(DOS, fieldContext)),
              requiredFieldValidator());
    }

    private static IntegerFieldValidator requiredFieldValidator() {
        Descriptors.FieldDescriptor descriptor = RequiredIntFieldValue.getDescriptor()
                                                                      .getFields()
                                                                      .get(0);
        FieldContext context = FieldContext.create(descriptor);
        FieldValue value = FieldValue.of(DOS, context);
        IntegerFieldValidator requiredValidator = new IntegerFieldValidator(value);
        return requiredValidator;
    }
}
