/*
 * Copyright 2018, TeamDev. All rights reserved.
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
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import io.spine.logging.Logging;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Validates that one of the fields defined by the {@code required_field} option is present.
 *
 * See definition of {@code MessageOptions.required_field} in {@code options.proto}.
 */
final class AlternativeFieldValidator implements Logging {

    /**
     * The name of the message option field.
     */
    private static final String OPTION_REQUIRED_FIELD = "required_field";

    /**
     * The separator of field name (or field combination) options.
     */
    private static final char OPTION_SEPARATOR = '|';

    /**
     * Combination of fields are made with ampersand.
     */
    private static final char AMPERSAND = '&';

    /**
     * The pattern to remove whitespace from the option field value.
     */
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private final MessageValue message;

    /**
     * The list builder to accumulate violations.
     */
    private final ImmutableList.Builder<ConstraintViolation> violations = ImmutableList.builder();

    AlternativeFieldValidator(MessageValue message) {
        this.message = checkNotNull(message);
    }

    List<? extends ConstraintViolation> validate() {
        Map<FieldDescriptor, Object> options = message.options();
        for (FieldDescriptor optionDescriptor : options.keySet()) {
            if (OPTION_REQUIRED_FIELD.equals(optionDescriptor.getName())) {
                JavaType optionType = optionDescriptor.getJavaType();
                if (optionType == JavaType.STRING) {
                    String requiredFieldExpression = (String) options.get(optionDescriptor);
                    ImmutableList<RequiredFieldOption> fieldOptions =
                            parse(requiredFieldExpression);
                    if (!alternativeFound(fieldOptions)) {
                        String msgFormat =
                                "None of the fields match the `required_field` definition: %s";
                        ConstraintViolation requiredFieldNotFound = ConstraintViolation
                                .newBuilder()
                                .setMsgFormat(msgFormat)
                                .addParam(requiredFieldExpression)
                                .build();
                        violations.add(requiredFieldNotFound);
                    }
                } else {
                    log().warn("`{}` is not of string type. Found: {}",
                               OPTION_REQUIRED_FIELD, optionType);
                }
            }
        }
        return violations.build();
    }

    private static ImmutableList<RequiredFieldOption> parse(String optionsDefinition) {
        ImmutableList.Builder<RequiredFieldOption> alternatives = ImmutableList.builder();
        String whiteSpaceRemoved = WHITESPACE.matcher(optionsDefinition)
                                             .replaceAll("");
        Iterable<String> parts = Splitter.on(OPTION_SEPARATOR)
                                         .split(whiteSpaceRemoved);
        for (String part : parts) {
            if (part.indexOf(AMPERSAND) > 0) {
                alternatives.add(RequiredFieldOption.ofCombination(part));
            } else {
                alternatives.add(RequiredFieldOption.ofField(part));
            }
        }
        return alternatives.build();
    }

    private boolean alternativeFound(Iterable<RequiredFieldOption> fieldOptions) {
        for (RequiredFieldOption option : fieldOptions) {
            boolean found = option.isCombination()
                            ? checkCombination(option.getFieldNames())
                            : checkField(option.getFieldName());
            if (found) {
                return true;
            }
        }
        return false;
    }

    private boolean checkField(String fieldName) {
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

    private boolean checkCombination(ImmutableList<String> fieldNames) {
        for (String fieldName : fieldNames) {
            if (!checkField(fieldName)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Represents an alternative in the definition of {@code required_field}.
     *
     * <p>It can be either a field or combination of fields.
     */
    private static final class RequiredFieldOption {

        private final @Nullable String fieldName;
        private final @Nullable ImmutableList<String> fieldNames;

        private RequiredFieldOption(String fieldName) {
            this.fieldName = fieldName;
            this.fieldNames = null;
        }

        private RequiredFieldOption(Iterable<String> fieldNames) {
            this.fieldName = null;
            this.fieldNames = ImmutableList.copyOf(fieldNames);
        }

        static RequiredFieldOption ofField(String fieldName) {
            return new RequiredFieldOption(fieldName);
        }

        static RequiredFieldOption ofCombination(Iterable<String> fieldNames) {
            return new RequiredFieldOption(fieldNames);
        }

        static RequiredFieldOption ofCombination(CharSequence expression) {
            Iterable<String> parts = Splitter.on(AMPERSAND)
                                             .split(expression);
            return ofCombination(parts);
        }

        boolean isField() {
            return fieldName != null;
        }

        boolean isCombination() {
            return fieldNames != null;
        }

        String getFieldName() {
            if (fieldName == null) {
                String msg = "The option is not a field but a combination of fields.";
                throw new IllegalStateException(msg);
            }
            return fieldName;
        }

        @SuppressWarnings("ReturnOfCollectionOrArrayField")
            // It is OK to suppress as we're using ImmutableList.
        ImmutableList<String> getFieldNames() {
            if (fieldNames == null) {
                String msg = "The option is not a combination, but a single field.";
                throw new IllegalStateException(msg);
            }

            return fieldNames;
        }
    }
}
