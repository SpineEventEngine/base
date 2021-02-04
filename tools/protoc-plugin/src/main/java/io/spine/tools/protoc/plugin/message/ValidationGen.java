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
import com.google.protobuf.compiler.PluginProtos;
import io.spine.code.java.ClassName;
import io.spine.tools.protoc.plugin.CodeGenerator;
import io.spine.tools.protoc.plugin.CompilerOutput;
import io.spine.tools.protoc.plugin.InsertionPoint;
import io.spine.tools.protoc.plugin.NoOpGenerator;
import io.spine.tools.protoc.plugin.ProtocPluginFiles;
import io.spine.tools.protoc.SpineProtocConfig;
import io.spine.tools.validate.ValidateSpecs;
import io.spine.type.MessageType;
import io.spine.type.Type;
import io.spine.validate.MessageWithConstraints;

import static io.spine.tools.protoc.plugin.InsertionPoint.builder_scope;
import static io.spine.tools.protoc.plugin.InsertionPoint.class_scope;
import static io.spine.tools.protoc.plugin.message.Implement.interfaceFor;
import static io.spine.tools.protoc.plugin.message.InterfaceParameters.empty;

/**
 * Generates code which validates message fields upon the constraints, as well as the API which
 * exposes validation to the user of the message class.
 */
public final class ValidationGen extends CodeGenerator {

    private static final Interface MESSAGE_WITH_CONSTRAINTS =
            new ExistingInterface(ClassName.of(MessageWithConstraints.class));
    /**
     * Prevents direct instantiation.
     */
    private ValidationGen() {
        super();
    }

    public static CodeGenerator instance(SpineProtocConfig config) {
        return config.getSkipValidatingBuilders() || !config.getGenerateValidation()
               ? NoOpGenerator.instance()
               : new ValidationGen();
    }

    @Override
    protected ImmutableSet<CompilerOutput> generate(Type<?, ?> type) {
        return type instanceof MessageType
               ? generateValidationFor((MessageType) type)
               : ImmutableSet.of();
    }

    private static ImmutableSet<CompilerOutput> generateValidationFor(MessageType type) {
        ValidateSpecs factory = new ValidateSpecs(type);
        CompilerOutput builderInsertionPoint =
                insertCode(type, builder_scope, factory.vBuildMethod().toString());
        CompilerOutput validateMethod =
                insertCode(type, class_scope, factory.validateMethod().toString());
        CompilerOutput validatorClass =
                insertCode(type, class_scope, factory.validatorClass().toString());
        Implement iface = interfaceFor(type, MESSAGE_WITH_CONSTRAINTS);
        return ImmutableSet.of(
                builderInsertionPoint,
                validateMethod,
                validatorClass,
                iface
        );
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
