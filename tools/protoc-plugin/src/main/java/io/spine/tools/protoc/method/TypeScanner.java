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

package io.spine.tools.protoc.method;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Message;
import io.spine.code.proto.MessageType;
import io.spine.tools.protoc.CompilerOutput;
import io.spine.tools.protoc.TypeFilter;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static io.spine.base.MessageClassifiers.uuidContainer;

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
    ImmutableList<CompilerOutput> scan(MessageType type) {
        if (uuidContainer().test(type)) {
            return uuidMessage(type);
        }
        if (type.isEnrichment()) {
            return enrichmentMessage(type);
        }
        ImmutableList<CompilerOutput> result = filePatterns()
                .stream()
                .filter(isNotBlank())
                .filter(matchesPattern(type))
                .map(filePatternMapper(type))
                .flatMap(List::stream)
                .collect(toImmutableList());
        return result;
    }

    protected abstract ImmutableList<CompilerOutput> enrichmentMessage(MessageType type);

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

    /**
     * Creates a file pattern mapping function.
     */
    protected abstract Function<G, ImmutableList<CompilerOutput>>
    filePatternMapper(MessageType type);

    protected class IsNotBlank implements Predicate<G> {

        private final Function<G, String> targetExtractor;

        protected IsNotBlank(Function<G, String> targetExtractor) {
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

        private final String sourceFilePath;
        private final Function<G, TypeFilter> typeFilterExtractor;

        protected MatchesPattern(MessageType type, Function<G, TypeFilter> typeFilterExtractor) {
            checkNotNull(type);
            this.sourceFilePath = type.sourceFile()
                                      .getPath()
                                      .toString();
            this.typeFilterExtractor = typeFilterExtractor;
        }

        @Override
        public boolean test(G configuration) {
            checkNotNull(configuration);
            TypeFilter filter = typeFilterExtractor.apply(configuration);
            if (filter.getValueCase() != TypeFilter.ValueCase.FILE_POSTFIX) {
                return false;
            }
            return sourceFilePath.contains(filter.getFilePostfix());
        }
    }

}
