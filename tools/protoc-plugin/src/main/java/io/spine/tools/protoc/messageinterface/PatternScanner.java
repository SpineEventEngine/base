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

import io.spine.code.java.ClassName;
import io.spine.code.proto.MessageType;
import io.spine.tools.protoc.CompilerOutput;
import io.spine.tools.protoc.EnrichmentInterface;
import io.spine.tools.protoc.GeneratedInterface;
import io.spine.tools.protoc.GeneratedInterfacesConfig;
import io.spine.tools.protoc.UuidInterface;

import java.util.List;
import java.util.Optional;

import static io.spine.base.MessageClassifiers.uuidContainer;
import static io.spine.tools.protoc.messageinterface.MessageImplements.implementInterface;
import static io.spine.validate.Validate.isNotDefault;

/**
 * Scans the given type for a match upon patterns defined in {@link GeneratedInterfacesConfig}.
 */
final class PatternScanner {

    private final GeneratedInterfacesConfig patterns;

    PatternScanner(GeneratedInterfacesConfig patterns) {
        this.patterns = patterns;
    }

    /**
     * Finds an interface to be generated for the given type.
     */
    Optional<CompilerOutput> scan(MessageType type) {
        if (isUuidMessage(type)) {
            return uuidInterface(type);
        }
        if (type.isEnrichment()) {
            return enrichmentInterface(type);
        }
        if (type.isTopLevel()) {
            return postfixBasedInterface(type);
        }
        return Optional.empty();
    }

    private boolean isUuidMessage(MessageType type) {
        if (!patterns.hasUuidInterface()) {
            return false;
        }
        UuidInterface uuidInterface = patterns.getUuidInterface();
        return isNotDefault(uuidInterface) && uuidContainer().test(type);
    }

    private Optional<CompilerOutput> uuidInterface(MessageType type) {
        UuidInterface uuidInterface = patterns.getUuidInterface();
        return Optional.of(uuidInterface(type, uuidInterface));
    }

    private static CompilerOutput uuidInterface(MessageType type, UuidInterface uuidInterface) {
        ClassName interfaceName = ClassName.of(uuidInterface.getInterfaceName());
        MessageInterfaceParameters parameters =
                MessageInterfaceParameters.of(new IdentityParameter());
        MessageInterface messageInterface = new PredefinedInterface(interfaceName, parameters);
        CompilerOutput insertionPoint = implementInterface(type, messageInterface);
        return insertionPoint;
    }

    private Optional<CompilerOutput> enrichmentInterface(MessageType type) {
        EnrichmentInterface enrichmentInterface = patterns.getEnrichmentInterface();
        return Optional.of(enrichmentInterface(type, enrichmentInterface));
    }

    private static CompilerOutput
    enrichmentInterface(MessageType type, EnrichmentInterface enrichmentInterface) {
        MessageInterface messageInterface = new PredefinedInterface(
                ClassName.of(enrichmentInterface.getInterfaceName()),
                MessageInterfaceParameters.empty()
        );
        return implementInterface(type, messageInterface);
    }

    private Optional<CompilerOutput> postfixBasedInterface(MessageType type) {
        List<GeneratedInterface> generatedInterfaces = patterns.getGeneratedInterfaceList();
        String sourceFilePath = type.sourceFile()
                                    .getPath()
                                    .toString();
        return generatedInterfaces
                .stream()
                .filter(target -> !target.getInterfaceName()
                                         .isEmpty())
                .filter(target -> sourceFilePath.contains(target.getFilter()
                                                                .getFilePostfix()))
                .map(target -> implementByTargetTarget(type, target))
                .findFirst();
    }

    private static CompilerOutput implementByTargetTarget(MessageType type,
                                                          GeneratedInterface target) {
        MessageInterface messageInterface = new PredefinedInterface(
                ClassName.of(target.getInterfaceName()),
                MessageInterfaceParameters.empty()
        );
        return implementInterface(type, messageInterface);
    }
}
