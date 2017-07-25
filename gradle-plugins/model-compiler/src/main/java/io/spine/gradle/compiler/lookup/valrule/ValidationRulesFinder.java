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
