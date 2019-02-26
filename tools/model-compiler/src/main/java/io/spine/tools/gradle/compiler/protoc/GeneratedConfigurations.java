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
import com.google.protobuf.Message;
import io.spine.annotation.Internal;

/**
 * Abstract base for Gradle extension configurations related to Spine Protoc plugin.
 *
 * @param <M>
 *         file pattern Protobuf configuration
 * @param <F>
 *         file pattern factory
 * @param <U>
 *         UUID message selector
 * @param <E>
 *         enrichment message selector
 * @param <C>
 *         Protobuf configuration
 * @see GeneratedInterfaces
 * @see GeneratedMethods
 */
abstract class GeneratedConfigurations<M extends Message,
        F extends FilePatternFactory<M, ?, ?>,
        U extends UuidMessage<?>,
        E extends EnrichmentMessage<?>,
        C extends Message> {

    private final F filePatternFactory;

    GeneratedConfigurations(F filePatternFactory) {
        this.filePatternFactory = filePatternFactory;
    }

    /**
     * Obtains current unique pattern configurations.
     */
    ImmutableSet<FilePattern<M>> patternConfigurations() {
        return filePatternFactory.patterns();
    }

    /**
     * Configures code generation for messages declared in files matching a given pattern.
     *
     * <p>Sample usage is:
     * <pre>
     *     {@code
     *     filePattern().endsWith("events.proto")
     *     }
     * </pre>
     *
     * @return a configuration object for Proto files matching the pattern
     */
    public F filePattern() {
        return filePatternFactory;
    }

    /**
     * Configures code generation for messages with a single {@code string} field called
     * {@code uuid}.
     *
     * <p>This method functions similarly to the {@link #filePattern()} except for
     * several differences:
     * <ul>
     * <li>the file in which the message type is defined does not matter;
     * <li>nested definitions are affected as well as top-level ones.
     * </ul>
     *
     * @return a configuration object for Proto messages matching UUID message pattern
     */
    public abstract U uuidMessage();

    /**
     * Configures code generation for messages with {@code (enrichment_for)} option.
     *
     * <p>This method functions are similar to the {@link #filePattern} except for
     * several differences:
     * <ul>
     * <li>the file in which the message type is defined does not matter;
     * </ul>
     *
     * @return a configuration object for Proto messages matching enrichment message pattern
     */
    public abstract E enrichmentMessage();

    /**
     * Converts current configuration into its Protobuf counterpart.
     */
    @Internal
    public abstract C asProtocConfig();
}
