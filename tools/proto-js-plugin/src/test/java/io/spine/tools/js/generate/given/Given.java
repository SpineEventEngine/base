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

package io.spine.tools.js.generate.given;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.NullValue;
import com.google.protobuf.StringValue;
import io.spine.tools.code.proto.TypeSet;
import io.spine.type.Type;
import io.spine.type.TypeName;
import spine.test.js.Fields.FieldContainer;

import static io.spine.util.Exceptions.newIllegalStateException;

public final class Given {

    /** Prevents instantiation of this utility class. */
    private Given() {
    }

    public static FileDescriptor file() {
        FileDescriptor file = message().getFile();
        return file;
    }

    public static Descriptor message() {
        Descriptor message = FieldContainer.getDescriptor();
        return message;
    }

    public static Type typeFor(Descriptors.GenericDescriptor descriptor) {
        TypeName typeName = TypeName.of(descriptor.getFullName());
        FileDescriptor file = descriptor.getFile();
        TypeSet typeSet = TypeSet.from(file);
        return typeSet
                .find(typeName)
                .orElseThrow(() -> newIllegalStateException("Cannot find Type %s.", typeName));
    }

    public static Type messageType() {
        Descriptor descriptor = StringValue.getDescriptor();
        return typeFor(descriptor);
    }

    public static Type enumType() {
        Descriptors.EnumDescriptor descriptor = NullValue.getDescriptor();
        return typeFor(descriptor);
    }
}
