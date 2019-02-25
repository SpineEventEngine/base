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
import io.spine.protoc.MethodFactory;
import io.spine.tools.protoc.CompilerOutput;
import io.spine.tools.protoc.EnrichmentMethod;
import io.spine.tools.protoc.GeneratedMethod;
import io.spine.tools.protoc.GeneratedMethodsConfig;
import io.spine.tools.protoc.TypeScanner;
import io.spine.tools.protoc.UuidMethod;
import io.spine.type.MessageType;

import java.util.List;
import java.util.function.Function;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static io.spine.validate.Validate.isDefault;

/**
 * Scans the given type for a match upon patterns defined in {@link GeneratedMethodsConfig}.
 */
final class GeneratedMethodScanner extends TypeScanner<GeneratedMethod> {

    private final GeneratedMethodsConfig config;
    private final MethodFactories methodFactories;

    GeneratedMethodScanner(GeneratedMethodsConfig config) {
        super();
        this.config = config;
        this.methodFactories = new MethodFactories(config.getFactoryConfiguration());
    }

    @Override
    protected ImmutableList<CompilerOutput> enrichmentMessage(MessageType type) {
        EnrichmentMethod enrichmentMethod = config.getEnrichmentMethod();
        if (isDefault(enrichmentMethod)) {
            return ImmutableList.of();
        }
        return generateMethods(enrichmentMethod.getFactoryName(), type);
    }

    @Override
    protected ImmutableList<CompilerOutput> uuidMessage(MessageType type) {
        UuidMethod uuidMethod = config.getUuidMethod();
        if (isDefault(uuidMethod)) {
            return ImmutableList.of();
        }
        return generateMethods(uuidMethod.getFactoryName(), type);
    }

    @Override
    protected List<GeneratedMethod> filePatterns() {
        return config.getGeneratedMethodList();
    }

    @Override
    protected MatchesPattern matchesPattern(MessageType type) {
        return new MatchesPattern(type, GeneratedMethod::getFilter);
    }

    @Override
    protected IsNotBlank isNotBlank() {
        return new IsNotBlank(GeneratedMethod::getFactoryName);
    }

    @Override
    protected Function<GeneratedMethod, ImmutableList<CompilerOutput>>
    filePatternMapper(MessageType type) {
        return new GenerateMethods(type);
    }

    private class GenerateMethods implements Function<GeneratedMethod, ImmutableList<CompilerOutput>> {

        private final MessageType type;

        private GenerateMethods(MessageType type) {
            this.type = type;
        }

        @Override
        public ImmutableList<CompilerOutput> apply(GeneratedMethod spec) {
            return generateMethods(spec.getFactoryName(), type);
        }
    }

    private ImmutableList<CompilerOutput> generateMethods(String factoryName, MessageType type) {
        MethodFactory factory = methodFactories.newFactoryFor(factoryName);
        return factory
                .newMethodsFor(type)
                .stream()
                .map(methodBody -> MessageMethod.from(methodBody, type))
                .collect(toImmutableList());
    }
}
