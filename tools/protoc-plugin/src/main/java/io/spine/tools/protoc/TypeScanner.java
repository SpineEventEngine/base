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

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Message;
import io.spine.base.UuidValue;
import io.spine.type.MessageType;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;

/**
 * An abstract base for generated methods scanners.
 *
 * @param <G>
 *         generated code configuration
 */
public abstract class TypeScanner<G extends Message> {

    protected TypeScanner() {
    }

    /**
     * Finds methods to be generated for the given type.
     */
    public ImmutableList<CompilerOutput> scan(MessageType type) {
        if (UuidValue.classifier()
                     .test(type)) {
            return uuidMessage(type);
        }
        ImmutableList<CompilerOutput> result = filePatterns()
                .stream()
                .filter(isNotBlank())
                .filter(customFilter(type))
                .filter(matchesPattern(type))
                .map(filePatternMapper(type))
                .flatMap(List::stream)
                .collect(toImmutableList());
        return result;
    }

    protected abstract ImmutableList<CompilerOutput> uuidMessage(MessageType type);

    protected abstract List<G> filePatterns();

    /**
     * Creates a file type pattern matcher predicate.
     */
    protected abstract MatchesPattern matchesPattern(MessageType type);

    /**
     * Creates a predicate that ensured that a target code generation field is not empty.
     */
    protected abstract IsNotBlank isNotBlank();

    protected Predicate<G> customFilter(MessageType type) {
        return configuration -> true;
    }

    /**
     * Creates a file pattern mapping function.
     */
    protected abstract Function<G, ImmutableList<CompilerOutput>>
    filePatternMapper(MessageType type);

    protected class IsNotBlank implements Predicate<G> {

        private final Function<G, String> targetExtractor;

        public IsNotBlank(Function<G, String> targetExtractor) {
            this.targetExtractor = targetExtractor;
        }

        @Override
        public boolean test(G configuration) {
            String target = targetExtractor.apply(configuration);
            return target != null && !target.trim()
                                            .isEmpty();
        }
    }

    protected class MatchesPattern implements Predicate<G> {

        private final String protoFileName;
        private final Function<G, FilePattern> typeFilterExtractor;

        public MatchesPattern(MessageType type, Function<G, FilePattern> typeFilterExtractor) {
            checkNotNull(type);
            this.protoFileName = type.declaringFileName()
                                     .value();
            this.typeFilterExtractor = typeFilterExtractor;
        }

        @Override
        public boolean test(G configuration) {
            checkNotNull(configuration);
            FilePattern pattern = typeFilterExtractor.apply(configuration);
            switch (pattern.getValueCase()) {
                case FILE_POSTFIX:
                    return protoFileName.endsWith(pattern.getFilePostfix());
                case FILE_PREFIX:
                    return protoFileName.startsWith(pattern.getFilePrefix());
                case REGEX:
                    return protoFileName.matches(pattern.getRegex());
                case VALUE_NOT_SET:
                default:
                    return false;
            }
        }
    }
}
