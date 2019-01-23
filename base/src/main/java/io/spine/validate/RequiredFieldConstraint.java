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

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * A constraint that, when applied to a message, checks whether the specified combination of fields
 * have non-default values.
 */
public class RequiredFieldConstraint implements Constraint<MessageValue> {

    /**
     * Combination of fields are made with ampersand.
     */
    private static final char AMPERSAND = '&';
    private final RequiredField requiredField = new RequiredField();

    private final ImmutableList.Builder<ConstraintViolation> violations = ImmutableList.builder();

    @Override
    public List<ConstraintViolation> check(MessageValue value) {
        return matches(value) ? ImmutableList.of() : violations.build();
    }

    private boolean matches(MessageValue messageField) {
        if (!requiredField.valueFrom(messageField)
                          .isPresent()) {
            return true;
        }
        String expression = requiredField.valueFrom(messageField)
                                         .get();
        ImmutableList<RequiredFieldOptions> parse = parse(expression);
        if (!alternativeFound(parse, messageField)) {
            String msgFormat =
                    "None of the fields match the `required_field` definition: %s";
            ConstraintViolation requiredFieldNotFound = ConstraintViolation
                    .newBuilder()
                    .setMsgFormat(msgFormat)
                    .addParam(expression)
                    .build();
            violations.add(requiredFieldNotFound);
            return false;
        }
        return true;
    }

    private static class RequiredFieldOptions {

        /**
         * The pattern to remove whitespace from the option field value.
         */
        private static final Pattern WHITESPACE = Pattern.compile("\\s+");

        /**
         * The separator of field name (or field combination) options.
         */
        private static final char OPTION_SEPARATOR = '|';

        private final ImmutableList<String> fieldNames;

        private RequiredFieldOptions(ImmutableList<String> names) {
            fieldNames = names;
        }

        static RequiredFieldOptions ofCombination(ImmutableList<String> fieldNames) {
            return new RequiredFieldOptions(fieldNames);
        }

        static RequiredFieldOptions ofCombination(CharSequence expression) {
            ImmutableList<String> parts = ImmutableList.copyOf(Splitter.on(AMPERSAND)
                                                                       .split(expression));
            return ofCombination(parts);
        }
    }

    private static ImmutableList<RequiredFieldOptions> parse(String expression) {
        ImmutableList.Builder<RequiredFieldOptions> alternatives = ImmutableList.builder();
        String whiteSpaceRemoved = RequiredFieldOptions.WHITESPACE.matcher(expression)
                                                                  .replaceAll("");
        Iterable<String> parts = Splitter.on(RequiredFieldOptions.OPTION_SEPARATOR)
                                         .split(whiteSpaceRemoved);
        for (String part : parts) {
            alternatives.add(RequiredFieldOptions.ofCombination(part));
        }
        return alternatives.build();
    }

    private boolean alternativeFound(Iterable<RequiredFieldOptions> fieldOptions,
                                     MessageValue message) {
        for (RequiredFieldOptions option : fieldOptions) {
            boolean found = checkFields(option.fieldNames, message);
            if (found) {
                return true;
            }
        }
        return false;
    }

    private boolean checkField(String fieldName, MessageValue message) {
        Optional<FieldValue<?>> fieldValue = message.valueOf(fieldName);
        if (!fieldValue.isPresent()) {
            ConstraintViolation notFound = ConstraintViolation
                    .newBuilder()
                    .setMsgFormat("Field named `%s` is not found.")
                    .addParam(fieldName)
                    .build();
            violations.add(notFound);
            return false;
        }
        FieldValidator<?> fieldValidator = fieldValue.get()
                                                     .createValidatorAssumingRequired();
        List<ConstraintViolation> violations = fieldValidator.validate();
        // Do not add violations to the results because we have options.
        // The violation would be that none of the field or combinations is defined.

        return violations.isEmpty();
    }

    private boolean checkFields(ImmutableList<String> fieldNames, MessageValue message) {
        for (String fieldName : fieldNames) {
            if (!checkField(fieldName, message)) {
                return false;
            }
        }
        return true;
    }
}
