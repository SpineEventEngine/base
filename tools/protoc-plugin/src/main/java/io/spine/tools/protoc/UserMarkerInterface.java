/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.tools.protoc;

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;
import com.squareup.javapoet.JavaFile;
import io.spine.code.java.SourceFile;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A user-defined marker interface.
 *
 * <p>This interface is declared with an {@link io.spine.option.OptionsProto#is (is)} or
 * an {@link io.spine.option.OptionsProto#everyIs (every_is)} option. See the option doc for
 * details.
 */
final class UserMarkerInterface extends AbstractCompilerOutput implements MarkerInterface {

    private final String interfaceFqn;

    private UserMarkerInterface(File file, String interfaceFqn) {
        super(file);
        this.interfaceFqn = interfaceFqn;
    }

    /**
     * Creates a {@code UserMarkerInterface} from the given spec.
     *
     * @param spec
     *         the interface spec to create an interface from
     * @return new instance of {@code UserMarkerInterface}
     */
    static UserMarkerInterface from(MarkerInterfaceSpec spec) {
        checkNotNull(spec);
        JavaFile javaCode = spec.toJavaCode();
        SourceFile file = spec.toSourceFile();
        File interfaceFile = File
                .newBuilder()
                .setName(file.toString())
                .setContent(javaCode.toString())
                .build();
        String fqn = spec.getFqn();
        return new UserMarkerInterface(interfaceFile, fqn);
    }

    @Override
    public String name() {
        return interfaceFqn;
    }

    /**
     * Generic params are currently not supported for user-defined marker interfaces.
     */
    @Override
    public MarkerInterfaceParameters parameters() {
        return MarkerInterfaceParameters.empty();
    }
}
