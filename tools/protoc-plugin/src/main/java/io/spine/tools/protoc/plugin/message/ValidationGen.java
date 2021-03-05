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

package io.spine.tools.protoc.plugin.message;

import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Message;
import com.google.protobuf.compiler.PluginProtos;
import io.spine.base.CommandMessage;
import io.spine.base.EventMessage;
import io.spine.base.RejectionMessage;
import io.spine.code.java.ClassName;
import io.spine.tools.protoc.SpineProtocConfig;
import io.spine.tools.protoc.plugin.CodeGenerator;
import io.spine.tools.protoc.plugin.CompilerOutput;
import io.spine.tools.protoc.plugin.InsertionPoint;
import io.spine.tools.protoc.plugin.NoOpGenerator;
import io.spine.tools.protoc.plugin.ProtocPluginFiles;
import io.spine.tools.validate.ValidateSpecs;
import io.spine.type.MessageType;
import io.spine.type.Type;
import io.spine.validate.MessageWithConstraints;

import static io.spine.tools.protoc.plugin.InsertionPoint.builder_scope;
import static io.spine.tools.protoc.plugin.InsertionPoint.class_scope;
import static io.spine.tools.protoc.plugin.message.Implement.interfaceFor;

/**
 * Generates code which validates message fields upon the constraints, as well as the API which
 * exposes validation to the user of the message class.
 */
public final class ValidationGen extends CodeGenerator {

    /** Prevents direct instantiation. */
    private ValidationGen() {
        super();
    }

    public static CodeGenerator instance(SpineProtocConfig config) {
        boolean skipBuilders = config.getSkipValidatingBuilders();
        boolean doNotGenerate = !config.getGenerateValidation();
        return skipBuilders || doNotGenerate
               ? NoOpGenerator.instance()
               : new ValidationGen();
    }

    @Override
    protected ImmutableSet<CompilerOutput> generate(Type<?, ?> type) {
        return type instanceof MessageType
               ? generateValidationFor((MessageType) type)
               : ImmutableSet.of();
    }

    /**
     * Generates the validation code for several insertion points of the given {@code Message} type.
     *
     * <p>The returned compiler output explicitly marks the given type with
     * the {@link MessageWithConstraints} interface and includes the code pieces to comply with
     * its contract, such as the implementation of {@code MessageWithConstraints} API and
     * the nested {@code Validator} type.
     *
     * <p>In case the processed type is a {@linkplain MessageType#isSignal() signal},
     * the {@link MessageWithConstraints} marker interface is <em>not</em> appended. This is so,
     * because the contract of signals (e.g. {@link EventMessage} contract) already implies
     * them being a constrained message.
     *
     * @param type
     *         the type to generate the validation code for
     * @return compiler output relevant for the passed type
     */
    private static ImmutableSet<CompilerOutput> generateValidationFor(MessageType type) {
        ValidateSpecs factory = new ValidateSpecs(type);
        CompilerOutput builderInsertionPoint =
                insertCode(type, builder_scope, factory.vBuildMethod().toString());
        CompilerOutput validateMethod =
                insertCode(type, class_scope, factory.validateMethod().toString());
        CompilerOutput validatorClass =
                insertCode(type, class_scope, factory.validatorClass().toString());
        ImmutableSet.Builder<CompilerOutput> builder = ImmutableSet.builder();
        builder.add(builderInsertionPoint, validateMethod, validatorClass);
        if(!type.isSignal()) {
            Implement iface = interfaceFor(type, implementingBaseInterfaceOf(type));
            builder.add(iface);
        }
        ImmutableSet<CompilerOutput> result = builder.build();
        return result;
    }

    private static ExistingInterface implementingBaseInterfaceOf(MessageType type) {
        Class<? extends Message> baseClass = toBaseInterface(type);
        ExistingInterface result = new ExistingInterface(ClassName.of(baseClass));
        return result;
    }

    private static Class<? extends Message> toBaseInterface(MessageType type) {
        if (type.isEvent()) {
            return EventMessage.class;
        }
        if (type.isCommand()) {
            return CommandMessage.class;
        }
        if (type.isRejection()) {
            return RejectionMessage.class;
        }
        return MessageWithConstraints.class;
    }

    private static CompilerOutput
    insertCode(Type<?, ?> type, InsertionPoint target, String javaCode) {
        PluginProtos.CodeGeneratorResponse.File file = ProtocPluginFiles
                .prepareFile(type)
                .setInsertionPoint(target.forType(type))
                .setContent(javaCode)
                .build();
        return CompilerOutput.wrapping(file);
    }
}
