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

package io.spine.tools.gradle.compiler.protoc;

import io.spine.annotation.Internal;
import io.spine.code.java.ClassName;
import io.spine.tools.protoc.FilePatterns;
import io.spine.tools.protoc.GeneratedMethod;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.regex.qual.Regex;
import org.checkerframework.checker.signature.qual.FullyQualifiedName;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link GeneratedMethodConfig method} configuration {@link PrefixPattern prefix} pattern
 * selector.
 */
public final class MethodPrefixPattern extends PrefixPattern<GeneratedMethod> implements GeneratedMethodConfig {

    private @Nullable ClassName factoryName;

    /** Prevents direct instantiation. **/
    MethodPrefixPattern(@Regex String prefix) {
        super(prefix);
    }

    @Override
    public void withMethodFactory(@FullyQualifiedName String factoryName) {
        checkNotNull(factoryName);
        this.factoryName = ClassName.of(factoryName);
    }

    @Override
    public void ignore() {
        this.factoryName = null;
    }

    @Internal
    @Override
    public @Nullable ClassName methodFactory() {
        return factoryName;
    }

    @Internal
    @Override
    public GeneratedMethod toProto() {
        return GeneratedMethod
                .newBuilder()
                .setPattern(FilePatterns.filePrefix(getPattern()))
                .setFactoryName(safeName())
                .build();
    }
}
