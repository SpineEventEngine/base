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

package io.spine.tools.protoc.plugin.column;

import com.google.common.collect.ImmutableList;
import io.spine.code.gen.java.ColumnFactory;
import io.spine.tools.protoc.plugin.ClassMember;
import io.spine.tools.protoc.plugin.CodeGenerator;
import io.spine.tools.protoc.plugin.CompilerOutput;
import io.spine.tools.protoc.SpineProtocConfig;
import io.spine.tools.protoc.plugin.nested.GeneratedNestedClass;
import io.spine.tools.protoc.plugin.InsertionPoint;
import io.spine.type.MessageType;
import io.spine.type.Type;

import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static io.spine.code.proto.ColumnOption.hasColumns;

/**
 * A code generator which adds the strongly-typed columns to a message type.
 *
 * <p>The generator produces {@link CompilerOutput compiler output} that fits into the message's
 * {@link InsertionPoint#class_scope class_scope} insertion point.
 *
 * <p>Generates output only for those message types that represent an
 * {@linkplain MessageType#isEntityState() entity state} with
 * {@linkplain io.spine.code.proto.ColumnOption columns}.
 */
public final class ColumnGenerator extends CodeGenerator {

    /**
     * The factory which is used for code generation.
     */
    private final ColumnFactory factory = new ColumnFactory();
    private final boolean generate;

    private ColumnGenerator(boolean generate) {
        super();
        this.generate = generate;
    }

    /**
     * Creates a new instance based on the passed Protoc config.
     */
    public static ColumnGenerator instance(SpineProtocConfig config) {
        checkNotNull(config);
        boolean generate = config.getAddEntityQueries()
                                 .getGenerate();
        return new ColumnGenerator(generate);
    }

    @Override
    protected Collection<CompilerOutput> generate(Type<?, ?> type) {
        checkNotNull(type);
        if (!generate || !isEntityStateWithColumns(type)) {
            return ImmutableList.of();
        }
        return generateFor((MessageType) type);
    }

    private ImmutableList<CompilerOutput> generateFor(MessageType type) {
        List<GeneratedNestedClass> generatedClasses = factory.generateClassesFor(type);
        ImmutableList<CompilerOutput> result =
                generatedClasses.stream()
                                .map(cls -> ClassMember.nestedClass(cls, type))
                                .collect(toImmutableList());
        return result;
    }

    private static boolean isEntityStateWithColumns(Type<?, ?> type) {
        if (!(type instanceof MessageType)) {
            return false;
        }
        MessageType messageType = (MessageType) type;
        return messageType.isEntityState() && hasColumns(messageType);
    }
}
