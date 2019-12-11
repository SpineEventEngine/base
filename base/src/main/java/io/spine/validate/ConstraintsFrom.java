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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.spine.code.proto.FieldContext;
import io.spine.validate.option.FieldValidatingOption;
import io.spine.validate.option.ValidatingOptionFactory;
import io.spine.validate.option.ValidatingOptionsLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.Streams.concat;

/**
 * A builder of {@link Constraint}s for a given field.
 */
final class ConstraintsFrom {

    private static final ImmutableSet<ValidatingOptionFactory> factories =
            ValidatingOptionsLoader.INSTANCE.implementations();

    private final List<FieldValidatingOption<?>> options = new ArrayList<>();
    private final List<FieldValidatingOption<?>> collectionOptions = new ArrayList<>();

    static ConstraintsFrom
    factories(Function<ValidatingOptionFactory, Set<FieldValidatingOption<?>>> selector) {
        ImmutableSet<FieldValidatingOption<?>> options = fromFactories(selector);
        ConstraintsFrom result = new ConstraintsFrom();
        result.options.addAll(options);
        return result;
    }

    ConstraintsFrom and(FieldValidatingOption<?>... options) {
        this.options.addAll(ImmutableList.copyOf(options));
        return this;
    }

    ConstraintsFrom andForCollections(FieldValidatingOption<?>... options) {
        collectionOptions.addAll(ImmutableList.copyOf(options));
        return this;
    }

    Stream<Constraint> forField(FieldContext field) {
        Stream<Constraint> constraints = toConstraints(options, field);
        if (field.targetDeclaration().isCollection()) {
            Stream<Constraint> collectionConstraints = toConstraints(collectionOptions, field);
            constraints = concat(constraints, collectionConstraints);
        }
        return constraints;
    }

    private static Stream<Constraint>
    toConstraints(List<FieldValidatingOption<?>> options, FieldContext field) {
        return options.stream()
                      .filter(option -> option.shouldValidate(field))
                      .map(option -> option.constraintFor(field));
    }

    private static ImmutableSet<FieldValidatingOption<?>>
    fromFactories(Function<ValidatingOptionFactory, Set<FieldValidatingOption<?>>> selector) {
        return factories.stream()
                        .map(selector)
                        .flatMap(Set::stream)
                        .collect(toImmutableSet());
    }
}
