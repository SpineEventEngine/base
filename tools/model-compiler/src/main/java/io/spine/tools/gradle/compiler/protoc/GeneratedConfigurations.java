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

import com.google.common.collect.ImmutableList;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newConcurrentMap;

/**
 * Abstract base for Gradle extension configurations related to Spine Protoc plugin.
 *
 * @param <T>
 *         actual configuration type
 * @see GeneratedInterfaces
 */
abstract class GeneratedConfigurations<T extends ProtocConfig> {

    private final Map<FilePattern, T> patternConfigs;

    GeneratedConfigurations() {
        patternConfigs = newConcurrentMap();
    }

    /**
     * Configures code generation for messages declared in files matching a given pattern.
     *
     * <p>Sample usage is:
     * <pre>
     *     {@code
     *     filePattern(endsWith("events.proto"))
     *     }
     * </pre>
     *
     * @param pattern
     *         the file pattern
     * @return a configuration object for Proto files matching the pattern
     */
    public T filePattern(FilePattern pattern) {
        checkNotNull(pattern);
        T config = patternConfiguration(pattern);
        patternConfigs.put(pattern, config);
        return config;
    }

    abstract T patternConfiguration(@NonNull FilePattern pattern);

    /**
     * Obtains current pattern configurations.
     */
    ImmutableList<T> patternConfigurations(){
        return ImmutableList.copyOf(patternConfigs.values());
    }

    /**
     * Creates a file pattern to match files names of which end with a given postfix.
     *
     * @see #filePattern(FilePattern)
     */
    @SuppressWarnings("WeakerAccess") // Gradle DSL public API
    public PostfixPattern endsWith(String postfix) {
        return new PostfixPattern(postfix);
    }
}
