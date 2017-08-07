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

package io.spine.validate.rules;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.type.TypeName;

import java.util.Collection;

import static com.google.protobuf.Descriptors.FieldDescriptor.JavaType.MESSAGE;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * A {@linkplain io.spine.option.OptionsProto#validationOf validation rule}.
 *
 * <p>Aggregates descriptors related to a particular validation rule.
 *
 * @author Dmytro Grankin
 */
class ValidationRule {

    private static final String PROTO_TYPE_SEPARATOR = ".";

    /**
     * The descriptor for the validation rule message.
     */
    private final Descriptor descriptor;

    /**
     * Descriptors for the target fields of the validation rule.
     */
    private final Collection<FieldDescriptor> targets;

    ValidationRule(String typeName, Iterable<String> targetPaths) {
        this.descriptor = TypeName.of(typeName)
                                  .getDescriptor();
        this.targets = constructTargets(descriptor, targetPaths);
    }

    Descriptor getDescriptor() {
        return descriptor;
    }

    /**
     * Obtains targets for the validation rule.
     *
     * @return an immutable collection of the targets
     */
    @SuppressWarnings("ReturnOfCollectionOrArrayField") // As return value is
                                                        // an immutable collection.
    Collection<FieldDescriptor> getTargets() {
        return targets;
    }

    private static Collection<FieldDescriptor> constructTargets(Descriptor ruleDescriptor,
                                                                Iterable<String> targetPaths) {
        final ImmutableCollection.Builder<FieldDescriptor> targets = ImmutableSet.builder();
        for (String targetPath : targetPaths) {
            final FieldDescriptor target = getTargetDescriptor(targetPath);
            checkRuleFields(ruleDescriptor, target);
            targets.add(target);
        }
        return targets.build();
    }

    /**
     * Obtains rule target descriptor by the specified path.
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
        return checkTargetType(field);
    }

    private static FieldDescriptor checkTargetType(FieldDescriptor targetDescriptor) {
        if (targetDescriptor.getJavaType() != MESSAGE) {
            final String errMsg = "Validation rule target must be a Message." +
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
     * @param rule   the validation rule descriptor
     * @param target the target of the validation rule
     */
    private static void checkRuleFields(Descriptor rule, FieldDescriptor target) {
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
