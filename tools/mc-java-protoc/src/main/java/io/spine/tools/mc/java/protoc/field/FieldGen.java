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

package io.spine.tools.mc.java.protoc.field;

import com.google.common.collect.ImmutableList;
import io.spine.tools.java.code.field.FieldFactory;
import io.spine.tools.mc.java.protoc.CodeGenerationTask;
import io.spine.tools.mc.java.protoc.CodeGenerationTasks;
import io.spine.tools.mc.java.protoc.CodeGenerator;
import io.spine.tools.mc.java.protoc.CompilerOutput;
import io.spine.tools.mc.java.protoc.InsertionPoint;
import io.spine.tools.protoc.ForEntities;
import io.spine.tools.protoc.ForMessages;
import io.spine.tools.protoc.ForSignals;
import io.spine.tools.protoc.GenerateFields;
import io.spine.tools.protoc.Pattern;
import io.spine.tools.protoc.SpineProtocConfig;
import io.spine.type.MessageType;
import io.spine.type.Type;

import java.util.Collection;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;

/**
 * A code generator which adds the strongly-typed fields to a message type.
 *
 * <p>The generator produces {@link CompilerOutput compiler output} that fits into the message's
 * {@link InsertionPoint#class_scope class_scope} insertion point.
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
    public static FieldGen instance(SpineProtocConfig spineProtocConfig) {
        checkNotNull(spineProtocConfig);
        ImmutableList.Builder<CodeGenerationTask> tasks = ImmutableList.builder();

        if (spineProtocConfig.hasEntities()) {
            ForEntities entities = spineProtocConfig.getEntities();
            GenerateFields fields = entities.getGenerateFields();
            if (fields.hasSuperclass()) {
                tasks.add(new GenerateEntityStateFields(entities, factory));
            }
        }
        if (spineProtocConfig.hasCommands()) {
            ForSignals signals = spineProtocConfig.getCommands();
            tasks.addAll(tasksFor(signals));
        }
        if (spineProtocConfig.hasEvents()) {
            ForSignals signals = spineProtocConfig.getEvents();
            tasks.addAll(tasksFor(signals));
        }
        if (spineProtocConfig.hasRejections()) {
            ForSignals signals = spineProtocConfig.getRejections();
            tasks.addAll(tasksFor(signals));
        }
        for (ForMessages group : spineProtocConfig.getMessagesList()) {
            taskFor(group).ifPresent(tasks::add);
        }
        return new FieldGen(tasks.build());
    }

    private static ImmutableList<GenerateFieldsByPattern> tasksFor(ForSignals forSignals) {
        GenerateFields generateFields = forSignals.getGenerateFields();
        if (!generateFields.hasSuperclass()) {
            return ImmutableList.of();
        }
        return forSignals.getPatternList()
                         .stream()
                         .map(pattern -> new GenerateFieldsByPattern(
                                 generateFields.getSuperclass(), pattern, factory
                         )).collect(toImmutableList());
    }

    private static Optional<GenerateFieldsByPattern> taskFor(ForMessages forMessages) {
        GenerateFields generateFields = forMessages.getGenerateFields();
        if (!generateFields.hasSuperclass()) {
            return Optional.empty();
        }
        Pattern pattern = forMessages.getPattern();
        GenerateFieldsByPattern task = new GenerateFieldsByPattern(
                generateFields.getSuperclass(), pattern, factory
        );
        return Optional.of(task);
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
