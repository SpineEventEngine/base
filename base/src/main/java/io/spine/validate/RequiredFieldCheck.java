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
import io.spine.base.FieldPath;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.newIllegalStateException;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * Method object for validating a value.
 */
final class RequiredFieldCheck {

    private static final String ERROR_MESSAGE =
            "Message must have all the required fields set according to the rule: `%s`.";

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

    private final MessageValue message;
    private final ImmutableList<Alternative> alternatives;
    private final String optionValue;

    RequiredFieldCheck(String optionValue, MessageValue value) {
        this.message = checkNotNull(value);
        this.optionValue = checkNotEmptyOrBlank(optionValue.trim());
        this.alternatives = parse();
    }

    Optional<ConstraintViolation> perform() {
        boolean matches = alternatives
                .stream()
                .anyMatch(alternative -> alternative.allPresent(message));
        return matches
               ? Optional.empty()
               : Optional.of(ConstraintViolation
                                     .newBuilder()
                                     .setMsgFormat(ERROR_MESSAGE)
                                     .setFieldPath(fieldPath())
                                     .setTypeName(typeName())
                                     .addParam(optionValue)
                                     .build()
        );
    }

    private FieldPath fieldPath() {
        return message.context()
                      .fieldPath();
    }

    private String typeName() {
        return message.declaration()
                      .name()
                      .value();
    }

    private ImmutableList<Alternative> parse() {
        ImmutableList.Builder<Alternative> alternatives = ImmutableList.builder();
        String whiteSpaceRemoved = WHITESPACE.matcher(optionValue)
                                             .replaceAll("");
        Iterable<String> parts = orSplitter.split(whiteSpaceRemoved);
        for (String part : parts) {
            List<String> fieldNames = andSplitter.splitToList(part);
            alternatives.add(Alternative.ofCombination(fieldNames));
        }
        return alternatives.build();
    }

    /**
     * Combinations of required fields found in the message value.
     */
    private static class Alternative {

        private final ImmutableList<String> fieldNames;

        private Alternative(ImmutableList<String> names) {
            fieldNames = names;
        }

        private static Alternative ofCombination(Iterable<String> fieldNames) {
            return new Alternative(ImmutableList.copyOf(fieldNames));
        }

        private boolean allPresent(MessageValue containingMessage) {
            for (String fieldName : fieldNames) {
                Optional<FieldValue> field = containingMessage.valueOf(fieldName);
                FieldValue value = field.orElseThrow(
                        () -> fieldNotFound(containingMessage, fieldName)
                );
                if (value.isDefault()) {
                    return false;
                }
            }
            return true;
        }

        private static IllegalStateException
        fieldNotFound(MessageValue containingMessage, String fieldName) {
            return newIllegalStateException(
                    "Message `%s` declares a constraint with a field `%s`, which does not exist.",
                    containingMessage.declaration(),
                    fieldName
            );
        }
    }
}
