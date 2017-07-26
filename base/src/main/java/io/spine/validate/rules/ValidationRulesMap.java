package io.spine.validate.rules;

import com.google.common.collect.ImmutableBiMap;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.type.TypeName;

import java.util.Properties;
import java.util.Set;

import static com.google.protobuf.Descriptors.FieldDescriptor.JavaType.MESSAGE;
import static io.spine.util.Exceptions.newIllegalStateException;
import static io.spine.util.PropertyFiles.loadAllProperties;

/**
 * A map from a validation rule descriptor to the field descriptor of the rule target.
 *
 * @author Dmytro Grankin
 */
class ValidationRulesMap {

    /**
     * A path to the file, which contains validation rules and their target fields paths.
     */
    private static final String PROPS_FILE_NAME = "validation_rules.properties";
    private static final String PROTO_TYPE_SEPARATOR = ".";

    private static final ImmutableBiMap<Descriptor, FieldDescriptor> rules = buildValidationRulesMap();

    private ValidationRulesMap() {
        // Prevent instantiation of this class.
    }

    /**
     * Obtains validation rules map instance.
     *
     * @return the immutable map
     */
    @SuppressWarnings("ReturnOfCollectionOrArrayField") // As return value is immutable collection.
    static ImmutableBiMap<Descriptor, FieldDescriptor> getInstance() {
        return rules;
    }

    private static ImmutableBiMap<Descriptor, FieldDescriptor> buildValidationRulesMap() {
        final Set<Properties> propertiesSet = loadAllProperties(PROPS_FILE_NAME);
        final Builder builder = new Builder(propertiesSet);
        return builder.build();
    }

    private static class Builder {

        private final Iterable<Properties> properties;
        private final ImmutableBiMap.Builder<Descriptor, FieldDescriptor> builder;

        private Builder(Iterable<Properties> properties) {
            this.properties = properties;
            this.builder = ImmutableBiMap.builder();
        }

        private ImmutableBiMap<Descriptor, FieldDescriptor> build() {
            for (Properties props : this.properties) {
                put(props);
            }
            return builder.build();
        }

        private void put(Properties properties) {
            for (String validationRuleType : properties.stringPropertyNames()) {
                final String validationRuleTarget = properties.getProperty(validationRuleType);
                final Descriptor rule = TypeName.of(validationRuleType)
                                                .getDescriptor();
                final FieldDescriptor target = getTargetDescriptor(validationRuleTarget);
                checkValidationRule(rule, target);
                builder.put(rule, target);
            }
        }

        private static FieldDescriptor getTargetDescriptor(String target) {
            final int typeAndFieldNameBound = target.lastIndexOf(PROTO_TYPE_SEPARATOR);
            if (typeAndFieldNameBound == -1) {
                final String msg = "Invalid validation rule target `%s`. " +
                        "Proper format is `package.TargetMessage.target_field`.";
                throw newIllegalStateException(msg, target);
            }

            final String fieldName = target.substring(typeAndFieldNameBound + 1);
            final String targetMessageType = target.substring(0, typeAndFieldNameBound);
            final Descriptor message = TypeName.of(targetMessageType)
                                               .getDescriptor();
            final FieldDescriptor field = message.findFieldByName(fieldName);
            if (field == null) {
                throw newIllegalStateException("`%s` has not the field `%s`.",
                                               message.getName(), fieldName);
            }
            return field;
        }

        private static void checkValidationRule(Descriptor rule, FieldDescriptor target) {
            if (target.getJavaType() != MESSAGE) {
                final String errMsg = "Validation rule target should be a message." +
                        " Specified type is `%s`.";
                throw newIllegalStateException(errMsg, target.getJavaType());
            }

            for (FieldDescriptor ruleField : rule.getFields()) {
                final Descriptor targetType = target.getMessageType();
                final String ruleFieldName = ruleField.getName();
                final FieldDescriptor targetField = targetType.findFieldByName(ruleFieldName);
                if (targetField == null) {
                    final String msg = "Validation rule target `%s` of type `%s`" +
                            " has not field `%s`.";
                    throw newIllegalStateException(msg, target.getFullName(),
                                                   targetType.getName(), ruleFieldName);
                }

                final boolean isCorrectType = ruleField.getJavaType() == targetField.getJavaType();
                if (!isCorrectType) {
                    final String errMsg = "`%s` should be of type `%s`.";
                    throw newIllegalStateException(errMsg, ruleField.getFullName(),
                                                   targetField.getJavaType());
                }
            }
        }
    }
}
