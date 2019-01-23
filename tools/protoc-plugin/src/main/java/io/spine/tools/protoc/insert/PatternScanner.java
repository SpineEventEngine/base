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

import io.spine.code.java.ClassName;
import io.spine.code.proto.MessageType;
import io.spine.tools.protoc.CompilerOutput;
import io.spine.tools.protoc.InterfaceTarget;
import io.spine.tools.protoc.SpineProtocConfig;
import io.spine.tools.protoc.UuidInterface;

import java.util.List;
import java.util.Optional;

import static io.spine.base.MessageClassifiers.uuidContainer;
import static io.spine.tools.protoc.insert.InsertionPoint.implementInterface;
import static io.spine.validate.Validate.isNotDefault;

final class PatternScanner {

    private final SpineProtocConfig patterns;

    PatternScanner(SpineProtocConfig patterns) {
        this.patterns = patterns;
    }

    Optional<CompilerOutput> scan(MessageType type) {
        UuidInterface uuidInterface = patterns.getUuidInterface();
        if (isNotDefault(uuidInterface) && uuidContainer().test(type)) {
            return Optional.of(uuidInterface(type, uuidInterface));
        } else if (type.isTopLevel()) {
            return postfixBasedInterface(type);
        } else {
            return Optional.empty();
        }
    }

    private Optional<CompilerOutput> postfixBasedInterface(MessageType type) {
        List<InterfaceTarget> interfaceTargets = patterns.getInterfaceTargetList();
        String sourceFilePath = type.sourceFile()
                                    .getPath()
                                    .toString();
        return interfaceTargets
                .stream()
                .filter(target -> !target.getInterfaceName().isEmpty())
                .filter(target -> sourceFilePath.contains(target.getFileSuffix()))
                .map(target -> implementByTargetTarget(type, target))
                .findFirst();
    }

    private static CompilerOutput implementByTargetTarget(MessageType type,
                                                          InterfaceTarget target) {
        MessageInterface messageInterface = new PredefinedInterface(
                ClassName.of(target.getInterfaceName()),
                MessageInterfaceParameters.empty()
        );
        return implementInterface(type, messageInterface);
    }

    private static CompilerOutput uuidInterface(MessageType type, UuidInterface uuidInterface) {
        ClassName interfaceName = ClassName.of(uuidInterface.getInterfaceName());
        MessageInterfaceParameters parameters =
                MessageInterfaceParameters.of(new IdentityParameter());
        MessageInterface messageInterface = new PredefinedInterface(interfaceName, parameters);
        CompilerOutput insertionPoint = implementInterface(type, messageInterface);
        return insertionPoint;
    }
}
