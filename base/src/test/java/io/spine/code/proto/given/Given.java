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

package io.spine.code.proto.given;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.test.code.proto.FieldContainer;

import java.util.List;

public final class Given {

    /** Prevents instantiation of this utility class. */
    private Given() {
    }

    public static FieldDescriptor singularField() {
        return fieldWithIndex(0);
    }

    public static FieldDescriptor repeatedField() {
        return fieldWithIndex(1);
    }

    public static FieldDescriptor mapField() {
        return fieldWithIndex(2);
    }

    public static FieldDescriptor primitiveField() {
        return fieldWithIndex(3);
    }

    public static FieldDescriptor messageField() {
        return fieldWithIndex(4);
    }

    public static FieldDescriptor enumField() {
        return fieldWithIndex(5);
    }

    private static FieldDescriptor fieldWithIndex(int index) {
        Descriptor fieldContainer = FieldContainer.getDescriptor();
        List<FieldDescriptor> fields = fieldContainer.getFields();
        FieldDescriptor field = fields.get(index);
        return field;
    }
}
