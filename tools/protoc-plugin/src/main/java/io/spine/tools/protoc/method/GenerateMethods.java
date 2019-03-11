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
import io.spine.tools.protoc.CompilerOutput;
import io.spine.tools.protoc.FilePatternMatcher;
import io.spine.tools.protoc.GenerateMethod;
import io.spine.type.MessageType;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.validate.Validate.checkNotDefault;

/**
 * Generates methods for supplied type based on code generation task {@link GenerateMethod
 * configuration}.
 */
final class GenerateMethods extends AbstractMethodGenerationTask {

    private final FilePatternMatcher patternMatcher;

    GenerateMethods(MethodFactories methodFactories, GenerateMethod config) {
        super(methodFactories, config.getFactoryName());
        checkNotDefault(config.getPattern());
        this.patternMatcher = new FilePatternMatcher(config.getPattern());
    }

    /**
     * Generates new method for supplied {@code type}.
     *
     * <p>No methods are generated if:
     *
     * <ul>
     *     <li>the method factory name is empty;
     *     <li>the type file name does not match supplied
     *     {@link io.spine.tools.protoc.FilePattern pattern}.
     * </ul>
     */
    @Override
    public ImmutableList<CompilerOutput> generateFor(MessageType type) {
        checkNotNull(type);
        if (isFactoryNameEmpty() || !patternMatcher.test(type)) {
            return ImmutableList.of();
        }
        return generateMethods(type);
    }
}
