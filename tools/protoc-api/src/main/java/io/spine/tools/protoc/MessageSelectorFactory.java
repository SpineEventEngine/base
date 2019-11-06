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

package io.spine.tools.protoc;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.spine.code.proto.FileName;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static io.spine.util.Exceptions.newIllegalArgumentException;

/**
 * Creates {@link MessageSelector} selectors.
 */
public final class MessageSelectorFactory {

    private static final String PREFIX = "prefix";
    private static final String SUFFIX = "suffix";
    private static final String REGEX = "regex";

    static final MessageSelectorFactory INSTANCE = new MessageSelectorFactory();

    /** Prevents direct instantiation. **/
    private MessageSelectorFactory() {
    }

    /**
     * Creates a new {@link UuidMessage} selector.
     */
    public UuidMessage uuid() {
        return new UuidMessage();
    }

    /**
     * Creates a new {@link EntityState} selector.
     */
    public EntityState entityState() {
        return new EntityState();
    }

    /**
     * Creates a {@link PatternSelector} out of the supplied configuration.
     *
     * <p>The supported configuration parameters are:
     * <ul>
     *     <li>{@code suffix} — for the {@link SuffixSelector};
     *     <li>{@code prefix} — for the {@link PrefixSelector};
     *     <li>{@code regex} — for the {@link RegexSelector}.
     * </ul>
     */
    public PatternSelector inFiles(Map<String, String> conf) {
        checkNotNull(conf);
        Parser parser = new Parser();
        return parser.fileSelector(conf);
    }

    /**
     * Creates a {@link SuffixSelector} selector that matches {@code all} Protobuf files.
     *
     * <p>It is expected that a Protobuf file ends with {@link FileName#EXTENSION .proto} extension.
     */
    public PatternSelector all() {
        SuffixSelector result = new SuffixSelector(FileName.EXTENSION);
        return result;
    }

    /**
     * Creates {@code inFiles} {@code prefix} configuration.
     */
    public static ImmutableMap<String, String> prefix(String prefix) {
        return ImmutableMap.of(PREFIX, checkNotNull(prefix));
    }

    /**
     * Creates {@code inFiles} {@code suffix} configuration.
     */
    public static ImmutableMap<String, String> suffix(String suffix) {
        return ImmutableMap.of(SUFFIX, checkNotNull(suffix));
    }

    /**
     * Creates {@code inFiles} {@code regex} configuration.
     */
    public static ImmutableMap<String, String> regex(String regex) {
        return ImmutableMap.of(REGEX, checkNotNull(regex));
    }

    /**
     * {@code inFiles} configuration parser.
     */
    private static class Parser {

        private final Map<String, Function<String, PatternSelector>> configurations;

        private Parser() {
            configurations = Maps.newConcurrentMap();
            configurations.put(SUFFIX, SuffixSelector::new);
            configurations.put(PREFIX, PrefixSelector::new);
            configurations.put(REGEX, RegexSelector::new);
        }

        private PatternSelector fileSelector(Map<String, String> conf) {
            checkArgument(conf.size() == 1,
                          "File selector should have a single value, but had: '%s'",
                          conf);
            for (Entry<String, Function<String, PatternSelector>> configEntry : configurations.entrySet()) {
                String filePattern = conf.get(configEntry.getKey());
                if (!isNullOrEmpty(filePattern)) {
                    return configEntry.getValue()
                                      .apply(filePattern);
                }
            }
            throw newIllegalArgumentException(
                    "Unsupported parameter `%s` supplied. Supported parameters are: `%s`.",
                    conf, configurations.keySet());
        }
    }
}
