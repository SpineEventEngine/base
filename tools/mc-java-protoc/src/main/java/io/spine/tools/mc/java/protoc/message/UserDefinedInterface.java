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

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;
import com.squareup.javapoet.JavaFile;
import io.spine.option.IsOption;
import io.spine.tools.java.fs.SourceFile;
import io.spine.tools.java.protoc.AbstractCompilerOutput;
import io.spine.type.MessageType;
import org.checkerframework.checker.signature.qual.FullyQualifiedName;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.java.protoc.ProtocPluginFiles.prepareFile;
import static io.spine.tools.mc.java.protoc.message.InterfaceSpec.createFor;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A user-defined interface or a message type.
 *
 * <p>This interface is declared with an {@link io.spine.option.OptionsProto#is (is)} or
 * an {@link io.spine.option.OptionsProto#everyIs (every_is)} options. Please see the documentation
 * in the proto files declaring these options for details.
 */
final class UserDefinedInterface
        extends AbstractCompilerOutput
        implements Interface {

    private final String interfaceFqn;

    private UserDefinedInterface(File file, @FullyQualifiedName String interfaceFqn) {
        super(file);
        this.interfaceFqn = interfaceFqn;
    }

    /**
     * Creates a new compiler output for implementing an interfaces specified in
     * the passed option.
     *
     * @param type
     *         the type which is going to implement the custom interface
     * @param isOption
     *         the option which specifies the custom interface name
     */
    static UserDefinedInterface declaredFor(MessageType type, IsOption isOption) {
        checkNotNull(type);
        checkNotNull(isOption);
        checkNotEmptyOrBlank(isOption.getJavaType());
        InterfaceSpec spec = createFor(type, isOption);
        JavaFile javaCode = spec.toJavaCode();
        SourceFile file = spec.toSourceFile();
        File interfaceFile = prepareFile(file.toString())
                .setContent(javaCode.toString())
                .build();
        String fqn = spec.fullName();
        UserDefinedInterface result = new UserDefinedInterface(interfaceFile, fqn);
        return result;
    }

    @Override
    public String name() {
        return interfaceFqn;
    }

    /**
     * Generic params are currently not supported for user-defined message interfaces.
     *
     * @return {@link InterfaceParameters#empty()} always
     */
    @Override
    public InterfaceParameters parameters() {
        return InterfaceParameters.empty();
    }
}
