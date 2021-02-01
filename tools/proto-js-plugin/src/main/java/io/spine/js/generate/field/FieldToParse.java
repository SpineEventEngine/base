/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.js.generate.field;

import com.google.protobuf.Descriptors.FieldDescriptor;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A Protobuf field, which is going to be parsed from a plain JavaScript
 * {@linkplain #objectVariable object} to a Protobuf-specific {@linkplain #messageVariable type}.
 *
 * <p>The class contains the data required to generate code for parsing of a field,
 * such as a field descriptor, names of variables involved in code for parsing.
 */
public final class FieldToParse {

    private final String objectVariable;
    private final String messageVariable;
    private final FieldDescriptor descriptor;

    /**
     * Creates a new instance.
     *
     * @param descriptor
     *         the descriptor of the field to be parsed
     * @param objectVariable
     *         the name of the object to get the field value to be parsed
     * @param messageVariable
     *         the name of the message to set the parsed field
     */
    public FieldToParse(FieldDescriptor descriptor, String objectVariable, String messageVariable) {
        checkNotNull(descriptor);
        checkNotNull(objectVariable);
        checkNotNull(messageVariable);
        this.descriptor = descriptor;
        this.objectVariable = objectVariable;
        this.messageVariable = messageVariable;
    }

    /**
     * Obtains the descriptor of the field.
     */
    public FieldDescriptor descriptor() {
        return descriptor;
    }

    /**
     * Obtains the name of a variable to set the field at.
     */
    public String messageVariable() {
        return messageVariable;
    }

    /**
     * Obtains the reference to the field value on the source object.
     */
    public String value() {
        String fieldJsonName = descriptor.getJsonName();
        String jsObject = objectVariable + '.' + fieldJsonName;
        return jsObject;
    }
}
