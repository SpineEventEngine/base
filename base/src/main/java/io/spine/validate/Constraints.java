/*
 * Copyright 2021, TeamDev. All rights reserved.
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

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import io.spine.tools.code.proto.FieldContext;
import io.spine.tools.code.proto.OneofDeclaration;
import io.spine.type.MessageType;
import io.spine.validate.option.IsRequired;
import io.spine.validate.option.RequiredField;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static io.spine.validate.ConstraintCache.customForType;
import static io.spine.validate.ConstraintCache.forType;
import static io.spine.validate.FieldConstraints.customFactoriesExist;

/**
 * Validation constraints of a single Protobuf message type.
 */
@Immutable
public final class Constraints {

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
        return forType(type, context);
    }

    /**
     * Assembles constraints for the given params.
     *
     * <p>Use {@link #onlyCustom(MessageType, FieldContext)} over this method, as it relies on
     * caching.
     */
    static Constraints loadFor(MessageType type, FieldContext context) {
        ImmutableList.Builder<Constraint> constraintBuilder = ImmutableList.builder();
        type.fields()
            .stream()
            .map(context::forChild)
            .flatMap(FieldConstraints::of)
            .forEach(constraintBuilder::add);
        addRequiredField(type, constraintBuilder);
        scanIsRequired(type, constraintBuilder);
        return new Constraints(constraintBuilder.build());
    }

    private static void addRequiredField(MessageType type,
                                         ImmutableList.Builder<Constraint> constraintBuilder) {
        RequiredField requiredField = new RequiredField();
        if (requiredField.valuePresent(type.descriptor())) {
            Constraint requiredFieldConstraint = requiredField.constraintFor(type);
            constraintBuilder.add(requiredFieldConstraint);
        }
    }

    private static void scanIsRequired(MessageType type,
                                       ImmutableList.Builder<Constraint> builder) {
        IsRequired option = new IsRequired();
        type.descriptor()
            .getOneofs()
            .stream()
            .filter(option::valuePresent)
            .map(descriptor -> new OneofDeclaration(descriptor, type))
            .map(option::constraintFor)
            .forEach(builder::add);
    }

    /**
     * Assembles non-standard constraints from the given message type in the given field context.
     */
    static Constraints onlyCustom(MessageType type, FieldContext context) {
        checkNotNull(type);
        checkNotNull(context);
        return customForType(type, context);
    }

    /**
     * Assembles non-standard constraints for the given params.
     *
     * <p>Use {@link #onlyCustom(MessageType, FieldContext)} over this method, as it relies on
     * caching.
     */
    static Constraints loadCustomFor(MessageType type, FieldContext context) {
        ImmutableList<Constraint> constraints =
                customFactoriesExist()
                ? type.fields()
                      .stream()
                      .map(field -> context.forChild(field.descriptor()))
                      .flatMap(FieldConstraints::customConstraintsFor)
                      .collect(toImmutableList())
                : ImmutableList.of();
        return new Constraints(constraints);
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
}
