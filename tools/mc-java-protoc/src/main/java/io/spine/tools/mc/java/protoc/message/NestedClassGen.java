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

import com.google.common.collect.ImmutableList;
import io.spine.tools.java.protoc.AddNestedClasses;
import io.spine.tools.java.protoc.Classpath;
import io.spine.tools.java.protoc.CodeGenerationTask;
import io.spine.tools.java.protoc.CodeGenerationTasks;
import io.spine.tools.java.protoc.CodeGenerator;
import io.spine.tools.java.protoc.CompilerOutput;
import io.spine.tools.java.protoc.ConfigByPattern;
import io.spine.tools.mc.java.protoc.ExternalClassLoader;
import io.spine.tools.java.code.NestedClassFactory;
import io.spine.tools.java.protoc.SpineProtocConfig;
import io.spine.tools.java.protoc.InsertionPoint;
import io.spine.type.MessageType;
import io.spine.type.Type;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The {@link CodeGenerator} implementation which inserts additional nested classes into
 * the message.
 *
 * <p>The generator produces {@link CompilerOutput compiler output} that fits into the message's
 * {@link InsertionPoint#class_scope class scope} insertion point.
 */
public final class NestedClassGen extends CodeGenerator {

    private final CodeGenerationTasks tasks;

    private NestedClassGen(ImmutableList<CodeGenerationTask> tasks) {
        super();
        this.tasks = new CodeGenerationTasks(tasks);
    }

    /**
     * Creates a new instance based on the passed Protoc config.
     */
    public static NestedClassGen instance(SpineProtocConfig spineProtocConfig) {
        checkNotNull(spineProtocConfig);
        Classpath classpath = spineProtocConfig.getClasspath();
        ExternalClassLoader<NestedClassFactory> classLoader =
                new ExternalClassLoader<>(classpath, NestedClassFactory.class);
        AddNestedClasses config = spineProtocConfig.getAddNestedClasses();
        ImmutableList.Builder<CodeGenerationTask> tasks = ImmutableList.builder();
        for (ConfigByPattern byPattern : config.getFactoryByPatternList()) {
            tasks.add(new GenerateNestedClasses(classLoader, byPattern));
        }
        return new NestedClassGen(tasks.build());
    }

    @Override
    protected Collection<CompilerOutput> generate(Type<?, ?> type) {
        if (!(type instanceof MessageType)) {
            return ImmutableList.of();
        }
        MessageType messageType = (MessageType) type;
        ImmutableList<CompilerOutput> result = tasks.generateFor(messageType);
        return result;
    }
}
