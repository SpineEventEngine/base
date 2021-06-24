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
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Message;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;
import io.spine.tools.mc.java.protoc.CodeGenerationTask;
import io.spine.tools.mc.java.protoc.CodeGenerationTasks;
import io.spine.tools.mc.java.protoc.CodeGenerator;
import io.spine.tools.mc.java.protoc.CompilerOutput;
import io.spine.tools.protoc.EntityStateConfig;
import io.spine.tools.protoc.SpineProtocConfig;
import io.spine.type.MessageType;
import io.spine.type.Type;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.protobuf.Messages.isNotDefault;

/**
 * The {@link CodeGenerator} implementation generating the specific interfaces implemented by
 * some message types.
 *
 * <p>This generator processes interfaces obtained from {@code (is)} and {@code (every_is)} options
 * as well as interfaces defined for {@linkplain CodeGenerationTask file patterns}.
 *
 * <p>The generator produces two types of {@link File CodeGeneratorResponse.File} instances
 * representing:
 * <ul>
 *     <li>the interfaces derived from {@link com.google.protobuf.Message}
 *     <li>the insertion entries to the existing messages (see
 *         {@link File#getInsertionPoint() CodeGeneratorResponse.File.insertionPoint}).
 * </ul>
 */
public final class InterfaceGen extends CodeGenerator {

    private final CodeGenerationTasks tasks;

    private InterfaceGen(ImmutableList<CodeGenerationTask> tasks) {
        super();
        this.tasks = new CodeGenerationTasks(tasks);
    }

    /**
     * Retrieves the single instance of the {@code InterfaceGenerator}.
     */
    public static CodeGenerator instance(SpineProtocConfig spineProtocConfig) {
        checkNotNull(spineProtocConfig);
//        AddInterfaces config = spineProtocConfig.getAddInterfaces();
        ImmutableList.Builder<CodeGenerationTask> tasks = ImmutableList.builder();
//        UuidConfig uuidConfig = config.getUuidInterface();
//        if (generate(uuidConfig)) {
//            tasks.add(new ImplementUuidValue(uuidConfig));
//        }
//        for (ConfigByPattern byPattern : config.getInterfaceByPatternList()) {
//            tasks.add(new ImplementByPattern(byPattern));
//        }
//        EntityStateConfig esConfig = config.ger;
//        if (generate(esConfig)) {
//            tasks.add(new ImplementEntityState(esConfig));
//        }
        return new InterfaceGen(tasks.build());
    }

    /**
     * This is a DSL method for checking if a passed Model Compiler configuration setting
     * (such as {@link UuidConfig} or {@link EntityStateConfig}) is turned on.
     */
    private static boolean generate(Message config) {
        return isNotDefault(config);
    }

    /**
     * Makes a generated message class implement an interface according to specified proto options.
     *
     * <p>The method processes the passed message type as follows:
     * <ol>
     *     <li>If the message declaration matches any built-in interface contract,
     *         the message insertion point with the appropriate interface name is generated.
     *     <li>If the message has {@code (is)} option, the interface name is
     *         extracted from it and both the interface and the message insertion point are
     *         generated.
     *     <li>If the file where the message is declared has the {@code (every_is)} option,
     *         the interface name is extracted from this option and both the interface and
     *         the message insertion point are generated.
     * </ol>
     *
     * <p>If the passed type does not represent a message, no compiler response is generated.
     */
    @Override
    protected Collection<CompilerOutput> generate(Type<?, ?> type) {
        if (!(type instanceof MessageType)) {
            return ImmutableList.of();
        }
        return process((MessageType) type);
    }

    private ImmutableList<CompilerOutput> process(MessageType type) {
        ImmutableList<CompilerOutput> matched = tasks.generateFor(type);
        ImmutableList<CompilerOutput> mixed = MixInSpec.scanOptionsFor(type);
        ImmutableSet<CompilerOutput> deduplicated = ImmutableSet.<CompilerOutput>builder()
                .addAll(matched)
                .addAll(mixed)
                .build();
        return deduplicated.asList();
    }
}
