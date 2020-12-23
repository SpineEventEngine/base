/*
 * Copyright 2020, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import com.google.common.collect.ImmutableMap;
import com.google.common.flogger.FluentLogger;
import com.google.protobuf.DescriptorProtos.FieldOptions;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import io.spine.code.proto.FieldContext;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMapWithExpectedSize;

/**
 * Provides option value for a field mentioned in an {@link ExternalMessageConstraint}.
 */
public final class ExternalConstraintOptions implements Serializable {

    private static final long serialVersionUID = 0L;

    /**
     * A map from a field context to the options extracted from an external message constraint.
     */
    private final ImmutableMap<FieldContext, FieldOptions> options;

    private ExternalConstraintOptions() {
        this(new Builder().build());
    }

    private ExternalConstraintOptions(ImmutableMap<FieldContext, FieldOptions> options) {
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
     * @return the {@code Optional} of option value or {@code Optional.empty()}
     *         if there is no such an option for the field descriptor
     */
    public static <T> Optional<T>
    getOptionValue(FieldContext fieldContext, GeneratedExtension<FieldOptions, T> option) {
        ImmutableMap<FieldContext, FieldOptions> options = Holder.instance.options;
        for (FieldContext context : options.keySet()) {
            FieldOptions fieldOptions = options.get(context);
            if (fieldContext.hasSameTargetAndParent(context) && fieldOptions.hasExtension(option)) {
                T optionValue = fieldOptions.getExtension(option);
                // A option is set explicitly if it was found in external constraints.
                return Optional.of(optionValue);
            }
        }
        return Optional.empty();
    }

    /**
     * A holder of the {@link ExternalConstraintOptions} instance.
     *
     * @apiNote This class is package-private for allowing constraints options update being
     *         triggered whenever {@link ExternalConstraints} are updated.
     */
    static final class Holder {

        private static final FluentLogger logger = FluentLogger.forEnclosingClass();

        /** The singleton instance. */
        private static ExternalConstraintOptions instance = new ExternalConstraintOptions();

        /** Prevents instantiation from outside. */
        private Holder() {
        }

        /**
         * Extends external constraint options with some more options from the supplied
         * {@code externalConstraints}.
         */
        static void updateFrom(Iterable<ExternalMessageConstraint> externalConstraints) {
            checkNotNull(externalConstraints);
            logger.atFine()
                  .log("Updating external constraint options from constraints `%s`.",
                       externalConstraints);
            ImmutableMap<FieldContext, FieldOptions> currentOptions = instance.options;
            ImmutableMap<FieldContext, FieldOptions> newOptions = new Builder()
                    .buildFrom(externalConstraints);
            Map<FieldContext, FieldOptions> options =
                    newHashMapWithExpectedSize(currentOptions.size() + newOptions.size());
            options.putAll(currentOptions);
            options.putAll(newOptions);
            instance = new ExternalConstraintOptions(ImmutableMap.copyOf(options));
        }
    }

    /**
     * Assembles {@linkplain #options}.
     */
    private static class Builder {

        private final Map<FieldContext, FieldOptions> state = new HashMap<>();

        private ImmutableMap<FieldContext, FieldOptions> build() {
            return buildFrom(ExternalConstraints.all());
        }

        private ImmutableMap<FieldContext, FieldOptions>
        buildFrom(Iterable<ExternalMessageConstraint> constraints) {
            for (ExternalMessageConstraint constraint : constraints) {
                putAll(constraint);
            }
            return ImmutableMap.copyOf(state);
        }

        private void putAll(ExternalMessageConstraint constraint) {
            Descriptor constraintDescriptor = constraint.getDescriptor();
            Collection<FieldDescriptor> targets = constraint.getTargets();
            for (FieldDescriptor target : targets) {
                put(constraintDescriptor, target);
            }
        }

        private void put(Descriptor constraint, FieldDescriptor target) {
            Descriptor targetType = target.getMessageType();
            for (FieldDescriptor constraintField : constraint.getFields()) {
                FieldDescriptor subTarget = targetType.findFieldByName(constraintField.getName());
                FieldContext targetContext = FieldContext.create(target);
                FieldContext subTargetContext = targetContext.forChild(subTarget);
                state.put(subTargetContext, constraintField.getOptions());
            }
        }
    }
}
