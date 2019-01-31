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
import io.spine.base.FieldPath;
import io.spine.option.PatternOption;

import java.util.List;

import static io.spine.validate.FieldValidator.getErrorMsgFormat;
import static java.util.stream.Collectors.toList;

/**
 * A constraint that, when applied to a string field, checks whether that field matches the
 * specified pattern.
 */
final class PatternConstraint implements Constraint<FieldValue<String>> {

    private final PatternOption optionValue;

    PatternConstraint(PatternOption optionValue) {
        this.optionValue = optionValue;
    }

    @Override
    public List<ConstraintViolation> check(FieldValue<String> fieldValue) {
        String regex = optionValue.getRegex();
        ImmutableList<String> values = fieldValue.asList();
        List<ConstraintViolation> violations =
                values.stream()
                      .filter(value -> !value.matches(regex))
                      .map(value -> newViolation(fieldValue))
                      .collect(toList());
        return violations;
    }

    private ConstraintViolation newViolation(FieldValue<String> fieldValue) {
        String msg = getErrorMsgFormat(this.optionValue, this.optionValue.getMsgFormat());
        FieldPath fieldPath = fieldValue.context()
                                        .getFieldPath();
        String regex = this.optionValue.getRegex();
        ConstraintViolation violation = ConstraintViolation
                .newBuilder()
                .setMsgFormat(msg)
                .addParam(regex)
                .setFieldPath(fieldPath)
                .build();
        return violation;
    }
}
