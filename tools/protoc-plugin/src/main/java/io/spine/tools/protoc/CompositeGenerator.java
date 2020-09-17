/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.tools.protoc;

import com.google.common.collect.ImmutableList;
import io.spine.type.Type;

import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.Lists.newArrayList;

/**
 * A generator which calls other generators and merges their results.
 */
public final class CompositeGenerator extends CodeGenerator {

    private final ImmutableList<? extends CodeGenerator> generators;

    private CompositeGenerator(Builder builder) {
        super();
        this.generators = ImmutableList.copyOf(builder.generators);
    }

    @Override
    protected Collection<CompilerOutput> generate(Type<?, ?> type) {
        return generators.stream()
                         .flatMap(generator -> generator.generate(type).stream())
                         .collect(toImmutableSet());
    }

    /**
     * Creates a new builder for the instances of this type.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * A builder for the {@code CompositeGenerator} instances.
     */
    public static final class Builder {

        private final List<CodeGenerator> generators = newArrayList();

        /**
         * Prevents direct instantiation.
         */
        private Builder() {
        }

        public Builder add(CodeGenerator generator) {
            checkNotNull(generator);
            generators.add(generator);
            return this;
        }

        /**
         * Creates a new instance of {@link CompositeGenerator}.
         */
        public CompositeGenerator build() {
            return new CompositeGenerator(this);
        }
    }
}
