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

package io.spine.tools.protoc.messageinterface;

import com.google.common.collect.ImmutableList;
import io.spine.code.java.ClassName;
import io.spine.tools.protoc.CompilerOutput;
import io.spine.tools.protoc.ImplementInterface;
import io.spine.tools.protoc.InterfacesGeneration;
import io.spine.tools.protoc.TypeScanner;
import io.spine.tools.protoc.UuidImplementInterface;
import io.spine.type.MessageType;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static io.spine.tools.protoc.messageinterface.MessageImplements.implementInterface;
import static io.spine.validate.Validate.isDefault;

/**
 * Scans the given type for a match upon patterns defined in {@link InterfacesGeneration}.
 */
final class MessageInterfaceScanner extends TypeScanner<ImplementInterface> {

    private final InterfacesGeneration config;

    MessageInterfaceScanner(InterfacesGeneration config) {
        super();
        this.config = config;
    }

    @Override
    protected ImmutableList<CompilerOutput> uuidMessage(MessageType type) {
        UuidImplementInterface uuidInterface = config.getUuidInterface();
        if (isDefault(uuidInterface)) {
            return ImmutableList.of();
        }
        CompilerOutput result = uuidInterface(type, uuidInterface);
        return ImmutableList.of(result);
    }

    @Override
    protected List<ImplementInterface> filePatterns() {
        return config.getImplementInterfaceList();
    }

    @Override
    protected MatchesPattern matchesPattern(MessageType type) {
        return new MatchesPattern(type, ImplementInterface::getPattern);
    }

    @Override
    protected IsNotBlank isNotBlank() {
        return new IsNotBlank(ImplementInterface::getInterfaceName);
    }

    @Override
    protected Function<ImplementInterface, ImmutableList<CompilerOutput>>
    filePatternMapper(MessageType type) {
        return new GenerateInterfaces(type);
    }

    @Override
    protected Predicate<ImplementInterface> customFilter(MessageType type) {
        return configuration -> type.isTopLevel();
    }

    private static CompilerOutput uuidInterface(MessageType type,
                                                UuidImplementInterface uuidInterface) {
        ClassName interfaceName = ClassName.of(uuidInterface.getInterfaceName());
        MessageInterfaceParameters parameters =
                MessageInterfaceParameters.of(new IdentityParameter());
        MessageInterface messageInterface = new PredefinedInterface(interfaceName, parameters);
        CompilerOutput insertionPoint = implementInterface(type, messageInterface);
        return insertionPoint;
    }

    private static class GenerateInterfaces
            implements Function<ImplementInterface, ImmutableList<CompilerOutput>> {

        private final MessageType type;

        private GenerateInterfaces(MessageType type) {
            this.type = type;
        }

        @Override
        public ImmutableList<CompilerOutput> apply(ImplementInterface implementInterface) {
            MessageInterface messageInterface = new PredefinedInterface(
                    ClassName.of(implementInterface.getInterfaceName()),
                    MessageInterfaceParameters.empty()
            );
            MessageImplements result = implementInterface(type, messageInterface);
            return ImmutableList.of(result);
        }
    }
}
