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

import com.google.common.base.Objects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import io.spine.code.proto.FieldContext;
import io.spine.type.MessageType;
import io.spine.validate.option.RequiredField;

import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static io.spine.validate.FieldConstraints.customFactoriesExist;

/**
 * Validation constraints of a single Protobuf message type.
 */
@Immutable
public final class Constraints {

    private static final int CACHE_SIZE = 1000;

    private static final LoadingCache<CacheKey, Constraints> allConstraints = CacheBuilder
            .newBuilder()
            .maximumSize(CACHE_SIZE)
            .build(new ConstraintCacheLoader());
    private static final LoadingCache<CacheKey, Constraints> customConstraints = CacheBuilder
            .newBuilder()
            .maximumSize(CACHE_SIZE)
            .build(new CustomConstraintCacheLoader());

    private final ImmutableList<Constraint> constraints;

    private Constraints(ImmutableList<Constraint> constraints) {
        this.constraints = constraints;
    }

    /**
     * Assembles constraints from the given message type.
     */
    public static Constraints of(MessageType type) {
        return of(type, FieldContext.empty());
    }

    /**
     * Assembles constraints from the given message type in the given field context.
     *
     * <p>The field context is not empty if the constraints must consider messages values of a field
     * rather than independent messages.
     */
    public static Constraints of(MessageType type, FieldContext context) {
        checkNotNull(type);
        checkNotNull(context);
        CacheKey key = new CacheKey(type, context);
        return allConstraints.getUnchecked(key);
    }

    static Constraints onlyCustom(MessageType type, FieldContext context) {
        checkNotNull(type);
        checkNotNull(context);
        CacheKey key = new CacheKey(type, context);
        return customConstraints.getUnchecked(key);
    }

    /**
     * Feeds these constraints to the given {@link ConstraintTranslator} and obtains the result of
     * translation.
     *
     * @param constraintTranslator
     *         the {@code ConstraintTranslator} which reduces the constrains to
     *         a value of {@code T}
     * @param <T>
     *         type of the translation result
     * @return the translation result
     */
    public <T> T runThrough(ConstraintTranslator<T> constraintTranslator) {
        constraints.forEach(c -> c.accept(constraintTranslator));
        return constraintTranslator.translate();
    }

    private static final class CacheKey {

        private final MessageType type;
        private final FieldContext fieldContext;

        private CacheKey(MessageType type, FieldContext context) {
            this.type = checkNotNull(type);
            this.fieldContext = checkNotNull(context);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof CacheKey)) {
                return false;
            }
            CacheKey key = (CacheKey) o;
            return Objects.equal(type, key.type) &&
                    Objects.equal(fieldContext, key.fieldContext);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(type, fieldContext);
        }
    }

    private static final class ConstraintCacheLoader extends CacheLoader<CacheKey, Constraints> {

        @Override
        public Constraints load(CacheKey key) {
            ImmutableList.Builder<Constraint> constraintBuilder = ImmutableList.builder();
            fieldConstraints(key.type, key.fieldContext)
                    .forEach(constraintBuilder::add);
            addRequiredField(key.type, constraintBuilder);
            return new Constraints(constraintBuilder.build());
        }

        private static Stream<Constraint> fieldConstraints(MessageType type, FieldContext context) {
            return type
                    .fields()
                    .stream()
                    .map(field -> context.forChild(field.descriptor()))
                    .flatMap(FieldConstraints::of);
        }

        private static void addRequiredField(MessageType type,
                                             ImmutableList.Builder<Constraint> constraintBuilder) {
            RequiredField requiredField = new RequiredField();
            if (requiredField.valuePresent(type.descriptor())) {
                Constraint requiredFieldConstraint = requiredField.constraintFor(type);
                constraintBuilder.add(requiredFieldConstraint);
            }
        }
    }

    private static final class CustomConstraintCacheLoader extends CacheLoader<CacheKey, Constraints> {

        @Override
        public Constraints load(CacheKey key) {
            ImmutableList<Constraint> constraintBuilder =
                    customFieldConstraints(key.type, key.fieldContext)
                            .collect(toImmutableList());
            return new Constraints(constraintBuilder);
        }

        private static Stream<Constraint>
        customFieldConstraints(MessageType type, FieldContext context) {
            return customFactoriesExist()
                   ? type.fields()
                         .stream()
                         .map(field -> context.forChild(field.descriptor()))
                         .flatMap(FieldConstraints::customConstraintsFor)
                   : Stream.of();
        }
    }
}
