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

package io.spine.tools.protoc.iface;

import com.google.common.collect.ImmutableList;
import io.spine.code.java.ClassName;
import io.spine.tools.protoc.CodeGenerationTask;
import io.spine.tools.protoc.CompilerOutput;
import io.spine.tools.protoc.FilePatternMatcher;
import io.spine.tools.protoc.ImplementInterface;
import io.spine.type.MessageType;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.protoc.iface.MessageImplements.implementInterface;

/**
 * Makes type implement interface supplied with the {@link ImplementInterface configuration}.
 */
final class GenerateInterface implements CodeGenerationTask {

    private final String interfaceName;
    private final FilePatternMatcher patternMatcher;

    GenerateInterface(ImplementInterface config) {
        checkNotNull(config);
        this.interfaceName = config.getInterfaceName();
        this.patternMatcher = new FilePatternMatcher(config.getPattern());
    }

    /**
     * Makes supplied type implement configured interface.
     *
     * <p>The type does not implement an interface if:
     *
     * <ul>
     *     <li>the interface name is empty;
     *     <li>the type is not {@link MessageType#isTopLevel() top level};
     *     <li>the type file name does not match supplied
     *     {@link io.spine.tools.protoc.FilePattern pattern}.
     * </ul>
     */
    @Override
    public ImmutableList<CompilerOutput> generateFor(MessageType type) {
        if (interfaceName.isEmpty() || !type.isTopLevel() || !patternMatcher.test(type)) {
            return ImmutableList.of();
        }
        MessageInterface messageInterface = new PredefinedInterface(
                ClassName.of(interfaceName),
                MessageInterfaceParameters.empty()
        );
        MessageImplements result = implementInterface(type, messageInterface);
        return ImmutableList.of(result);
    }
}
