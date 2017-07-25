package io.spine.gradle.compiler.lookup.validate;

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

    Map<String, String> findRules() {
        log().debug("Looking up for validation rules in {}.", fileDescriptor.getName());
        final Map<String, String> rules = newLinkedHashMap();
        for (DescriptorProto messageDescriptor : fileDescriptor.getMessageTypeList()) {
            scanMessage(rules, messageDescriptor);
        }
        log().debug("Found validation rules: {}.", rules.toString());
        return rules;
    }

    private void scanMessage(Map<String, String> rules, DescriptorProto messageDescriptor) {
        if (isValidationRule(messageDescriptor)) {
            final String ruleType = packagePrefix + messageDescriptor.getName();
            final String ruleTarget = getUnknownOptionValue(messageDescriptor,
                                                            VALIDATION_OF_FIELD_NUMBER);
            rules.put(ruleType, ruleTarget);
        }

        for (DescriptorProto nestedMessageDescriptor : messageDescriptor.getNestedTypeList()) {
            scanMessage(rules, nestedMessageDescriptor);
        }
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
