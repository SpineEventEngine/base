/*
 * Copyright 2018, TeamDev. All rights reserved.
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

import com.google.common.collect.ImmutableMap;
import com.google.protobuf.DescriptorProtos.FieldOptions;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import io.spine.validate.FieldContext;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.ImmutableMap.builder;

/**
 * Utilities for obtaining Protobuf options extracted from validation rules.
 *
 * @author Dmytro Grankin
 */
public class ValidationRuleOptions {

    /**
     * A map from a field context to the options extracted from a validation rule.
     */
    private static final Map<FieldContext, FieldOptions> options = new Builder().build();

    private ValidationRuleOptions() {
        // Prevent instantiation of this utility class.
    }

    /**
     * Obtains value of the specified option by the specified field context.
     *
     * @param fieldContext the field descriptor to obtain the option
     * @param option       the option to obtain
     * @param <T>          the type of the option
     * @return the {@code Optional} of option value
     *         or {@code Optional.empty()} if there is not option for the field descriptor
     */
    public static <T> Optional<T> getOptionValue(FieldContext fieldContext,
                                                 GeneratedExtension<FieldOptions, T> option) {
        for (FieldContext context : options.keySet()) {
            if (fieldContext.hasSameTargetAndParent(context)) {
                FieldOptions fieldOptions = options.get(context);
                T optionValue = fieldOptions.getExtension(option);
                return Optional.of(optionValue);
            }
        }

        return Optional.empty();
    }

    /**
     * {@code Builder} assembles a map from a field context
     * to the options extracted from a validation rule.
     *
     * <p>Keys of the resulting map are field contexts for the fields of a validation rule targets.
     */
    private static class Builder {

        private final ImmutableMap.Builder<FieldContext, FieldOptions> state = builder();

        private ImmutableMap<FieldContext, FieldOptions> build() {
            for (ValidationRule rule : ValidationRules.getRules()) {
                putAll(rule);
            }
            return state.build();
        }

        private void putAll(ValidationRule validationRule) {
            Descriptor ruleDescriptor = validationRule.getDescriptor();
            Collection<FieldDescriptor> targets = validationRule.getTargets();
            for (FieldDescriptor target : targets) {
                put(ruleDescriptor, target);
            }
        }

        private void put(Descriptor rule, FieldDescriptor target) {
            Descriptor targetType = target.getMessageType();
            for (FieldDescriptor ruleField : rule.getFields()) {
                FieldDescriptor subTarget = targetType.findFieldByName(ruleField.getName());
                FieldContext targetContext = FieldContext.create(target);
                FieldContext subTargetContext = targetContext.forChild(subTarget);
                state.put(subTargetContext, ruleField.getOptions());
            }
        }
    }
}
