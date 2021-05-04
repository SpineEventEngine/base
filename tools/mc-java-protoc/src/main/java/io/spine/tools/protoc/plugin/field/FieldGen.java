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

package io.spine.tools.protoc.plugin.field;

import com.google.common.collect.ImmutableList;
import io.spine.tools.java.code.field.FieldFactory;
import io.spine.tools.protoc.AddFields;
import io.spine.tools.protoc.plugin.CodeGenerationTask;
import io.spine.tools.protoc.plugin.CodeGenerationTasks;
import io.spine.tools.protoc.plugin.CodeGenerator;
import io.spine.tools.protoc.plugin.CompilerOutput;
import io.spine.tools.protoc.ConfigByPattern;
import io.spine.tools.protoc.ConfigByType;
import io.spine.tools.protoc.EntityStateConfig;
import io.spine.tools.protoc.Fields;
import io.spine.tools.protoc.SpineProtocConfig;
import io.spine.type.MessageType;
import io.spine.type.Type;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.protobuf.Messages.isNotDefault;

/**
 * A code generator which adds the strongly-typed fields to a message type.
 *
 * <p>The generator produces {@link CompilerOutput compiler output} that fits into the message's
 * {@link io.spine.tools.protoc.plugin.InsertionPoint#class_scope class_scope} insertion point.
 *
 * <p>Generates output based on the passed
 * {@linkplain Fields Protoc config}.
 */
public final class FieldGen extends CodeGenerator {

    /**
     * The factory used for code generation.
     */
    private static final FieldFactory factory = new FieldFactory();

    private final CodeGenerationTasks codeGenerationTasks;

    private FieldGen(ImmutableList<CodeGenerationTask> tasks) {
        super();
        this.codeGenerationTasks = new CodeGenerationTasks(tasks);
    }

    /**
     * Creates a new instance based on the passed Protoc config.
     */
    @SuppressWarnings("MethodWithMultipleLoops") // Required to configure code generation tasks.
    public static FieldGen instance(SpineProtocConfig spineProtocConfig) {
        checkNotNull(spineProtocConfig);
        AddFields config = spineProtocConfig.getAddFields();

        ImmutableList.Builder<CodeGenerationTask> tasks = ImmutableList.builder();
        EntityStateConfig entityStateConfig = config.getEntityStateConfig();
        if (isNotDefault(entityStateConfig)) {
            tasks.add(new GenerateEntityStateFields(entityStateConfig, factory));
        }
        for (ConfigByPattern byPattern : config.getConfigByPatternList()) {
            tasks.add(new GenerateFieldsByPattern(byPattern, factory));
        }
        for (ConfigByType byType : config.getConfigByTypeList()) {
            tasks.add(new GenerateFieldsByType(byType, factory));
        }
        return new FieldGen(tasks.build());
    }

    @Override
    public Collection<CompilerOutput> generate(Type<?, ?> type) {
        if (!(type instanceof MessageType)) {
            return ImmutableList.of();
        }
        MessageType messageType = (MessageType) type;
        ImmutableList<CompilerOutput> result = codeGenerationTasks.generateFor(messageType);
        return result;
    }
}
