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

package io.spine.tools.protoc.query;

import com.google.common.collect.ImmutableList;
import io.spine.code.gen.java.EntityQueryFactory;
import io.spine.tools.protoc.ClassMember;
import io.spine.tools.protoc.CodeGenerator;
import io.spine.tools.protoc.CompilerOutput;
import io.spine.tools.protoc.SpineProtocConfig;
import io.spine.tools.protoc.method.GeneratedMethod;
import io.spine.tools.protoc.nested.GeneratedNestedClass;
import io.spine.type.MessageType;
import io.spine.type.Type;

import java.util.Collection;
import java.util.List;

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
 *     Customer.newQuery()
 *             .id().in(westCustomerIds())
 *             .type().equalTo("permanent")    // `type()` is a `...Criterion`.
 *             .discountPercent().isGreaterThan(10)
 *             .orderBy(Column.name()).ascending()
 *             .withMask(Column.name(), Column.address(), Column.email())
 *             .limit(1)
 *             .build()     // `Customer.Query`
 * </pre>
 */
public class EntityQueryGenerator extends CodeGenerator {

    private final EntityQueryFactory factory = new EntityQueryFactory();
    private final boolean enabled;

    private EntityQueryGenerator(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Creates a new instance based on the passed Protoc config.
     */
    public static EntityQueryGenerator instance(SpineProtocConfig config) {
        checkNotNull(config);
        boolean enabled = config.getAddEntityQueries()
                                .getGenerate();
        return new EntityQueryGenerator(enabled);
    }

    @Override
    protected Collection<CompilerOutput> generate(Type<?, ?> type) {
        checkNotNull(type);
        if (enabled && isEntityState(type)) {
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
        List<GeneratedNestedClass> classes = factory.generateClassesFor(type);
        for (GeneratedNestedClass cls : classes) {
            builder.add(ClassMember.nestedClass(cls, type));
        }
    }

    private void addMethods(MessageType type, ImmutableList.Builder<CompilerOutput> builder) {
        List<GeneratedMethod> methods = factory.generateMethodsFor(type);
        for (GeneratedMethod method : methods) {
            builder.add(ClassMember.method(method, type));
        }
    }

    private static boolean isEntityState(Type<?, ?> type) {
        return type instanceof MessageType
                && ((MessageType) type).isEntityState();
    }
}
