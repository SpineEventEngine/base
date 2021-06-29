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

package io.spine.tools.mc.java.protoc.query;

import com.google.common.collect.ImmutableList;
import io.spine.tools.java.code.query.EntityQueryFactory;
import io.spine.tools.mc.java.protoc.ClassMember;
import io.spine.tools.mc.java.protoc.CodeGenerator;
import io.spine.tools.mc.java.protoc.CompilerOutput;
import io.spine.tools.mc.java.protoc.EntityMatcher;
import io.spine.tools.mc.java.protoc.NoOpGenerator;
import io.spine.tools.protoc.ForEntities;
import io.spine.tools.protoc.Method;
import io.spine.tools.protoc.NestedClass;
import io.spine.tools.protoc.SpineProtocConfig;
import io.spine.type.MessageType;
import io.spine.type.Type;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * Generates the {@code Query} and a {@code QueryBuilder} to allow creating typed queries for
 * the state of the {@code Entity}.
 *
 * <p>Builds a DSL specific to the declared entity columns.
 *
 * <p>Example.
 *
 * <p>Consider a Proto definition:
 *
 * <pre>
 *   message Customer {
 *       option (entity).kind = PROJECTION;
 *
 *       CustomerId id = 1;
 *
 *       string name = 2 [(required) = true];
 *
 *       EmailAddress email = 3;
 *
 *       Address address = 4;
 *
 *       CustomerType type = 5 [(required) = true, (column) = true];
 *
 *       int32 discount_percent = 6 [(min).value = "0", (column) = true];
 *   }
 * </pre>
 *
 * <p>This generator produces a specific builder for {@code Query}:
 * <pre>
 *     Customer.query()
 *             .id().in(westCustomerIds())
 *             .type().is("permanent")    // `type()` is a `...Criterion`.
 *             .discountPercent().isGreaterThan(10)
 *             .sortAscendingBy(Column.name())
 *             .withMask(Field.name(), Field.address())  // `Customer.Field` type is generated.
 *             .limit(1)
 *             .build()     // `Customer.Query`
 * </pre>
 */
public class EntityQueryGen extends CodeGenerator {

    private final EntityQueryFactory factory = new EntityQueryFactory();

    private final Predicate<MessageType> matcher;

    private EntityQueryGen(ForEntities forEntities) {
        super();
        this.matcher = new EntityMatcher(forEntities);
    }

    /**
     * Creates a new instance based on the passed Protoc config.
     */
    public static CodeGenerator instance(SpineProtocConfig config) {
        checkNotNull(config);
        ForEntities entities = config.getEntities();
        boolean enabled = entities.getGenerateQueries();
        return enabled
               ? new EntityQueryGen(entities)
               : NoOpGenerator.instance();
    }

    @Override
    protected Collection<CompilerOutput> generate(Type<?, ?> type) {
        checkNotNull(type);
        if (isEntityState(type)) {
            return generateFor((MessageType) type);
        }
        return ImmutableList.of();
    }

    private ImmutableList<CompilerOutput> generateFor(MessageType type) {
        try {
            ImmutableList.Builder<CompilerOutput> builder = ImmutableList.builder();
            addClasses(type, builder);
            addMethods(type, builder);
            return builder.build();
        } catch (@SuppressWarnings("OverlyBroadCatchBlock")   /* Every exception is rethrown */
                Exception e) {                                /*  with more diagnostic data. */
            throw newIllegalStateException(e, "Error generating `EntityQuery`" +
                    " for the type `%s`.", type.name());
        }
    }

    private void addClasses(MessageType type, ImmutableList.Builder<CompilerOutput> builder) {
        List<NestedClass> classes = factory.generateClassesFor(type);
        for (NestedClass cls : classes) {
            builder.add(ClassMember.nestedClass(cls, type));
        }
    }

    private void addMethods(MessageType type, ImmutableList.Builder<CompilerOutput> builder) {
        List<Method> methods = factory.generateMethodsFor(type);
        for (Method method : methods) {
            builder.add(ClassMember.method(method, type));
        }
    }

    private boolean isEntityState(Type<?, ?> type) {
        return type instanceof MessageType && matcher.test((MessageType) type);
    }
}
