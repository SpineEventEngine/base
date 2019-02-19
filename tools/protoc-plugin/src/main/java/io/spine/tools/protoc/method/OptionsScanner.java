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
import io.spine.code.proto.MessageType;
import io.spine.protoc.MethodFactory;
import io.spine.tools.protoc.CompilerOutput;
import io.spine.tools.protoc.GeneratedMethod;
import io.spine.tools.protoc.SpineProtocConfig;
import io.spine.tools.protoc.TypeFilter;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.google.common.collect.ImmutableList.toImmutableList;

/**
 * Scans the given type for a match upon options defined in {@link SpineProtocConfig}.
 */
final class OptionsScanner {

    private final SpineProtocConfig config;

    OptionsScanner(SpineProtocConfig config) {
        this.config = config;
    }

    /**
     * Finds methods to be generated for the given type.
     */
    ImmutableList<CompilerOutput> scan(MessageType type) {
        List<GeneratedMethod> generatedMethods = config.getGeneratedMethodList();

        return generatedMethods
                .stream()
                .filter(OptionsScanner::hasNotBlankName)
                .filter(new HasProtoOption(type))
                .map(new GenerateMethods(type))
                .flatMap(List::stream)
                .collect(toImmutableList());
    }

    private static boolean hasNotBlankName(GeneratedMethod spec) {
        return !spec.getFactoryName()
                    .trim()
                    .isEmpty();
    }

    private static class GenerateMethods implements Function<GeneratedMethod, ImmutableList<CompilerOutput>> {

        private final MessageType type;

        private GenerateMethods(MessageType type) {
            this.type = type;
        }

        @Override
        public ImmutableList<CompilerOutput> apply(GeneratedMethod spec) {
            MethodFactory factory = MethodFactories.newFactoryFor(spec);
            return factory
                    .newMethodsFor(type)
                    .stream()
                    .map(methodBody -> MessageMethod.from(methodBody, type))
                    .collect(toImmutableList());
        }
    }

    private static class HasProtoOption implements Predicate<GeneratedMethod> {

        private final MessageType type;

        private HasProtoOption(MessageType type) {
            this.type = type;
        }

        @Override
        public boolean test(GeneratedMethod method) {
            TypeFilter filter = method.getFilter();
            if (filter.getValueCase() != TypeFilter.ValueCase.OPTION_NAME) {
                return false;
            }
            return MessageOptions.hasOption(filter.getOptionName(), type);
        }
    }
}
