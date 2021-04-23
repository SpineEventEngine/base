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

package io.spine.tools.protoc.plugin.java.message;

import com.google.common.collect.ImmutableList;
import io.spine.tools.protoc.plugin.java.ClassMember;
import io.spine.tools.protoc.plugin.CodeGenerationTask;
import io.spine.tools.protoc.plugin.CompilerOutput;
import io.spine.tools.protoc.plugin.java.ExternalClassLoader;
import io.spine.tools.protoc.NestedClassFactory;
import io.spine.type.MessageType;
import org.checkerframework.checker.nullness.qual.NonNull;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * An abstract base for the nested classes generation tasks.
 */
abstract class NestedClassGenerationTask implements CodeGenerationTask {

    private final ExternalClassLoader<NestedClassFactory> classLoader;
    private final String factoryName;

    NestedClassGenerationTask(ExternalClassLoader<NestedClassFactory> classLoader,
                              String factoryName) {
        this.classLoader = checkNotNull(classLoader);
        this.factoryName = checkNotEmptyOrBlank(factoryName);
    }

    /**
     * Performs the actual code generation using the supplied {@linkplain #factoryName factory}.
     */
    ImmutableList<CompilerOutput> generateNestedClassesFor(@NonNull MessageType type) {
        NestedClassFactory factory = classLoader.newInstance(factoryName);
        return factory
                .generateClassesFor(type)
                .stream()
                .map(classBody -> ClassMember.nestedClass(classBody, type))
                .collect(toImmutableList());
    }
}
