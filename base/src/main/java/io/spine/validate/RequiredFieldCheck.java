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

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * Method object for validating a value.
 */
final class RequiredFieldCheck {

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
    private final MessageValue value;
    private final ImmutableList<Alternatives> alternatives;
    private final ImmutableList.Builder<ConstraintViolation> violations =
            ImmutableList.builder();

    RequiredFieldCheck(String optionValue, MessageValue value) {
        this.value = checkNotNull(value);
        this.optionValue = checkNotEmptyOrBlank(optionValue);
        this.alternatives = parse(optionValue);
    }

    ImmutableList<ConstraintViolation> perform() {
        if (!alternativeFound()) {
            String msgFormat =
                    "None of the fields match the `required_field` definition: `%s`.";
            TypeName typeName = value.declaration()
                                     .name();
            ConstraintViolation requiredFieldNotFound = ConstraintViolation
                    .newBuilder()
                    .setMsgFormat(msgFormat)
                    .addParam(optionValue)
                    .setTypeName(typeName.value())
                    .build();
            violations.add(requiredFieldNotFound);

        }
        return violations.build();
    }

    private boolean alternativeFound() {
        for (Alternatives alternative : alternatives) {
            boolean found = checkFields(alternative.fieldNames);
            if (found) {
                return true;
            }
        }
        return false;
    }

    private boolean checkFields(Iterable<String> fieldNames) {
        for (String fieldName : fieldNames) {
            if (!checkField(fieldName)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkField(String fieldName) {
        Optional<FieldValue> fieldValue = value.valueOf(fieldName);
        if (!fieldValue.isPresent()) {
            ConstraintViolation notFound = ConstraintViolation
                    .newBuilder()
                    .setMsgFormat("Field named `%s` is not found.")
                    .addParam(fieldName)
                    .build();
            violations.add(notFound);
            return false;
        }
        if (fieldValue.get().isDefault()) {
            ConstraintViolation notFound = ConstraintViolation
                    .newBuilder()
                    .setMsgFormat("Field named `%s` is not found.")
                    .addParam(fieldName)
                    .build();
            violations.add(notFound);
        }
        FieldValidator<?> fieldValidator = fieldValue.get()
                                                     .createValidatorAssumingRequired();
        List<ConstraintViolation> violations = fieldValidator.validate();
        // Do not add violations to the results because we have options.
        // The violation would be that none of the field or combinations is defined.

        return violations.isEmpty();
    }

    private static ImmutableList<Alternatives> parse(String expression) {
        ImmutableList.Builder<Alternatives> alternatives = ImmutableList.builder();
        String whiteSpaceRemoved = WHITESPACE.matcher(expression)
                                                                     .replaceAll("");
        Iterable<String> parts = orSplitter.split(whiteSpaceRemoved);
        for (String part : parts) {
            alternatives.add(Alternatives.ofCombination(part));
        }
        return alternatives.build();
    }

    /**
     * Combinations of required fields found in the message value.
     */
    private static class Alternatives {

        private final ImmutableList<String> fieldNames;

        private Alternatives(ImmutableList<String> names) {
            fieldNames = names;
        }

        private static Alternatives ofCombination(ImmutableList<String> fieldNames) {
            return new Alternatives(fieldNames);
        }

        private static Alternatives ofCombination(String expression) {
            Iterable<String> parts = andSplitter.split(expression);
            return ofCombination(ImmutableList.copyOf(parts));
        }
    }
}
