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
 * An abstract base for Protoc code generators.
 *
 * <p>The code generation is split into two processing pipelines:
 * <ul>
 *     <li>specific messages (currently only {@link UuidValue} messages are treated this way);
 *     <li>file pattern-based messages.
 * </ul>
 *
 * <p>If a message is classified as a {@link UuidValue}
 * {@link #generateForUuidMessage(MessageType) UUID generation} is used.
 *
 * <p>For the file pattern-based messages, the implementation is responsible for defining what
 * the file patterns are, but the abstract base holds the generic pattern processing flow:
 * <ul>
 *     <li>filter out code generation tasks with blank target field (e.g. interface name);
 *     <li>apply custom generator-specific filtering (e.g. only top-level messages should implement
 *         an interface);
 *     <li>filter out all code generation tasks which pattern does not match supplied message;
 *     <li>perform the actual code generation.
 * </ul>
 *
 * @param <T>
 *         type of the code generation task
 */
public abstract class AbstractCodeGenerator<T extends Message> {

    protected AbstractCodeGenerator() {
    }

    /**
     * Generates code for the supplied {@code type}.
     */
    public ImmutableList<CompilerOutput> generate(MessageType type) {
        if (UuidValue.classifier()
                     .test(type)) {
            return generateForUuidMessage(type);
        }
        ImmutableList<CompilerOutput> result = codeGenerationTasks()
                .stream()
                .filter(isNotBlank())
                .filter(customFilter(type))
                .filter(matchesPattern(type))
                .map(generateCode(type))
                .flatMap(List::stream)
                .collect(toImmutableList());
        return result;
    }

    /**
     * Generates code for the supplied {@link UuidValue UUID message} {@code type}.
     */
    protected abstract ImmutableList<CompilerOutput> generateForUuidMessage(MessageType type);

    /**
     * Returns list of code generation tasks.
     */
    protected abstract ImmutableList<T> codeGenerationTasks();

    /**
     * Creates a file type pattern matcher predicate.
     */
    protected abstract MatchesPattern matchesPattern(MessageType type);

    /**
     * Creates a predicate that ensured that a target code generation field is not empty.
     */
    protected abstract IsNotBlank isNotBlank();

    /**
     * Creates a custom code generation task predicate.
     */
    protected Predicate<T> customFilter(MessageType type) {
        return configuration -> true;
    }

    /**
     * Creates a file pattern mapping function.
     */
    protected abstract CodeGenerationFn<T> generateCode(MessageType type);

    /**
     * Ensures that a code generation task has a specific {@code target} field filled.
     */
    protected class IsNotBlank implements Predicate<T> {

        private final Function<T, String> targetExtractor;

        public IsNotBlank(Function<T, String> targetExtractor) {
            this.targetExtractor = targetExtractor;
        }

        @Override
        public boolean test(T configuration) {
            String target = targetExtractor.apply(configuration);
            return target != null && !target.trim()
                                            .isEmpty();
        }
    }

    /**
     * Determines whether a specific {@code codeGenerationTask} should be applied to a specified
     * {@code type}.
     */
    protected class MatchesPattern implements Predicate<T> {

        private final String protoFileName;
        private final FilePatternExtractorFn<T> filePatternExtractor;

        public MatchesPattern(MessageType type, FilePatternExtractorFn<T> filePatternExtractor) {
            checkNotNull(type);
            checkNotNull(filePatternExtractor);
            this.protoFileName = type.declaringFileName()
                                     .value();
            this.filePatternExtractor = filePatternExtractor;
        }

        @Override
        public boolean test(T codeGenerationTask) {
            checkNotNull(codeGenerationTask);
            FilePattern pattern = filePatternExtractor.apply(codeGenerationTask);
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

    /**
     * Generates code from the supplied {@code code generation task}.
     *
     * @param <T>
     *         type of the code generation task
     */
    @FunctionalInterface
    protected interface CodeGenerationFn<T> extends Function<T, ImmutableList<CompilerOutput>> {
    }

    /**
     * {@link FilePattern} extractor function.
     *
     * @param <T>
     *         type of the code generation task
     */
    @FunctionalInterface
    protected interface FilePatternExtractorFn<T> extends Function<T, FilePattern>{
    }
}
