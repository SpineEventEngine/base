/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.tools.protoc.insert;

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;
import com.squareup.javapoet.JavaFile;
import io.spine.code.java.SourceFile;
import io.spine.tools.protoc.AbstractCompilerOutput;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A user-defined message interface.
 *
 * <p>This interface is declared with an {@link io.spine.option.OptionsProto#is (is)} or
 * an {@link io.spine.option.OptionsProto#everyIs (every_is)} option. See the option doc for
 * details.
 */
final class CustomMessageInterface extends AbstractCompilerOutput implements MessageInterface {

    private final String interfaceFqn;

    private CustomMessageInterface(File file, String interfaceFqn) {
        super(file);
        this.interfaceFqn = interfaceFqn;
    }

    /**
     * Creates a {@code CustomMessageInterface} from the given spec.
     *
     * @param spec
     *         the interface spec to create an interface from
     * @return new instance of {@code CustomMessageInterface}
     */
    static CustomMessageInterface from(MessageInterfaceSpec spec) {
        checkNotNull(spec);
        JavaFile javaCode = spec.toJavaCode();
        SourceFile file = spec.toSourceFile();
        File interfaceFile = File
                .newBuilder()
                .setName(file.toString())
                .setContent(javaCode.toString())
                .build();
        String fqn = spec.getFqn();
        return new CustomMessageInterface(interfaceFile, fqn);
    }

    @Override
    public String name() {
        return interfaceFqn;
    }

    /**
     * Generic params are currently not supported for user-defined message interfaces.
     */
    @Override
    public MessageInterfaceParameters parameters() {
        return MessageInterfaceParameters.empty();
    }
}
