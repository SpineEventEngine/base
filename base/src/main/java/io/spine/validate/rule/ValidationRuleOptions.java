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

import com.google.common.collect.ImmutableMap;
import com.google.protobuf.DescriptorProtos.FieldOptions;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import io.spine.annotation.Internal;
import io.spine.code.proto.FieldContext;
import io.spine.logging.Logging;
import org.slf4j.Logger;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides option value for a field mentioned in a validation rule.
 */
public final class ValidationRuleOptions implements Serializable {

    private static final long serialVersionUID = 0L;

    /**
     * A map from a field context to the options extracted from a validation rule.
     */
    private final ImmutableMap<FieldContext, FieldOptions> options;

    private ValidationRuleOptions() {
        this(new Builder().build());
    }

    private ValidationRuleOptions(ImmutableMap<FieldContext, FieldOptions> options) {
        this.options = options;
    }

    /**
     * Obtains value of the specified option by the specified field context.
     *
     * @param fieldContext
     *         the field descriptor to obtain the option
     * @param option
     *         the option to obtain
     * @param <T>
     *         the type of the option value
     * @return the {@code Optional} of option value
     *         or {@code Optional.empty()} if there is not option for the field descriptor
     */
    public static <T> Optional<T>
    getOptionValue(FieldContext fieldContext, GeneratedExtension<FieldOptions, T> option) {
        ImmutableMap<FieldContext, FieldOptions> options = Holder.instance.options;
        for (FieldContext context : options.keySet()) {
            if (fieldContext.hasSameTargetAndParent(context)) {
                FieldOptions fieldOptions = options.get(context);
                T optionValue = fieldOptions.getExtension(option);
                // A option is set explicitly if it was found in validation rules.
                return Optional.of(optionValue);
            }
        }
        return Optional.empty();
    }

    /**
     * A holder of the {@link ValidationRuleOptions} instance.
     *
     * @apiNote This class is package-private for allowing rule options update being
     *         triggered whenever {@link ValidationRules} are updated.
     */
    @Internal
    static class Holder {

        private static final Logger log = Logging.get(Holder.class);

        /** The singleton instance. */
        private static ValidationRuleOptions instance = new ValidationRuleOptions();

        /** Prevents instantiation from outside. */
        private Holder() {
        }

        /**
         * Extends validation rule options with some more options from the supplied
         * {@code validationRules}.
         */
        static void updateFrom(Iterable<ValidationRule> validationRules) {
            checkNotNull(validationRules);
            log.debug("Updating validation rule options from rules {}.", validationRules);
            ImmutableMap<FieldContext, FieldOptions> options = ImmutableMap
                    .<FieldContext, FieldOptions>builder()
                    .putAll(instance.options)
                    .putAll(new Builder().buildFrom(validationRules))
                    .build();
            instance = new ValidationRuleOptions(options);
        }
    }

    /**
     * Assembles {@linkplain #options}.
     */
    private static class Builder {

        private final ImmutableMap.Builder<FieldContext, FieldOptions> state =
                ImmutableMap.builder();

        private ImmutableMap<FieldContext, FieldOptions> build() {
            return buildFrom(ValidationRules.all());
        }

        private ImmutableMap<FieldContext, FieldOptions>
        buildFrom(Iterable<ValidationRule> validationRules) {
            for (ValidationRule rule : validationRules) {
                putAll(rule);
            }
            return state.build();
        }

        private void putAll(ValidationRule rule) {
            Descriptor ruleDescriptor = rule.getDescriptor();
            Collection<FieldDescriptor> targets = rule.getTargets();
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
