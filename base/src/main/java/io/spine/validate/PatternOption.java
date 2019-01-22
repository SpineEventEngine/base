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
import io.spine.option.OptionsProto;

import java.util.List;
import java.util.Optional;

import static io.spine.protobuf.TypeConverter.toAny;
import static io.spine.util.Exceptions.newIllegalStateException;
import static io.spine.validate.FieldValidator.getErrorMsgFormat;

/**
 * An option that defines a pattern that a field value has to match.
 */
public class PatternOption extends FieldValidatingOption<io.spine.option.PatternOption, String> {

    private PatternOption(){
    }

    /** Returns a new instance of this option. */
    public static PatternOption create(){
        return new PatternOption();
    }

    private static List<ConstraintViolation> newViolation(FieldValue<String> fieldValue) {
        io.spine.option.PatternOption patternOption = getOption(fieldValue);
        String regex = patternOption.getRegex();
        FieldPath fieldPath = fieldValue.context()
                                        .getFieldPath();
        String msg = getErrorMsgFormat(patternOption, patternOption.getMsgFormat());
        ConstraintViolation violation = ConstraintViolation
                .newBuilder()
                .setMsgFormat(msg)
                .addParam(regex)
                .setFieldPath(fieldPath)
                .setFieldValue(toAny(fieldValue))
                .build();
        return ImmutableList.of(violation);
    }

    private static io.spine.option.PatternOption getOption(FieldValue<String> fieldValue) {
        io.spine.option.PatternOption option = fieldValue.valueOf(OptionsProto.pattern);
        return option;
    }

    private String getRegex(FieldValue<String> fieldValue) {
        String regex = valueFrom(fieldValue).orElseThrow(() -> illegalState(fieldValue))
                                            .getRegex();
        return regex;
    }

    private static IllegalStateException illegalState(FieldValue<String> fieldValue) {
        return newIllegalStateException("Could not validationRule regexp from field %s.",
                                        fieldValue.context()
                                                  .getFieldPath());
    }

    @Override
    public Optional<io.spine.option.PatternOption> valueFrom(FieldValue<String> bearer) {
        io.spine.option.PatternOption regex = bearer.valueOf(OptionsProto.pattern);
        return bearer.valueOf(OptionsProto.pattern)
                     .getRegex()
                     .isEmpty() ?
               Optional.empty() :
               Optional.of(regex);
    }

    private boolean allMatch(FieldValue<String> fieldValue) {
        String regex = getRegex(fieldValue);
        return fieldValue.asList()
                         .stream()
                         .allMatch(regex::matches);
    }

    @Override
    ValidationRule<FieldValue<String>> validationRule() {
        return new ValidationRule<>(this::allMatch, PatternOption::newViolation);
    }
}
