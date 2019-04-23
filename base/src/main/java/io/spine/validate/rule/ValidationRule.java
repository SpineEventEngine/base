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

package io.spine.validate.rule;

import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.type.TypeName;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.protobuf.Descriptors.FieldDescriptor.JavaType.MESSAGE;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * A {@linkplain io.spine.option.OptionsProto#validationOf validation rule}.
 *
 * <p>Contains descriptors related to a particular validation rule.
 */
final class ValidationRule {

    /**
     * The delimiter in a full field name reference.
     */
    private static final String FIELD_NAME_SEPARATOR = ".";

    /**
     * The descriptor for the validation rule message.
     */
    private final Descriptor descriptor;

    /**
     * Descriptors for the target fields of the validation rule.
     */
    private final ImmutableSet<FieldDescriptor> targets;

    /**
     * Creates a new instance.
     *
     * <p>Must be used only in {@link ValidationRules}.
     *
     * @param descriptor
     *         the message descriptor of the validation rule
     * @param targetPaths
     *         the paths to the validation rule targets
     */
    ValidationRule(Descriptor descriptor, Iterable<String> targetPaths) {
        this.descriptor = checkNotNull(descriptor);
        this.targets = constructTargets(descriptor, checkNotNull(targetPaths));
    }

    Descriptor getDescriptor() {
        return descriptor;
    }

    /**
     * Obtains targets for the validation rule.
     *
     * @return an immutable collection of the targets
     */
    ImmutableSet<FieldDescriptor> getTargets() {
        return targets;
    }

    private static ImmutableSet<FieldDescriptor>
    constructTargets(Descriptor ruleDescriptor, Iterable<String> targetPaths) {
        ImmutableSet.Builder<FieldDescriptor> targets = ImmutableSet.builder();
        for (String targetPath : targetPaths) {
            FieldDescriptor target = getTargetDescriptor(targetPath);
            checkRuleFields(ruleDescriptor, target);
            targets.add(target);
        }
        return targets.build();
    }

    /**
     * Obtains rule target descriptor by the specified path.
     *
     * @param targetPath
     *         the path to a validation rule target
     * @return the field descriptor
     */
    private static FieldDescriptor getTargetDescriptor(String targetPath) {
        int typeAndFieldNameBound = targetPath.lastIndexOf(FIELD_NAME_SEPARATOR);
        if (typeAndFieldNameBound == -1) {
            String msg = "Invalid validation rule target `%s`. " +
                    "Proper format is `package.TargetMessage.target_field`.";
            throw newIllegalStateException(msg, targetPath);
        }

        String fieldName = targetPath.substring(typeAndFieldNameBound + 1);
        String targetMessageType = targetPath.substring(0, typeAndFieldNameBound);
        Descriptor message = TypeName.of(targetMessageType)
                                     .messageDescriptor();
        FieldDescriptor field = message.findFieldByName(fieldName);
        if (field == null) {
            throw newIllegalStateException("The field '%s' is not found in the '%s' message.",
                                           fieldName, message.getName());
        }
        return checkTargetType(field);
    }

    private static FieldDescriptor checkTargetType(FieldDescriptor targetDescriptor) {
        if (targetDescriptor.getJavaType() != MESSAGE) {
            String errMsg = "Validation rule target must be a Message." +
                    " Specified type is `%s`.";
            throw newIllegalStateException(errMsg, targetDescriptor.getJavaType());
        }
        return targetDescriptor;
    }

    /**
     * Ensures that fields from the validation rule present in the rule target.
     *
     * <p>A field is considered present if the target has the field with the same name and type.
     *
     * @param rule
     *         the validation rule descriptor
     * @param target
     *         the target of the validation rule
     */
    private static void checkRuleFields(Descriptor rule, FieldDescriptor target) {
        for (FieldDescriptor ruleField : rule.getFields()) {
            Descriptor targetType = target.getMessageType();
            String ruleFieldName = ruleField.getName();
            FieldDescriptor targetField = targetType.findFieldByName(ruleFieldName);
            if (targetField == null) {
                String msg = "The validation rule '%s' declares the field `%s`, " +
                        "which was not found in the `%s` message.";
                throw newIllegalStateException(msg, rule.getFullName(),
                                               ruleFieldName, targetType.getName());
            }

            boolean isCorrectType = ruleField.getJavaType() == targetField.getJavaType();
            if (!isCorrectType) {
                String errMsg = "`%s` must be of type `%s`.";
                throw newIllegalStateException(errMsg, ruleField.getFullName(),
                                               targetField.getJavaType());
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ValidationRule other = (ValidationRule) o;

        return descriptor.equals(other.descriptor);
    }

    @Override
    public int hashCode() {
        return descriptor.hashCode();
    }
}
