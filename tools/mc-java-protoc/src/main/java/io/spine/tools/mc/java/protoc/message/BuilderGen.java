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

package io.spine.tools.mc.java.protoc.message;

import com.google.common.collect.ImmutableSet;
import io.spine.tools.java.protoc.CodeGenerator;
import io.spine.tools.java.protoc.CompilerOutput;
import io.spine.tools.java.protoc.NoOpGenerator;
import io.spine.tools.java.protoc.SpineProtocConfig;
import io.spine.type.MessageType;
import io.spine.type.Type;

import static io.spine.tools.mc.java.protoc.message.BuilderImplements.implementValidatingBuilder;

/**
 * A code generator which makes the generated message builders implement
 * {@link io.spine.validate.ValidatingBuilder ValidatingBuilder}.
 */
public final class BuilderGen extends CodeGenerator {

    /**
     * Prevents direct instantiation.
     */
    private BuilderGen() {
        super();
    }

    /**
     * Creates a new instance of the generator.
     */
    public static CodeGenerator instance(SpineProtocConfig config) {
        return config.getSkipValidatingBuilders()
               ? NoOpGenerator.instance()
               : new BuilderGen();
    }

    @Override
    protected ImmutableSet<CompilerOutput> generate(Type<?, ?> type) {
        if (type instanceof MessageType) {
            CompilerOutput insertionPoint = implementValidatingBuilder((MessageType) type);
            return ImmutableSet.of(insertionPoint);
        } else {
            return ImmutableSet.of();
        }
    }
}
