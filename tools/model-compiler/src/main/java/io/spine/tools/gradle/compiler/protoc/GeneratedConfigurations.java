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

package io.spine.tools.gradle.compiler.protoc;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.protobuf.Message;
import io.spine.annotation.Internal;
import io.spine.code.java.ClassName;
import io.spine.tools.protoc.ConfigByPattern;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.protoc.ProtocTaskConfigs.byPatternConfig;

/**
 * Abstract base for Gradle extension configurations related to Spine Protoc plugin.
 *
 * @param <C>
 *         Protobuf configuration
 * @see GeneratedInterfaces
 * @see GeneratedMethods
 */
abstract class GeneratedConfigurations<C extends Message> {

    private final Map<FileSelector, ClassName> patterns;

    GeneratedConfigurations() {
        this.patterns = Maps.newConcurrentMap();
    }

    /**
     * Returns {@link FileSelectorFactory}.
     */
    public FileSelectorFactory filePattern() {
        return FileSelectorFactory.INSTANCE;
    }

    /**
     * Returns {@link UuidMessage} selector.
     */
    public UuidMessage uuidMessage() {
        return UuidMessage.INSTANCE;
    }

    /**
     * Ignores code generation for Protobuf files that matches supplied {@code pattern}.
     *
     * <p>Sample usage is:
     * <pre>
     *     {@code
     *     ignore filePattern().endsWith("events.proto")
     *     }
     * </pre>
     */
    public final void ignore(FileSelector pattern) {
        checkNotNull(pattern);
        patterns.remove(pattern);
    }

    /**
     * Ignores code generation for UUID messages.
     *
     * <p>Sample usage is:
     * <pre>
     *     {@code
     *     ignore uuidMessage()
     *     }
     * </pre>
     */
    public abstract void ignore(UuidMessage uuidMessage);

    /**
     * Converts current configuration into its Protobuf counterpart.
     */
    @Internal
    public abstract C asProtocConfig();

    /**
     * Adds a new {@link FileSelector} configuration with a supplied {@link ClassName}.
     *
     * <p>The {@code className} can represent a fully-qualified name of an interface of a
     * method factory.
     */
    void addPattern(FileSelector pattern, ClassName className) {
        patterns.put(pattern, className);
    }

    /**
     * Obtains current unique pattern configurations.
     */
    ImmutableSet<Map.Entry<FileSelector, ClassName>> patternConfigurations() {
        return ImmutableSet.copyOf(patterns.entrySet());
    }

    /**
     * Converts {@link FileSelector} — {@link ClassName} pair to {@link ConfigByPattern}.
     */
    static ConfigByPattern toPatternConfig(Map.Entry<FileSelector, ClassName> e) {
        FileSelector fileSelector = e.getKey();
        ClassName className = e.getValue();
        return byPatternConfig(className.value(), fileSelector.toProto());
    }
}
