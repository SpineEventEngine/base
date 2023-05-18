/*
 * Copyright 2023, TeamDev. All rights reserved.
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

package io.spine.tools.protoc.plugin.iface;

import com.google.common.collect.ImmutableList;
import io.spine.code.java.ClassName;
import io.spine.tools.protoc.plugin.CodeGenerationTask;
import io.spine.tools.protoc.plugin.CompilerOutput;
import io.spine.tools.protoc.plugin.TypeParameters;
import io.spine.type.MessageType;

import static io.spine.tools.protoc.plugin.iface.MessageImplements.implementInterface;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * An abstract base for the interface code generation tasks.
 */
abstract class InterfaceGenerationTask implements CodeGenerationTask {

    private final String interfaceName;

    InterfaceGenerationTask(String interfaceName) {
        this.interfaceName = checkNotEmptyOrBlank(interfaceName);
    }

    /**
     * Creates {@link MessageInterface} parameters.
     */
    TypeParameters interfaceParameters() {
        return TypeParameters.empty();
    }

    /**
     * Performs the actual interface code generation.
     */
    ImmutableList<CompilerOutput> generateInterfacesFor(MessageType type) {
        ClassName interfaceName = ClassName.of(this.interfaceName);
        MessageInterface messageInterface =
                new PredefinedInterface(interfaceName, interfaceParameters());
        MessageImplements result = implementInterface(type, messageInterface);
        return ImmutableList.of(result);
    }
}
