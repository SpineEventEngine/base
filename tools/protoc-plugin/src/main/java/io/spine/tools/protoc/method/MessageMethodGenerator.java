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
import io.spine.tools.protoc.AbstractCodeGenerator;
import io.spine.tools.protoc.CompilerOutput;
import io.spine.tools.protoc.GenerateMethod;
import io.spine.tools.protoc.MethodsGeneration;
import io.spine.tools.protoc.UuidGenerateMethod;
import io.spine.type.MessageType;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static io.spine.validate.Validate.isDefault;

/**
 * Generates new methods for types based on {@link GenerateMethod} tasks defined in
 * {@link MethodsGeneration configuration}.
 */
final class MessageMethodGenerator extends AbstractCodeGenerator<GenerateMethod> {

    private final MethodFactories methodFactories;
    private final UuidGenerateMethod uuidGenerateMethod;
    private final boolean generateUuidMethod;
    private final ImmutableList<GenerateMethod> generateMethods;

    MessageMethodGenerator(MethodsGeneration config) {
        super();
        this.uuidGenerateMethod = config.getUuidMethod();
        this.generateUuidMethod = !isDefault(uuidGenerateMethod);
        this.generateMethods = ImmutableList.copyOf(config.getGenerateMethodList());
        this.methodFactories = new MethodFactories(config.getFactoryConfiguration());
    }

    @Override
    protected ImmutableList<CompilerOutput> generateForUuidMessage(MessageType type) {
        if (generateUuidMethod) {
            return generateMethods(uuidGenerateMethod.getFactoryName(), type);
        }
        return ImmutableList.of();
    }

    @Override
    protected ImmutableList<GenerateMethod> codeGenerationTasks() {
        return generateMethods;
    }

    @Override
    protected MatchesPattern matchesPattern(MessageType type) {
        return new MatchesPattern(type, GenerateMethod::getPattern);
    }

    @Override
    protected IsNotBlank isNotBlank() {
        return new IsNotBlank(GenerateMethod::getFactoryName);
    }

    @Override
    protected GenerateMethods generateCode(MessageType type) {
        return new GenerateMethods(type);
    }

    private class GenerateMethods implements CodeGenerationFn<GenerateMethod> {

        private final MessageType type;

        private GenerateMethods(MessageType type) {
            this.type = type;
        }

        @Override
        public ImmutableList<CompilerOutput> apply(GenerateMethod task) {
            return generateMethods(task.getFactoryName(), type);
        }
    }

    private ImmutableList<CompilerOutput> generateMethods(String factoryName, MessageType type) {
        MethodFactory factory = methodFactories.newFactory(factoryName);
        return factory
                .newMethodsFor(type)
                .stream()
                .map(methodBody -> MessageMethod.from(methodBody, type))
                .collect(toImmutableList());
    }
}
