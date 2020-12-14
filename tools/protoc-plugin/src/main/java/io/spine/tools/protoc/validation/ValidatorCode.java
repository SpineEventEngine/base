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

package io.spine.tools.protoc.validation;

import com.google.common.collect.ImmutableSet;
import io.spine.code.java.ClassName;
import io.spine.protobuf.MessageWithConstraints;
import io.spine.tools.protoc.CodeGenerator;
import io.spine.tools.protoc.CompilerOutput;
import io.spine.tools.protoc.InsertionPoint;
import io.spine.tools.protoc.NoOpGenerator;
import io.spine.tools.protoc.ProtocPluginFiles;
import io.spine.tools.protoc.SpineProtocConfig;
import io.spine.tools.protoc.iface.MessageImplements;
import io.spine.tools.protoc.iface.MessageInterface;
import io.spine.tools.protoc.iface.PredefinedInterface;
import io.spine.tools.validate.ValidateGenerator;
import io.spine.type.MessageType;
import io.spine.type.Type;

import java.util.Collection;

import static io.spine.tools.protoc.InsertionPoint.builder_scope;
import static io.spine.tools.protoc.InsertionPoint.class_scope;
import static io.spine.tools.protoc.TypeParameters.empty;
import static io.spine.tools.protoc.iface.MessageImplements.implementInterface;

/**
 * Generates code which validates message fields upon the constraints, as well as the API which
 * exposes validation to the user of the message class.
 */
public final class ValidatorCode extends CodeGenerator {

    private static final MessageInterface VALIDATABLE_MESSAGE =
            new PredefinedInterface(ClassName.of(MessageWithConstraints.class), empty());

    /**
     * Prevents direct instantiation.
     */
    private ValidatorCode() {
        super();
    }

    public static CodeGenerator instance(SpineProtocConfig config) {
        return config.getSkipValidatingBuilders() || !config.getGenerateValidation()
               ? NoOpGenerator.instance()
               : new ValidatorCode();
    }

    @Override
    protected Collection<CompilerOutput> generate(Type<?, ?> type) {
        return type instanceof MessageType
               ? compileValidation((MessageType) type)
               : ImmutableSet.of();
    }

    private static Collection<CompilerOutput> compileValidation(MessageType type) {
        ValidateGenerator factory = new ValidateGenerator(type);
        CompilerOutput builderInsertionPoint =
                insertCode(type, builder_scope, factory.generateVBuild().toString());
        CompilerOutput validateMethod =
                insertCode(type, class_scope, factory.generateValidate().toString());
        CompilerOutput validatorClass =
                insertCode(type, class_scope, factory.generateClass().toString());
        MessageImplements messageInterface = implementInterface(type, VALIDATABLE_MESSAGE);
        return ImmutableSet.of(
                builderInsertionPoint,
                validateMethod,
                validatorClass,
                messageInterface
        );
    }

    private static CompilerOutput
    insertCode(Type<?, ?> type, InsertionPoint target, String javaCode) {
        return CompilerOutput.wrapping(ProtocPluginFiles
                                               .prepareFile(type)
                                               .setInsertionPoint(target.forType(type))
                                               .setContent(javaCode)
                                               .build());
    }
}
