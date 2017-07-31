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

package io.spine.validate;

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
 * {@code ValidationRules} provides access to a map
 * from a validation rule descriptor to the field descriptor of the rule target.
 *
 * @author Dmytro Grankin
 */
class ValidationRules {

    /**
     * A path to the file, which contains validation rules and their target fields paths.
     */
    @SuppressWarnings("DuplicateStringLiteralInspection") // To avoid the undesirable dependency
                                                          // on the model compiler.
    private static final String PROPS_FILE_NAME = "validation_rules.properties";
    private static final String PROTO_TYPE_SEPARATOR = ".";

    private static final ImmutableBiMap<Descriptor, FieldDescriptor> rules = build();

    private ValidationRules() {
        // Prevent instantiation of this class.
    }

    /**
     * Obtains the validation rules map.
     *
     * @return the immutable map
     */
    @SuppressWarnings("ReturnOfCollectionOrArrayField") // As the return value
                                                        // is an immutable collection.
    static ImmutableBiMap<Descriptor, FieldDescriptor> getMap() {
        return rules;
    }

    private static ImmutableBiMap<Descriptor, FieldDescriptor> build() {
        final Set<Properties> propertiesSet = loadAllProperties(PROPS_FILE_NAME);
        final Builder builder = new Builder(propertiesSet);
        return builder.build();
    }

    /**
     * {@code Builder} assembles the validation rules map from the specified {@code Properties}.
     */
    private static class Builder {

        /**
         * Properties to process.
         *
         * <p>Properties must contain the list of validation rules and the targets for the rules.
         */
        private final Iterable<Properties> properties;

        /**
         * The state of the validation rules map to be assembled.
         */
        private final ImmutableBiMap.Builder<Descriptor, FieldDescriptor> state;

        private Builder(Iterable<Properties> properties) {
            this.properties = properties;
            this.state = ImmutableBiMap.builder();
        }

        private ImmutableBiMap<Descriptor, FieldDescriptor> build() {
            for (Properties props : this.properties) {
                put(props);
            }
            return state.build();
        }

        /**
         * Puts the validation rules obtained from the specified properties to the {@link #state}.
         *
         * @param properties the properties to process
         * @throws IllegalStateException if an entry from the properties contains invalid data
         */
        private void put(Properties properties) {
            for (String validationRuleType : properties.stringPropertyNames()) {
                final String ruleTargetPath = properties.getProperty(validationRuleType);
                final Descriptor rule = TypeName.of(validationRuleType)
                                                .getDescriptor();
                final FieldDescriptor target = getTargetDescriptor(ruleTargetPath);
                checkValidationRule(rule, target);
                state.put(rule, target);
            }
        }

        /**
         * Obtains {@link FieldDescriptor} by the specified path.
         *
         * @param targetPath the path to a validation rule target
         * @return the field descriptor
         */
        private static FieldDescriptor getTargetDescriptor(String targetPath) {
            final int typeAndFieldNameBound = targetPath.lastIndexOf(PROTO_TYPE_SEPARATOR);
            if (typeAndFieldNameBound == -1) {
                final String msg = "Invalid validation rule target `%s`. " +
                        "Proper format is `package.TargetMessage.target_field`.";
                throw newIllegalStateException(msg, targetPath);
            }

            final String fieldName = targetPath.substring(typeAndFieldNameBound + 1);
            final String targetMessageType = targetPath.substring(0, typeAndFieldNameBound);
            final Descriptor message = TypeName.of(targetMessageType)
                                               .getDescriptor();
            final FieldDescriptor field = message.findFieldByName(fieldName);
            if (field == null) {
                throw newIllegalStateException("The field '%s' is not found in the '%s' message.",
                                               fieldName, message.getName());
            }
            return field;
        }

        /**
         * Ensures that the specified rule is valid for the specified target.
         *
         * @param rule the validation rule
         * @param target the target of the validation rule
         */
        private static void checkValidationRule(Descriptor rule, FieldDescriptor target) {
            if (target.getJavaType() != MESSAGE) {
                final String errMsg = "Validation rule target must be a Message." +
                        " Specified type is `%s`.";
                throw newIllegalStateException(errMsg, target.getJavaType());
            }

            for (FieldDescriptor ruleField : rule.getFields()) {
                final Descriptor targetType = target.getMessageType();
                final String ruleFieldName = ruleField.getName();
                final FieldDescriptor targetField = targetType.findFieldByName(ruleFieldName);
                if (targetField == null) {
                    final String msg = "The validation rule '%s' declares the field `%s`, " +
                            "which was not found in the `%s` message.";
                    throw newIllegalStateException(msg, rule.getFullName(),
                                                   ruleFieldName, targetType.getName());
                }

                final boolean isCorrectType = ruleField.getJavaType() == targetField.getJavaType();
                if (!isCorrectType) {
                    final String errMsg = "`%s` must be of type `%s`.";
                    throw newIllegalStateException(errMsg, ruleField.getFullName(),
                                                   targetField.getJavaType());
                }
            }
        }
    }
}
