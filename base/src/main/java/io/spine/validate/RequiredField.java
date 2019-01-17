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
import com.google.protobuf.Descriptors;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * A message option that defines a set of required fields for the message.
 *
 *
 */
public class RequiredField extends MessageValidatingOption<String> {

    /**
     * The name of the message option field.
     */
    private static final String OPTION_REQUIRED_FIELD = "required_field";

    /**
     * Combination of fields are made with ampersand.
     */
    private static final char AMPERSAND = '&';

    private final ImmutableList.Builder<ConstraintViolation> violations = ImmutableList.builder();

    @Override
    List<ConstraintViolation> applyValidationRules(MessageValue message) {
        String expression = getValueFor(message);
        ImmutableList<RequiredFieldOptionData> parse = parse(expression);
        if (!alternativeFound(parse, message)) {
            String msgFormat =
                    "None of the fields match the `required_field` definition: %s";
            ConstraintViolation requiredFieldNotFound = ConstraintViolation
                    .newBuilder()
                    .setMsgFormat(msgFormat)
                    .addParam(expression)
                    .build();
            violations.add(requiredFieldNotFound);
        }
        return violations.build();
    }

    @Override
    boolean optionPresent(MessageValue message) {
        return !getValueFor(message).isEmpty();
    }

    @Override
    public String getValueFor(MessageValue message) {
        Map<Descriptors.FieldDescriptor, Object> options = message.options();
        for (Descriptors.FieldDescriptor optionDescriptor : options.keySet()) {
            if (OPTION_REQUIRED_FIELD.equals(optionDescriptor.getName())) {
                String expression = (String) options.get(optionDescriptor);
                return expression;
            }
        }
        return "";
    }

    private static class RequiredFieldOptionData {

        /**
         * The pattern to remove whitespace from the option field value.
         */
        private static final Pattern WHITESPACE = Pattern.compile("\\s+");

        /**
         * The separator of field name (or field combination) options.
         */
        private static final char OPTION_SEPARATOR = '|';

        private final ImmutableList<String> fieldNames;

        private RequiredFieldOptionData(ImmutableList<String> names) {
            fieldNames = names;
        }

        static RequiredFieldOptionData ofCombination(ImmutableList<String> fieldNames) {
            return new RequiredFieldOptionData(fieldNames);
        }

        static RequiredFieldOptionData ofCombination(CharSequence expression) {
            ImmutableList<String> parts = ImmutableList.copyOf(Splitter.on(AMPERSAND)
                                                                       .split(expression));
            return ofCombination(parts);
        }
    }

    private static ImmutableList<RequiredFieldOptionData> parse(String expression) {
        ImmutableList.Builder<RequiredFieldOptionData> alternatives = ImmutableList.builder();
        String whiteSpaceRemoved = RequiredFieldOptionData.WHITESPACE.matcher(expression)
                                                                     .replaceAll("");
        Iterable<String> parts = Splitter.on(RequiredFieldOptionData.OPTION_SEPARATOR)
                                         .split(whiteSpaceRemoved);
        for (String part : parts) {
            alternatives.add(RequiredFieldOptionData.ofCombination(part));
        }
        return alternatives.build();
    }

    private boolean alternativeFound(Iterable<RequiredFieldOptionData> fieldOptions,
                                     MessageValue message) {
        for (RequiredFieldOptionData option : fieldOptions) {
            boolean found = checkFields(option.fieldNames, message);
            if (found) {
                return true;
            }
        }
        return false;
    }

    private boolean checkField(String fieldName, MessageValue message) {
        Optional<FieldValue> fieldValue = message.valueOf(fieldName);
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
