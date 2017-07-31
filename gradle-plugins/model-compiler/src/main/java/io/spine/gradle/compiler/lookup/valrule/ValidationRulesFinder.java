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

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import org.slf4j.Logger;

import java.util.Map;

import static com.google.common.collect.Maps.newLinkedHashMap;
import static io.spine.gradle.compiler.util.UnknownOptions.getUnknownOptionValue;
import static io.spine.gradle.compiler.util.UnknownOptions.hasUnknownOption;
import static io.spine.option.OptionsProto.VALIDATION_OF_FIELD_NUMBER;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Utilities for finding Protobuf definitions of validation rules.
 *
 * @author Dmytro Grankin
 */
class ValidationRulesFinder {

    private static final String PROTO_TYPE_SEPARATOR = ".";

    private ValidationRulesFinder() {
        // Prevent instantiation of this utility class.
    }

    /**
     * Finds Protobuf definitions of validation rules in the specified file descriptor.
     *
     * @param fileDescriptor the descriptor to scan
     * @return a map from a validation rule type name to the target field
     */
    static Map<String, String> find(FileDescriptorProto fileDescriptor) {
        log().debug("Looking up for validation rules in {}.", fileDescriptor.getName());
        final String packagePrefix = getPackagePrefix(fileDescriptor);
        final Map<String, String> rules = newLinkedHashMap();
        for (DescriptorProto messageDescriptor : fileDescriptor.getMessageTypeList()) {
            final Map<String, String> foundRules = scanMessageAndNestedTypes(packagePrefix,
                                                                             messageDescriptor);
            rules.putAll(foundRules);
        }
        log().debug("Found validation rules: {}.", rules.toString());
        return rules;
    }

    private static Map<String, String> scanMessageAndNestedTypes(String packagePrefix,
                                                                 DescriptorProto messageDescriptor) {
        final Map<String, String> rules = newLinkedHashMap();
        if (isValidationRule(messageDescriptor)) {
            final String ruleType = packagePrefix + messageDescriptor.getName();
            final String ruleTarget = getUnknownOptionValue(messageDescriptor,
                                                            VALIDATION_OF_FIELD_NUMBER);
            rules.put(ruleType, ruleTarget);
        }

        final Map<String, String> rulesFromNestedTypes = scanNestedTypes(packagePrefix,
                                                                         messageDescriptor);
        rules.putAll(rulesFromNestedTypes);
        return rules;
    }

    private static Map<String, String> scanNestedTypes(String packagePrefix,
                                                       DescriptorProto messageDescriptor) {
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

    private static String getPackagePrefix(FileDescriptorProto fileDescriptor) {
        return fileDescriptor.getPackage() + PROTO_TYPE_SEPARATOR;
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
