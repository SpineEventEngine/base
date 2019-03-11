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
import io.spine.tools.protoc.CodeGenerationTask;
import io.spine.tools.protoc.CodeGenerationTasks;
import io.spine.tools.protoc.CompilerOutput;
import io.spine.tools.protoc.GenerateMethod;
import io.spine.tools.protoc.MethodsGeneration;
import io.spine.tools.protoc.SpineProtoGenerator;
import io.spine.tools.protoc.SpineProtocConfig;
import io.spine.type.MessageType;
import io.spine.type.Type;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The {@link SpineProtoGenerator} implementation generating additional message methods.
 *
 * <p>The generator produces {@link CompilerOutput compiler output} that fits into the message's
 * {@link io.spine.tools.protoc.InsertionPoint#class_scope class scope} insertion point.
 */
public final class MethodGenerator extends SpineProtoGenerator {

    private final CodeGenerationTasks codeGenerationTasks;

    /** Prevents singleton class instantiation. */
    private MethodGenerator(ImmutableList<CodeGenerationTask> codeGenerationTasks) {
        super();
        this.codeGenerationTasks = new CodeGenerationTasks(codeGenerationTasks);
    }

    /**
     * Retrieves the single instance of the {@code MethodGenerator}.
     */
    public static MethodGenerator instance(SpineProtocConfig spineProtocConfig) {
        checkNotNull(spineProtocConfig);
        MethodsGeneration config = spineProtocConfig.getMethodsGeneration();
        MethodFactories methodFactories = new MethodFactories(config.getFactoryConfiguration());
        ImmutableList.Builder<CodeGenerationTask> codeGenerationTasks = ImmutableList
                .<CodeGenerationTask>builder()
                .add(new GenerateUuidMethods(methodFactories, config.getUuidMethod()));
        for (GenerateMethod taskConfiguration : config.getGenerateMethodList()) {
            codeGenerationTasks.add(new GenerateMethods(methodFactories, taskConfiguration));
        }
        return new MethodGenerator(codeGenerationTasks.build());
    }

    @Override
    protected Collection<CompilerOutput> generate(Type<?, ?> type) {
        if (!(type instanceof MessageType)) {
            return ImmutableList.of();
        }
        MessageType messageType = (MessageType) type;
        ImmutableList<CompilerOutput> result = codeGenerationTasks.generateFor(messageType);
        return result;
    }
}
