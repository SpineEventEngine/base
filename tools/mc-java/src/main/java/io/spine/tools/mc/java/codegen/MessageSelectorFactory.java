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

package io.spine.tools.mc.java.codegen;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.spine.code.proto.FileName;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static io.spine.util.Exceptions.newIllegalArgumentException;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * Creates {@linkplain MessageSelector message selectors}.
 */
public final class MessageSelectorFactory {

    @VisibleForTesting
    static final String PREFIX = "prefix";
    @VisibleForTesting
    static final String SUFFIX = "suffix";
    @VisibleForTesting
    static final String REGEX = "regex";

    public static final MessageSelectorFactory INSTANCE = new MessageSelectorFactory();

    /** Prevents direct instantiation. **/
    private MessageSelectorFactory() {
    }

    /**
     * Creates a {@code ByPattern} out of the supplied configuration.
     *
     * <p>The supported configuration parameters are:
     * <ul>
     *     <li>{@code suffix} — for the {@link WithSuffix};
     *     <li>{@code prefix} — for the {@link WithPrefix};
     *     <li>{@code regex} — for the {@link ByRegex}.
     * </ul>
     */
    public ByPattern inFiles(Map<String, String> conf) {
        checkNotNull(conf);
        Parser parser = new Parser();
        return parser.fileSelector(conf);
    }

    /**
     * Creates a {@code WithSuffix} selector that matches {@code all} Protobuf files.
     *
     * <p>It is expected that a Protobuf file ends with {@link FileName#EXTENSION .proto} extension.
     */
    public ByPattern all() {
        WithSuffix result = new WithSuffix(FileName.EXTENSION);
        return result;
    }

    /**
     * Creates {@code inFiles} {@code prefix} configuration.
     */
    public static ImmutableMap<String, String> prefix(String prefix) {
        checkNotEmptyOrBlank(prefix);
        return ImmutableMap.of(PREFIX, prefix);
    }

    /**
     * Creates {@code inFiles} {@code suffix} configuration.
     */
    public static ImmutableMap<String, String> suffix(String suffix) {
        checkNotEmptyOrBlank(suffix);
        return ImmutableMap.of(SUFFIX, suffix);
    }

    /**
     * Creates {@code inFiles} {@code regex} configuration.
     */
    public static ImmutableMap<String, String> regex(String regex) {
        checkNotEmptyOrBlank(regex);
        return ImmutableMap.of(REGEX, regex);
    }

    /**
     * {@code inFiles} configuration parser.
     */
    private static class Parser {

        private final Map<String, Function<String, ByPattern>> configurations;

        private Parser() {
            configurations = Maps.newConcurrentMap();
            configurations.put(SUFFIX, WithSuffix::new);
            configurations.put(PREFIX, WithPrefix::new);
            configurations.put(REGEX, ByRegex::new);
        }

        private ByPattern fileSelector(Map<String, String> conf) {
            checkArgument(
                    conf.size() == 1,
                    "File selector should have a single value, but had: '%s'",
                    conf
            );
            Set<String> patternTypes = configurations.keySet();
            for (String patternType : patternTypes) {
                String filePattern = conf.get(patternType);
                if (!isNullOrEmpty(filePattern)) {
                    Function<String, ByPattern> factory = configurations.get(patternType);
                    return factory.apply(filePattern);
                }
            }
            throw newIllegalArgumentException(
                    "Unsupported parameter `%s` supplied. Supported parameters are: `%s`.",
                    conf, patternTypes
            );
        }
    }
}
