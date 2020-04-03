/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.type;

import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.Descriptors.OneofDescriptor;
import io.spine.code.proto.FieldName;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A declaration of a {@code oneof} field.
 */
@Immutable
public final class OneofDeclaration {

    private final OneofDescriptor oneof;
    private final MessageType declaringType;

    public OneofDeclaration(OneofDescriptor oneof, MessageType type) {
        this.oneof = checkNotNull(oneof);
        this.declaringType = checkNotNull(type);
    }

    /**
     * Obtains the name of the {@code oneof} field.
     */
    public FieldName name() {
        return FieldName.of(oneof.getName());
    }

    /**
     * Obtains the {@code oneof} descriptor.
     */
    public OneofDescriptor descriptor() {
        return oneof;
    }

    public MessageType declaringType() {
        return declaringType;
    }
}
