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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.signature.qual.FullyQualifiedName;

import java.util.Optional;

/**
 * Configuration of a generated method for a certain target.
 *
 * @see GeneratedMethods#filePattern()
 * @see GeneratedMethods#uuidMessage()
 * @see GeneratedMethods#enrichmentMessage()
 */
public interface GeneratedMethodConfig extends ProtocConfig {

    /**
     * For the given target sets a {@link io.spine.protoc.MethodFactory MethodFactory}.
     *
     * <p>The method factory should be accessible at the classpath.
     *
     * @param methodFactoryName
     *         the FQN of the method factory
     */
    void withMethodFactory(@FullyQualifiedName String methodFactoryName);

    /**
     * Returns current interface name associated with the configuration.
     */
    @Internal
    @Nullable ClassName methodFactory();

    /**
     * Returns a non-null method factory class name.
     */
    @Internal
    default String safeName() {
        return Optional.ofNullable(methodFactory())
                       .map(ClassName::value)
                       .orElse("");
    }
}
