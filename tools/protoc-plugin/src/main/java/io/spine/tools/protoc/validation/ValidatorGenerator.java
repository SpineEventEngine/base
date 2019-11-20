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

package io.spine.tools.protoc.validation;

import com.google.common.collect.ImmutableSet;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;
import com.squareup.javapoet.JavaFile;
import io.spine.tools.protoc.CompilerOutput;
import io.spine.tools.protoc.NoOpGenerator;
import io.spine.tools.protoc.ProtocPluginFiles;
import io.spine.tools.protoc.SpineProtoGenerator;
import io.spine.tools.protoc.SpineProtocConfig;
import io.spine.tools.validate.MessageValidatorFactory;
import io.spine.type.MessageType;
import io.spine.type.Type;

import java.util.Collection;

import static io.spine.tools.protoc.InsertionPoint.builder_scope;
import static io.spine.tools.protoc.InsertionPoint.class_scope;

public final class ValidatorGenerator extends SpineProtoGenerator {

    /**
     * Prevents direct instantiation.
     */
    private ValidatorGenerator() {
        super();
    }

    public static SpineProtoGenerator instance(SpineProtocConfig config) {
        return config.getSkipValidatingBuilders() || !config.getGenerateKotlinValidation()
               ? NoOpGenerator.instance()
               : new ValidatorGenerator();
    }

    @Override
    protected Collection<CompilerOutput> generate(Type<?, ?> type) {
        if (type instanceof MessageType) {
            MessageValidatorFactory factory = new MessageValidatorFactory((MessageType) type);
            JavaFile validatorClass = factory.generateClass();
            String validatorClassFile = validatorClass.toJavaFileObject()
                                                      .getName();
            File file = ProtocPluginFiles
                    .prepareFile(validatorClassFile)
                    .setContent(validatorClass.toString())
                    .build();
            File builderInsertionPoint = ProtocPluginFiles
                    .prepareFile(type)
                    .setInsertionPoint(builder_scope.forType(type))
                    .setContent(factory.generateVBuild().toString())
                    .build();
            File messageInsertionPoint = ProtocPluginFiles
                    .prepareFile(type)
                    .setInsertionPoint(class_scope.forType(type))
                    .setContent(factory.generateValidate().toString())
                    .build();
            return ImmutableSet.of(() -> file,
                                   () -> builderInsertionPoint,
                                   () -> messageInsertionPoint);
        } else {
            return ImmutableSet.of();
        }
    }
}
