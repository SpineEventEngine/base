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

package io.spine.validate;

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
 * A collection of {@linkplain ExternalMessageConstraint external constrains} known to
 * the application.
 *
 * <p>During initialization of this class, definitions of external constraints are verified to
 * conform the {@link io.spine.option.OptionsProto#constraintFor constaint} contract.
 * If an invalid constraint is found, a runtime exception is thrown.
 */
public final class ExternalConstraints implements Serializable {

    private static final long serialVersionUID = 0L;

    /**
     * An instance of {@link Splitter} for the string option values.
     *
     * <p>Targets the string options which list multiple values separated with a {@code ,} (comma)
     * symbol.
     */
    private static final Splitter optionSplitter = Splitter.on(',');

    @SuppressWarnings("TransientFieldNotInitialized") // Instance is substituted on deserialization.
    private final transient ImmutableSet<ExternalMessageConstraint> constraints;

    private ExternalConstraints() {
        this(constraintsFor(KnownTypes.instance()));
    }

    private ExternalConstraints(ImmutableSet<ExternalMessageConstraint> constraints) {
        this.constraints = checkNotNull(constraints);
    }

    /**
     * Obtains external constraints known to the application.
     *
     * @return the immutable collection of external constraints
     */
    static ImmutableSet<ExternalMessageConstraint> all() {
        return Holder.instance.constraints;
    }

    /**
     * Extends external constraints with some more constraints from the {@code types}.
     */
    @Internal
    public static void updateFrom(ImmutableSet<MessageType> types) {
        Holder.updateFrom(types);
    }

    /**
     * Builds external constraints for known Protobuf types.
     */
    private static ImmutableSet<ExternalMessageConstraint> constraintsFor(KnownTypes knownTypes) {
        ImmutableSet<MessageType> types = checkNotNull(knownTypes)
                .asTypeSet()
                .messageTypes();
        return constraintsFor(types);
    }

    /**
     * Builds external constraints for supplied message types.
     */
    private static ImmutableSet<ExternalMessageConstraint>
    constraintsFor(ImmutableSet<MessageType> types) {
        return checkNotNull(types)
                .stream()
                .filter(new HasExternalConstraint())
                .map(ExternalConstraints::toConstraint)
                .collect(toImmutableSet());
    }

    /**
     * Builds an external constraint from the supplied message type.
     */
    private static ExternalMessageConstraint toConstraint(MessageType type) {
        checkNotNull(type);
        ConstraintFor constraintFor = new ConstraintFor();
        String constraintTargets = constraintFor
                .valueFrom(type.toProto())
                .orElseThrow(() -> newIllegalArgumentException(type.name()
                                                                   .value()));
        Collection<String> parsedPaths = optionSplitter.splitToList(constraintTargets);
        return new ExternalMessageConstraint(type.descriptor(), parsedPaths);
    }

    /**
     * Re-creates de-serialized instance.
     */
    private Object readResolve() {
        return new ExternalConstraints();
    }

    /**
     * A holder of the {@link ExternalConstraints} instance.
     */
    private static class Holder {

        private static final Logger log = Logging.get(Holder.class);

        /** The singleton instance. */
        private static ExternalConstraints instance = new ExternalConstraints();

        /** Prevents instantiation from outside. */
        private Holder() {
        }

        /**
         * Extends external constraints with some more constraints from the supplied {@code types}.
         *
         * <p>Triggers external constraint options
         * {@link ExternalConstraintOptions.Holder#updateFrom(Iterable) update}.
         */
        private static void updateFrom(ImmutableSet<MessageType> types) {
            checkNotNull(types);
            log.debug("Updating external constraints from types {}.", types);
            ImmutableSet<ExternalMessageConstraint> currentConstraints = instance.constraints;
            ImmutableSet<ExternalMessageConstraint> newConstraints = constraintsFor(types);
            Set<ExternalMessageConstraint> constraints =
                    newHashSetWithExpectedSize(currentConstraints.size() + newConstraints.size());
            constraints.addAll(currentConstraints);
            constraints.addAll(newConstraints);
            instance = new ExternalConstraints(ImmutableSet.copyOf(constraints));
            ExternalConstraintOptions.Holder.updateFrom(newConstraints);
        }
    }

    /**
     * Determines if a {@link MessageType} contains an external constraint.
     */
    private static class HasExternalConstraint implements Predicate<MessageType>, Logging {

        @Override
        public boolean test(MessageType input) {
            checkNotNull(input);
            DescriptorProtos.DescriptorProto proto = input.toProto();
            boolean result = new ConstraintFor().valueFrom(proto)
                                                .isPresent();
            _debug("[HasExternalConstraint] Tested {} with the result of {}.",
                   proto.getName(), result);
            return result;
        }

        @Override
        public String toString() {
            return "HasExternalConstraint predicate over MessageType";
        }
    }
}
