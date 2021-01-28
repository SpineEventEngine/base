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

package io.spine.tools.protoc.message;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Message;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;
import io.spine.tools.protoc.AddInterfaces;
import io.spine.tools.protoc.CodeGenerationTask;
import io.spine.tools.protoc.CodeGenerationTasks;
import io.spine.tools.protoc.CodeGenerator;
import io.spine.tools.protoc.CompilerOutput;
import io.spine.tools.protoc.ConfigByPattern;
import io.spine.tools.protoc.EntityStateConfig;
import io.spine.tools.protoc.SpineProtocConfig;
import io.spine.tools.protoc.UuidConfig;
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
public final class InterfaceGenerator extends CodeGenerator {

    private final CodeGenerationTasks codeGenerationTasks;

    private InterfaceGenerator(ImmutableList<CodeGenerationTask> tasks) {
        super();
        this.codeGenerationTasks = new CodeGenerationTasks(tasks);
    }

    /**
     * Retrieves the single instance of the {@code InterfaceGenerator}.
     */
    public static CodeGenerator instance(SpineProtocConfig spineProtocConfig) {
        checkNotNull(spineProtocConfig);
        AddInterfaces config = spineProtocConfig.getAddInterfaces();
        ImmutableList.Builder<CodeGenerationTask> tasks = ImmutableList.builder();
        UuidConfig uuidInterface = config.getUuidInterface();
        if (generate(uuidInterface)) {
            tasks.add(new GenerateUuidInterfaces(uuidInterface));
        }
        for (ConfigByPattern byPattern : config.getInterfaceByPatternList()) {
            tasks.add(new GenerateInterfaces(byPattern));
        }
        EntityStateConfig entityStateInterface = config.getEntityStateInterface();
        if (generate(entityStateInterface)) {
            tasks.add(new GenerateEntityStateInterfaces(entityStateInterface));
        }
        return new InterfaceGenerator(tasks.build());
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
     * Otherwise, no compiler response is generated for this message type.
     */
    @Override
    protected Collection<CompilerOutput> generate(Type<?, ?> type) {
        return type instanceof MessageType
               ? processMessageType((MessageType) type)
               : ImmutableList.of();
    }

    private ImmutableList<CompilerOutput> processMessageType(MessageType type) {
        ImmutableList<CompilerOutput> matched = codeGenerationTasks.generateFor(type);
        Collection<CompilerOutput> fromMsgOption = MixInSpec.scanMsgOption(type);
        Collection<CompilerOutput> fromFileOption = MixInSpec.scanFileOption(type);
        return ImmutableList.<CompilerOutput>builder()
                            .addAll(matched)
                            .addAll(fromMsgOption)
                            .addAll(fromFileOption)
                            .build();
    }
}
