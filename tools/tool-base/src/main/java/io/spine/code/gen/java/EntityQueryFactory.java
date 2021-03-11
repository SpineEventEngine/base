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

package io.spine.code.gen.java;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import io.spine.code.gen.java.query.EntityQueryBuilderSpec;
import io.spine.code.gen.java.query.EntityQuerySpec;
import io.spine.tools.protoc.Method;
import io.spine.tools.protoc.MethodFactory;
import io.spine.tools.protoc.NestedClass;
import io.spine.tools.protoc.NestedClassFactory;
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
    public List<NestedClass> generateClassesFor(MessageType type) {
        NestedClass queryType = new NestedClass(new EntityQuerySpec(type));
        NestedClass queryBuilderType = new NestedClass(new EntityQueryBuilderSpec(type));
        return ImmutableList.of(queryType, queryBuilderType);
    }

    @Override
    public List<Method> generateMethodsFor(MessageType type) {
        EntityQuerySpec spec = new EntityQuerySpec(type);
        Method method = new Method(spec.methodSpec());
        return ImmutableList.of(method);
    }
}
