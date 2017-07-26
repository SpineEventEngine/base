/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

package io.spine.gradle.compiler.lookup.valrule;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import org.slf4j.Logger;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static io.spine.gradle.compiler.util.UnknownOptions.getUnknownOptionValue;
import static io.spine.gradle.compiler.util.UnknownOptions.hasUnknownOption;
import static io.spine.option.OptionsProto.VALIDATION_OF_FIELD_NUMBER;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Finds Protobuf definitions of validation rules.
 *
 * @author Dmytro Grankin
 */
class ValidationRulesFinder {

    @VisibleForTesting
    static final String PROTO_TYPE_SEPARATOR = ".";

    private final FileDescriptorProto fileDescriptor;
    private final String packagePrefix;

    ValidationRulesFinder(FileDescriptorProto fileDescriptor) {
        this.fileDescriptor = checkNotNull(fileDescriptor);
        this.packagePrefix = fileDescriptor.getPackage() + PROTO_TYPE_SEPARATOR;
    }

    /**
     * Finds Protobuf definitions of validation rules in the file descriptor.
     *
     * @return a map from a validation rule type name to the target field
     */
    Map<String, String> findRules() {
        log().debug("Looking up for validation rules in {}.", fileDescriptor.getName());
        final Map<String, String> rules = newLinkedHashMap();
        for (DescriptorProto messageDescriptor : fileDescriptor.getMessageTypeList()) {
            final Map<String, String> foundRules = scanMessageAndNestedTypes(messageDescriptor);
            rules.putAll(foundRules);
        }
        log().debug("Found validation rules: {}.", rules.toString());
        return rules;
    }

    private Map<String, String> scanMessageAndNestedTypes(DescriptorProto messageDescriptor) {
        final Map<String, String> rules = newLinkedHashMap();
        if (isValidationRule(messageDescriptor)) {
            final String ruleType = packagePrefix + messageDescriptor.getName();
            final String ruleTarget = getUnknownOptionValue(messageDescriptor,
                                                            VALIDATION_OF_FIELD_NUMBER);
            rules.put(ruleType, ruleTarget);
        }

        rules.putAll(scanNestedTypes(messageDescriptor));
        return rules;
    }

    private Map<String, String> scanNestedTypes(DescriptorProto messageDescriptor) {
        final Map<String, String> rules = newLinkedHashMap();
        for (DescriptorProto nestedMessage : messageDescriptor.getNestedTypeList()) {
            if (isValidationRule(nestedMessage)) {
                final String outerMessageType = packagePrefix + messageDescriptor.getName();
                final String ruleType =
                        outerMessageType + PROTO_TYPE_SEPARATOR + nestedMessage.getName();
                final String ruleTarget = getUnknownOptionValue(nestedMessage,
                                                                VALIDATION_OF_FIELD_NUMBER);
                rules.put(ruleType, ruleTarget);
            }
        }
        return rules;
    }

    private static boolean isValidationRule(DescriptorProto messageDescriptor) {
        return hasUnknownOption(messageDescriptor, VALIDATION_OF_FIELD_NUMBER);
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = getLogger(ValidationRulesFinder.class);
    }
}
