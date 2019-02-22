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
import io.spine.tools.protoc.UuidInterface;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.signature.qual.FullyQualifiedName;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An {@link GeneratedInterfaceConfig interface} configuration {@link UuidMessage uuid} message
 * selector.
 */
public final class InterfaceUuidMessage implements UuidMessage<UuidInterface>, GeneratedInterfaceConfig {

    private @Nullable ClassName interfaceName;

    /** Prevents direct instantiation. **/
    InterfaceUuidMessage() {
    }

    /**
     * Sets current target class to a supplied value.
     */
    @Override
    public void markWith(@FullyQualifiedName String targetName) {
        checkNotNull(targetName);
        this.interfaceName = ClassName.of(targetName);
    }

    @Override
    public void ignore() {
        this.interfaceName = null;
    }

    @Internal
    @Override
    public @Nullable ClassName interfaceName() {
        return interfaceName;
    }

    @Internal
    @Override
    public UuidInterface toProto() {
        return UuidInterface
                .newBuilder()
                .setInterfaceName(safeName())
                .build();
    }
}
