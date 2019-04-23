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

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.DescriptorProtos;
import io.spine.annotation.Internal;
import io.spine.logging.Logging;
import io.spine.type.KnownTypes;
import io.spine.type.MessageType;
import org.slf4j.Logger;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.Sets.newHashSetWithExpectedSize;
import static io.spine.util.Exceptions.newIllegalArgumentException;

/**
 * A collection of {@linkplain ValidationRule validation rules} known to the application.
 *
 * <p>During initialization of this class, definitions of validation rules are verified.
 * If an invalid validation rule was found, a runtime exception will be thrown.
 */
public final class ValidationRules implements Serializable {

    private static final long serialVersionUID = 0L;

    /**
     * An instance of {@link Splitter} for the string option values.
     *
     * <p>Targets the string options which list multiple values separated with a {@code ,} (comma)
     * symbol.
     */
    private static final Splitter optionSplitter = Splitter.on(',');

    @SuppressWarnings("TransientFieldNotInitialized") // Instance is substituted on deserialization.
    private final transient ImmutableSet<ValidationRule> rules;

    private ValidationRules() {
        this(rulesFor(KnownTypes.instance()));
    }

    private ValidationRules(ImmutableSet<ValidationRule> rules) {
        this.rules = checkNotNull(rules);
    }

    /**
     * Obtains validation rules known to the application.
     *
     * @return the immutable collection of validation rules
     */
    static ImmutableSet<ValidationRule> all() {
        return Holder.instance.rules;
    }

    /**
     * Extends validation rules with some more rules from the {@code types}.
     */
    @Internal
    public static void updateFrom(ImmutableSet<MessageType> types) {
        Holder.updateFrom(types);
    }

    /**
     * Builds validation rules for known Protobuf types.
     */
    private static ImmutableSet<ValidationRule> rulesFor(KnownTypes knownTypes) {
        ImmutableSet<MessageType> types = checkNotNull(knownTypes)
                .asTypeSet()
                .messageTypes();
        return rulesFor(types);
    }

    /**
     * Builds validation rules for supplied message types.
     */
    private static ImmutableSet<ValidationRule> rulesFor(ImmutableSet<MessageType> types) {
        return checkNotNull(types)
                .stream()
                .filter(new IsValidationRule())
                .map(ValidationRules::toValidationRule)
                .collect(toImmutableSet());
    }

    /**
     * Builds a validation rule from the supplied message type.
     */
    private static ValidationRule toValidationRule(MessageType type) {
        checkNotNull(type);
        ValidationOf validationOf = new ValidationOf();
        String ruleTargets = validationOf
                .valueFrom(type.toProto())
                .orElseThrow(() -> newIllegalArgumentException(type.name()
                                                                   .value()));
        Collection<String> parsedPaths = optionSplitter.splitToList(ruleTargets);
        return new ValidationRule(type.descriptor(), parsedPaths);
    }

    /**
     * Re-creates de-serialized instance.
     */
    private Object readResolve() {
        return new ValidationRules();
    }

    /**
     * A holder of the {@link ValidationRules} instance.
     */
    private static class Holder {

        private static final Logger log = Logging.get(Holder.class);

        /** The singleton instance. */
        private static ValidationRules instance = new ValidationRules();

        /** Prevents instantiation from outside. */
        private Holder() {
        }

        /**
         * Extends validation rules with some more rules from the supplied {@code types}.
         *
         * <p>Triggers validation rule options
         * {@link ValidationRuleOptions.Holder#updateFrom(Iterable) update}.
         */
        private static void updateFrom(ImmutableSet<MessageType> types) {
            checkNotNull(types);
            log.debug("Updating validation rules from types {}.", types);
            ImmutableSet<ValidationRule> currentRules = instance.rules;
            ImmutableSet<ValidationRule> newRules = rulesFor(types);
            Set<ValidationRule> rules =
                    newHashSetWithExpectedSize(currentRules.size() + newRules.size());
            rules.addAll(currentRules);
            rules.addAll(newRules);
            instance = new ValidationRules(ImmutableSet.copyOf(rules));
            ValidationRuleOptions.Holder.updateFrom(instance.rules);
        }
    }

    /**
     * Determines if a {@link MessageType} contains a validation rule.
     */
    private static class IsValidationRule implements Predicate<MessageType>, Logging {

        @Override
        public boolean test(MessageType input) {
            checkNotNull(input);
            DescriptorProtos.DescriptorProto proto = input.toProto();
            boolean result = new ValidationOf().valueFrom(proto)
                                               .isPresent();
            _debug("[IsValidationRule] Tested {} with the result of {}.", proto.getName(), result);
            return result;
        }

        @Override
        public String toString() {
            return "IsValidationRule predicate over MessageType";
        }
    }
}
