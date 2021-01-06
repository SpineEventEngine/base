/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.tools.protoc.plugin.method;

import com.google.common.collect.ImmutableList;
import io.spine.tools.protoc.AddMethods;
import io.spine.tools.protoc.Classpath;
import io.spine.tools.protoc.plugin.CodeGenerationTask;
import io.spine.tools.protoc.plugin.CodeGenerationTasks;
import io.spine.tools.protoc.plugin.CodeGenerator;
import io.spine.tools.protoc.plugin.CompilerOutput;
import io.spine.tools.protoc.ConfigByPattern;
import io.spine.tools.protoc.plugin.ExternalClassLoader;
import io.spine.tools.protoc.SpineProtocConfig;
import io.spine.tools.protoc.plugin.InsertionPoint;
import io.spine.type.MessageType;
import io.spine.type.Type;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.protobuf.Messages.isNotDefault;

/**
 * The {@link CodeGenerator} implementation generating additional message methods.
 *
 * <p>The generator produces {@link CompilerOutput compiler output} that fits into the message's
 * {@link InsertionPoint#class_scope class scope} insertion point.
 */
public final class MethodGenerator extends CodeGenerator {

    private final CodeGenerationTasks codeGenerationTasks;

    /** Prevents singleton class instantiation. */
    private MethodGenerator(ImmutableList<CodeGenerationTask> codeGenerationTasks) {
        super();
        this.codeGenerationTasks = new CodeGenerationTasks(codeGenerationTasks);
    }

    /**
     * Retrieves the single instance of the {@code MethodGenerator}.
     */
    public static MethodGenerator instance(SpineProtocConfig spineProtocConfig) {
        checkNotNull(spineProtocConfig);
        Classpath classpath = spineProtocConfig.getClasspath();
        ExternalClassLoader<MethodFactory> classLoader =
                new ExternalClassLoader<>(classpath, MethodFactory.class);
        AddMethods config = spineProtocConfig.getAddMethods();
        ImmutableList.Builder<CodeGenerationTask> tasks = ImmutableList.builder();
        if (isNotDefault(config.getUuidFactory())) {
            tasks.add(new GenerateUuidMethods(classLoader, config.getUuidFactory()));
        }
        for (ConfigByPattern byPattern : config.getFactoryByPatternList()) {
            tasks.add(new GenerateMethods(classLoader, byPattern));
        }
        return new MethodGenerator(tasks.build());
    }

    @Override
    protected Collection<CompilerOutput> generate(Type<?, ?> type) {
        if (!(type instanceof MessageType)) {
            return ImmutableList.of();
        }
        MessageType messageType = (MessageType) type;
        ImmutableList<CompilerOutput> result = codeGenerationTasks.generateFor(messageType);
        return result;
    }
}
