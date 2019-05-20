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
import io.spine.type.TypeName;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * A constraint that, when applied to a message, checks whether the specified combination of fields
 * has non-default values.
 */
final class RequiredFieldConstraint implements Constraint<MessageValue> {

    /**
     * The pattern to remove whitespace from the option field value.
     */
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    /**
     * Splits Protobuf field names separated with a logical disjunction (OR) literal {@literal |}.
     */
    private static final Splitter orSplitter = Splitter.on('|');

    /**
     * Splits Protobuf field names separated with a logical conjunction (AND) literal {@literal &}.
     */
    private static final Splitter andSplitter = Splitter.on('&');

    private final String optionValue;

    private final ImmutableList.Builder<ConstraintViolation> violations = ImmutableList.builder();

    RequiredFieldConstraint(String optionValue) {
        this.optionValue = optionValue;
    }

    @Override
    public ImmutableList<ConstraintViolation> check(MessageValue value) {
        return matches(value) ? ImmutableList.of() : violations.build();
    }

    private boolean matches(MessageValue messageField) {
        if (optionValue.isEmpty()) {
            return true;
        }
        ImmutableList<RequiredFieldAlternatives> alternatives = parse(this.optionValue);
        if (!alternativeFound(alternatives, messageField)) {
            String msgFormat =
                    "None of the fields match the `required_field` definition: `%s`.";
            TypeName typeName = messageField.declaration()
                                            .name();
            ConstraintViolation requiredFieldNotFound = ConstraintViolation
                    .newBuilder()
                    .setMsgFormat(msgFormat)
                    .addParam(this.optionValue)
                    .setTypeName(typeName.value())
                    .build();
            violations.add(requiredFieldNotFound);
            return false;
        }
        return true;
    }

    /**
     * Combinations of required fields found in the message value.
     */
    private static class RequiredFieldAlternatives {

        private final ImmutableList<String> fieldNames;

        private RequiredFieldAlternatives(ImmutableList<String> names) {
            fieldNames = names;
        }

        private static RequiredFieldAlternatives ofCombination(ImmutableList<String> fieldNames) {
            return new RequiredFieldAlternatives(fieldNames);
        }

        private static RequiredFieldAlternatives ofCombination(String expression) {
            Iterable<String> parts = andSplitter.split(expression);
            return ofCombination(ImmutableList.copyOf(parts));
        }
    }

    private static ImmutableList<RequiredFieldAlternatives> parse(String expression) {
        ImmutableList.Builder<RequiredFieldAlternatives> alternatives = ImmutableList.builder();
        String whiteSpaceRemoved = WHITESPACE.matcher(expression)
                                             .replaceAll("");
        Iterable<String> parts = orSplitter.split(whiteSpaceRemoved);
        for (String part : parts) {
            alternatives.add(RequiredFieldAlternatives.ofCombination(part));
        }
        return alternatives.build();
    }

    private boolean alternativeFound(Iterable<RequiredFieldAlternatives> alternatives,
                                     MessageValue message) {
        for (RequiredFieldAlternatives alternative : alternatives) {
            boolean found = checkFields(alternative.fieldNames, message);
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

    private boolean checkFields(Iterable<String> fieldNames, MessageValue message) {
        for (String fieldName : fieldNames) {
            if (!checkField(fieldName, message)) {
                return false;
            }
        }
        return true;
    }
}
