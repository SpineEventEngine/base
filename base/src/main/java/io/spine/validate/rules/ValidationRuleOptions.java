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

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.DescriptorProtos.FieldOptions;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newLinkedList;

/**
 * Utilities for obtaining Protobuf options extracted from validation rules.
 *
 * @author Dmytro Grankin
 */
public class ValidationRuleOptions {

    /**
     * A map between validation rules targets and the sub targets.
     */
    private static final Map<FieldDescriptor, List<ValidationRuleSubTarget>> targets =
            new Builder().build();

    private ValidationRuleOptions() {
        // Prevent instantiation of this utility class.
    }

    /**
     * Obtains value of the specified option for the specified field descriptor.
     *
     * @param fieldDescriptor       the field descriptor to obtain the option
     * @param parentFieldDescriptor the parent field descriptor for the field
     * @param option                the option to obtain
     * @param <T>                   the type of the option
     * @return the {@code Optional} of option value
     *         or {@code Optional.absent()} if there is not option for the field descriptor
     */
    public static <T> Optional<T> getOptionValue(FieldDescriptor fieldDescriptor,
                                                 FieldDescriptor parentFieldDescriptor,
                                                 GeneratedExtension<FieldOptions, T> option) {
        final List<ValidationRuleSubTarget> subTargets = targets.get(parentFieldDescriptor);
        for (ValidationRuleSubTarget subTarget : subTargets) {
            final FieldDescriptor subTargetDescriptor = subTarget.getDescriptor();
            if (subTargetDescriptor.equals(fieldDescriptor)) {
                final T optionValue = subTarget.getOptions()
                                               .getExtension(option);
                return Optional.fromNullable(optionValue);
            }
        }
        return Optional.absent();
    }

    private static class Builder {

        private final ImmutableMap.Builder<FieldDescriptor, List<ValidationRuleSubTarget>> builder =
                ImmutableMap.builder();

        private Map<FieldDescriptor, List<ValidationRuleSubTarget>> build() {
            final Map<Descriptor, FieldDescriptor> rules = ValidationRulesMap.getInstance();
            for (Descriptor rule : rules.keySet()) {
                final FieldDescriptor target = rules.get(rule);
                put(rule, target);
            }
            return builder.build();
        }

        private void put(Descriptor rule, FieldDescriptor target) {
            final Descriptor targetType = target.getMessageType();
            final List<ValidationRuleSubTarget> subTargets = newLinkedList();
            for (FieldDescriptor ruleField : rule.getFields()) {
                final FieldDescriptor subField = targetType.findFieldByName(ruleField.getName());
                final ValidationRuleSubTarget subTarget =
                        new ValidationRuleSubTarget(subField, ruleField.getOptions());
                subTargets.add(subTarget);
            }
            builder.put(target, subTargets);
        }
    }
}
