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

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Descriptors;
import io.spine.option.OptionsProto;

import java.util.List;
import java.util.function.Predicate;

/**
 * An option that makes a field {@code required}.
 *
 * <p>If a {@code required} field is missing, an error is produced.
 */
public class RequiredFieldOption extends FieldValidatingOption<Boolean> {

    private final Predicate<FieldValue> isNotSet;

    /**
     * Creates a new instance of this option.
     *
     * @param isNotSet
     *         a function that defines whether a field value is set or not
     */
    RequiredFieldOption(Predicate<FieldValue> isNotSet) {
        this.isNotSet = isNotSet;
    }

    /**
     * @inheritDoc <p>Any field can be {@code required}.
     */
    @Override
    boolean applicableTo(Descriptors.FieldDescriptor field) {
        return true;
    }

    /**
     * Any field can be {@code required}, so this method is never called.
     */
    @Override
    ValidationException onInapplicable(Descriptors.FieldDescriptor field) {
        return nop();
    }

    @Override
    List<ConstraintViolation> applyValidatingRules(FieldValue value) {
        if (isNotSet.test(value)) {
            return requiredViolated(value);
        }
        return ImmutableList.of();
    }

    private static List<ConstraintViolation> requiredViolated(FieldValue value) {
        ConstraintViolation violation = ConstraintViolation
                .newBuilder()
                .setMsgFormat("A required field %s was `required` but was not set.")
                .addParam(value.declaration()
                               .name()
                               .value())
                .setFieldPath(value.context().getFieldPath())
                .build();
        return ImmutableList.of(violation);
    }

    @Override
    boolean optionPresentFor(FieldValue value) {
        return getValueFor(value);
    }

    @Override
    public Boolean getValueFor(FieldValue something) {
        return something.valueOf(OptionsProto.required);
    }

    private <T> T nop() {
        return null;
    }
}
