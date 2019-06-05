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

package io.spine.validate.option;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import io.spine.base.FieldPath;
import io.spine.option.PatternOption;
import io.spine.type.TypeName;
import io.spine.validate.ConstraintViolation;
import io.spine.validate.FieldValue;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static io.spine.validate.FieldValidator.errorMsgFormat;

/**
 * A constraint, which when applied to a string field, checks whether that field matches the
 * specified pattern.
 */
@Immutable
final class PatternConstraint extends FieldValueConstraint<String, PatternOption> {

    PatternConstraint(PatternOption optionValue) {
        super(optionValue);
    }

    @Override
    public ImmutableList<ConstraintViolation> check(FieldValue<String> fieldValue) {
        String regex = optionValue().getRegex();
        ImmutableList<String> values = fieldValue.asList();
        ImmutableList<ConstraintViolation> violations =
                values.stream()
                      .filter(value -> !value.matches(regex))
                      .map(value -> newViolation(fieldValue))
                      .collect(toImmutableList());
        return violations;
    }

    private ConstraintViolation newViolation(FieldValue<String> fieldValue) {
        String msg = errorMsgFormat(optionValue(), optionValue().getMsgFormat());
        FieldPath fieldPath = fieldValue.context()
                                        .fieldPath();
        String regex = optionValue().getRegex();
        TypeName declaringType = fieldValue.declaration()
                                           .declaringType()
                                           .name();
        ConstraintViolation violation = ConstraintViolation
                .newBuilder()
                .setMsgFormat(msg)
                .addParam(regex)
                .setFieldPath(fieldPath)
                .setTypeName(declaringType.value())
                .build();
        return violation;
    }
}
