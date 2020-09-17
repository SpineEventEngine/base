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

package io.spine.code.gen.java;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import io.spine.code.gen.java.query.EntityQueryBuilderSpec;
import io.spine.code.gen.java.query.EntityQuerySpec;
import io.spine.tools.protoc.method.GeneratedMethod;
import io.spine.tools.protoc.method.MethodFactory;
import io.spine.tools.protoc.nested.GeneratedNestedClass;
import io.spine.tools.protoc.nested.NestedClassFactory;
import io.spine.type.MessageType;

import java.util.List;

/**
 * Generates an entity-specific {@code Query} and {@code QueryBuilder} classes.
 *
 * <p>Additionally, generates {@code query()} method to instantiate the {@code QueryBuilder}.
 */
@Immutable
public final class EntityQueryFactory implements NestedClassFactory, MethodFactory {

    @Override
    public List<GeneratedNestedClass> generateClassesFor(MessageType messageType) {
        GeneratedNestedClass generatedQueryType =
                asGeneratedClass(new EntityQuerySpec(messageType));
        GeneratedNestedClass generatedQueryBuilderType =
                asGeneratedClass(new EntityQueryBuilderSpec(messageType));

        return ImmutableList.of(generatedQueryType, generatedQueryBuilderType);
    }

    private static GeneratedNestedClass asGeneratedClass(GeneratedTypeSpec spec) {
        String rawOutput = spec.typeSpec()
                               .toString();
        GeneratedNestedClass generatedQueryType = new GeneratedNestedClass(rawOutput);
        return generatedQueryType;
    }

    @Override
    public List<GeneratedMethod> generateMethodsFor(MessageType messageType) {
        EntityQuerySpec spec = new EntityQuerySpec(messageType);
        GeneratedMethod method = new GeneratedMethod(spec.methodSpec()
                                                         .toString());
        return ImmutableList.of(method);
    }
}
