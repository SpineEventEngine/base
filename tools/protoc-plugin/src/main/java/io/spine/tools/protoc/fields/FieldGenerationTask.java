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

package io.spine.tools.protoc.fields;

import com.google.common.collect.ImmutableList;
import io.spine.code.gen.java.FieldFactory;
import io.spine.tools.protoc.CodeGenerationTask;
import io.spine.tools.protoc.CompilerOutput;
import io.spine.tools.protoc.ExternalClassLoader;
import io.spine.tools.protoc.NestedMember;
import io.spine.tools.protoc.nested.NestedClassFactory;
import io.spine.type.MessageType;

import static com.google.common.collect.ImmutableList.toImmutableList;

/**
 * An abstract base for field generation tasks.
 */
abstract class FieldGenerationTask implements CodeGenerationTask {

    /**
     * The factory used for code generation.
     */
    private final FieldFactory factory = new FieldFactory();

    FieldGenerationTask() {
    }

    /**
     * Performs the actual code generation using the supplied {@linkplain #factory}.
     */
    ImmutableList<CompilerOutput> generateFieldsFor(MessageType type) {
        return factory
                .createFor(type)
                .stream()
                .map(classBody -> NestedMember.from(classBody, type))
                .collect(toImmutableList());
    }

    private static NestedClassFactory
    factoryInstance(ExternalClassLoader<NestedClassFactory> classLoader, String factoryName) {
        return classLoader.newInstance(factoryName);
    }
}
