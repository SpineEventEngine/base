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

import com.google.common.collect.ImmutableSet;
import io.spine.option.OptionsProto;
import io.spine.option.PatternOption;

import java.util.Set;

import static io.spine.protobuf.TypeConverter.toAny;

/**
 * Validates fields of type {@link String}.
 */
class StringFieldValidator extends FieldValidator<String> {

    private final PatternOption patternOption;
    private final String regex;

    /**
     * Creates a new validator instance.
     *
     * @param fieldValue
     *         the value to validate
     * @param assumeRequired
     *         if {@code true} the validator would assume that the field is required even
     *         if this constraint is not set explicitly
     */
    StringFieldValidator(FieldValue fieldValue, boolean assumeRequired) {
        super(fieldValue, assumeRequired, true);
        this.patternOption = fieldValue.valueOf(OptionsProto.pattern);
        this.regex = patternOption.getRegex();
    }

    @Override
    protected void validateOwnRules() {
        checkIfMatchesToRegexp();
    }

    @Override
    protected Set<AbstractFieldValidatingOption> additionalOptions() {
        return ImmutableSet.of();
    }

    private void checkIfMatchesToRegexp() {
        if (regex.isEmpty()) {
            return;
        }
        for (String value : getValues()) {
            if (!value.matches(regex)) {
                addViolation(newViolation(value));
            }
        }
    }

    private ConstraintViolation newViolation(String fieldValue) {
        String msg = getErrorMsgFormat(patternOption, patternOption.getMsgFormat());
        ConstraintViolation violation = ConstraintViolation
                .newBuilder()
                .setMsgFormat(msg)
                .addParam(regex)
                .setFieldPath(getFieldPath())
                .setFieldValue(toAny(fieldValue))
                .build();
        return violation;
    }

    @Override
    protected boolean isNotSet(String value) {
        boolean result = value.isEmpty();
        return result;
    }
}
