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

package io.spine.tools.mc.java.gradle;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.protobuf.Message;
import io.spine.annotation.Internal;
import io.spine.code.java.ClassName;
import io.spine.tools.java.protoc.ConfigByPattern;
import io.spine.tools.mc.java.gradle.selector.MessageSelectorFactory;
import io.spine.tools.mc.java.gradle.selector.PatternSelector;

import java.util.Map;

import static io.spine.tools.mc.java.gradle.ProtocTaskConfigs.byPatternConfig;

/**
 * Abstract base for Gradle extension configurations related to Spine Protoc plugin.
 *
 * @param <C>
 *         Protobuf configuration
 * @see Interfaces
 * @see Methods
 * @see NestedClasses
 */
public abstract class ModelCompilerConfiguration<C extends Message> {

    private final Map<PatternSelector, ClassName> patterns;

    ModelCompilerConfiguration() {
        this.patterns = Maps.newConcurrentMap();
    }

    /**
     * Returns {@link MessageSelectorFactory}.
     */
    public MessageSelectorFactory messages() {
        return MessageSelectorFactory.INSTANCE;
    }

    /**
     * Converts current configuration into its Protobuf counterpart.
     */
    @Internal
    public abstract C asProtocConfig();

    /**
     * Adds a new {@link PatternSelector} configuration with a supplied {@link ClassName}.
     *
     * <p>The {@code className} can represent a fully-qualified name of an interface, method
     * factory, nested class factory or field type.
     */
    void addPattern(PatternSelector pattern, ClassName className) {
        patterns.put(pattern, className);
    }

    /**
     * Obtains current unique pattern configurations.
     */
    protected ImmutableSet<Map.Entry<PatternSelector, ClassName>> patternConfigurations() {
        return ImmutableSet.copyOf(patterns.entrySet());
    }

    /**
     * Converts {@link PatternSelector} — {@link ClassName} pair to {@link ConfigByPattern}.
     */
    public static ConfigByPattern toPatternConfig(Map.Entry<PatternSelector, ClassName> e) {
        PatternSelector patternSelector = e.getKey();
        ClassName className = e.getValue();
        return byPatternConfig(className, patternSelector.toProto());
    }
}
