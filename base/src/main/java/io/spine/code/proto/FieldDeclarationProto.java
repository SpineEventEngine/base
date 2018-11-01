/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.code.proto;

import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;

import java.util.Optional;

/**
 * A declaration of a message {@linkplain FieldDescriptorProto field}.
 */
public class FieldDeclarationProto {

    private final FieldDescriptorProto descriptor;
    private final AbstractMessageDeclaration originMessage;

    /**
     * Creates a new instance.
     *
     * @param descriptor
     *         the descriptor of the field
     * @param originMessage
     *         the message containing the field
     */
    public FieldDeclarationProto(FieldDescriptorProto descriptor,
                                 AbstractMessageDeclaration originMessage) {
        this.descriptor = descriptor;
        this.originMessage = originMessage;
    }

    /** Returns the name of the field. */
    public FieldName name() {
        return FieldName.of(descriptor);
    }

    public FieldDescriptorProto descriptor() {
        return descriptor;
    }

    /**
     * Obtains comments going before the field.
     *
     * @return the leading field comments or {@code Optional.empty()} if there are no comments
     */
    public Optional<String> leadingComments() {
        return originMessage.documentation()
                            .getFieldLeadingComments(descriptor);
    }
}
