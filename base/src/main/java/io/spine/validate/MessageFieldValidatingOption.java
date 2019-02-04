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

package io.spine.validate;

import com.google.protobuf.DescriptorProtos.FieldOptions;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import com.google.protobuf.Message;
import io.spine.code.proto.FieldDeclaration;

/**
 * An option that validates a message field.
 *
 * @param <T>
 *         type of option value
 * @param <M>
 *         type of the message being validated by this option
 */
abstract class MessageFieldValidatingOption<T, M extends Message>
        extends FieldValidatingOption<T, M> {

    MessageFieldValidatingOption(GeneratedExtension<FieldOptions, T> extension) {
        super(extension);
    }

    @Override
    boolean shouldValidate(FieldDescriptor value) {
        FieldDeclaration declaration = new FieldDeclaration(value);
        Valid<M> validOption = new Valid<>();
        Boolean valid = validOption.valueFrom(value)
                                   .orElse(false);
        boolean shouldValidateCollection = declaration.isNotCollection() || valid;
        return super.shouldValidate(value) && shouldValidateCollection;
    }
}
