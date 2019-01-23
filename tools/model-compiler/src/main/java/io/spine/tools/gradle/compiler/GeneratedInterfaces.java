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

package io.spine.tools.gradle.compiler;

import io.spine.code.java.ClassName;
import io.spine.tools.protoc.InterfaceTarget;
import io.spine.tools.protoc.SpineProtocConfig;
import io.spine.tools.protoc.UuidInterface;
import org.checkerframework.checker.regex.qual.Regex;

import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Sets.newConcurrentHashSet;

public final class GeneratedInterfaces {

    private final Set<PatternDefinitionGroup> patternGroups;
    private final UuidDefinitionGroup uuidDefinitionGroup = new UuidDefinitionGroup();

    GeneratedInterfaces() {
        this.patternGroups = newConcurrentHashSet();
    }

    public ProtoDefinitionGroup filePattern(@Regex String pattern) {
        PatternDefinitionGroup group = new PatternDefinitionGroup(pattern);
        patternGroups.add(group);
        return group;
    }

    public ProtoDefinitionGroup uuidMessage() {
        return uuidDefinitionGroup;
    }

    SpineProtocConfig asProtocConfig() {
        Optional<ClassName> name = uuidDefinitionGroup.interfaceName();
        UuidInterface uuidInterface = name
                .map(className -> UuidInterface
                        .newBuilder()
                        .setInterfaceName(className.value())
                        .build())
                .orElse(UuidInterface.getDefaultInstance());
        SpineProtocConfig.Builder result = SpineProtocConfig
                .newBuilder()
                .setUuidInterface(uuidInterface);
        patternGroups.stream()
                     .map(config -> InterfaceTarget
                             .newBuilder()
                             .setFileSuffix(config.fileSuffix())
                             .setInterfaceName(config.interfaceName()
                                                     .map(ClassName::value)
                                                     .orElse(""))
                             .build())
                     .forEach(result::addInterfaceTarget);
        return result.build();
    }
}
