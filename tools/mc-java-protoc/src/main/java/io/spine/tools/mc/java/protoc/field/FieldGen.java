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
import io.spine.tools.protoc.Entities;
import io.spine.tools.protoc.GenerateFields;
import io.spine.tools.protoc.Messages;
import io.spine.tools.protoc.Pattern;
import io.spine.tools.protoc.Signals;
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

    private FieldGen(Builder builder) {
        super();
        this.codeGenerationTasks = new CodeGenerationTasks(builder.tasks());
    }

    /**
     * Creates a new instance based on the passed Protoc config.
     */
    public static FieldGen instance(SpineProtocConfig spineProtocConfig) {
        checkNotNull(spineProtocConfig);
        Builder builder = new Builder(spineProtocConfig);
        builder.addFromAll();
        return builder.build();
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

    /**
     * A builder for the {@code FieldGen} instances.
     */
    private static final class Builder {

        private final SpineProtocConfig config;
        private final ImmutableList.Builder<CodeGenerationTask> tasks = ImmutableList.builder();

        /**
         * Prevents direct instantiation.
         */
        private Builder(SpineProtocConfig config) {
            this.config = config;
        }

        private ImmutableList<CodeGenerationTask> tasks() {
            return tasks.build();
        }

        /**
         * Creates a new instance of {@code FieldGen}.
         *
         * @return new instance of {@code FieldGen}
         */
        private FieldGen build() {
            return new FieldGen(this);
        }

        private void addFromAll() {
            addFromCommands();
            addFromEvents();
            addFromRejections();
            addFromEntities();
            addFromMessages();
        }

        private void addFromMessages() {
            for (Messages group : config.getMessagesList()) {
                taskFor(group).ifPresent(tasks::add);
            }
        }

        private void addFromRejections() {
            if (config.hasRejections()) {
                Signals signals = config.getRejections();
                tasks.addAll(tasksFor(signals));
            }
        }

        private void addFromEvents() {
            if (config.hasEvents()) {
                Signals signals = config.getEvents();
                tasks.addAll(tasksFor(signals));
            }
        }

        private void addFromCommands() {
            if (config.hasCommands()) {
                Signals signals = config.getCommands();
                tasks.addAll(tasksFor(signals));
            }
        }

        private void addFromEntities() {
            if (config.hasEntities()) {
                Entities entities = config.getEntities();
                GenerateFields fields = entities.getGenerateFields();
                if (fields.hasSuperclass()) {
                    tasks.add(new GenerateEntityStateFields(entities, factory));
                }
            }
        }

        private static ImmutableList<GenerateFieldsByPattern> tasksFor(Signals signals) {
            GenerateFields generateFields = signals.getGenerateFields();
            if (!generateFields.hasSuperclass()) {
                return ImmutableList.of();
            }
            return signals.getPatternList()
                          .stream()
                          .map(filePattern -> new GenerateFieldsByPattern(
                                  generateFields, filePattern, factory
                          )).collect(toImmutableList());
        }

        private static Optional<GenerateFieldsByPattern> taskFor(Messages messages) {
            GenerateFields generateFields = messages.getGenerateFields();
            if (!generateFields.hasSuperclass()) {
                return Optional.empty();
            }
            Pattern pattern = messages.getPattern();
            GenerateFieldsByPattern task = new GenerateFieldsByPattern(
                    generateFields, pattern, factory
            );
            return Optional.of(task);
        }
    }
}
