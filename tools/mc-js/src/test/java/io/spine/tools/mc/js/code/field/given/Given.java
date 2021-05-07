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

package io.spine.tools.mc.js.code.field.given;

import com.google.protobuf.Descriptors;

import static io.spine.tools.mc.js.code.field.given.FieldContainerEntry.ENUM_FIELD;
import static io.spine.tools.mc.js.code.field.given.FieldContainerEntry.MAP_FIELD;
import static io.spine.tools.mc.js.code.field.given.FieldContainerEntry.MESSAGE_FIELD;
import static io.spine.tools.mc.js.code.field.given.FieldContainerEntry.PRIMITIVE_FIELD;
import static io.spine.tools.mc.js.code.field.given.FieldContainerEntry.REPEATED_FIELD;
import static io.spine.tools.mc.js.code.field.given.FieldContainerEntry.TIMESTAMP_FIELD;

public class Given {

    /** Prevents instantiation of this utility class. */
    private Given() {
    }

    public static Descriptors.FieldDescriptor primitiveField() {
        return field(PRIMITIVE_FIELD);
    }

    public static Descriptors.FieldDescriptor enumField() {
        return field(ENUM_FIELD);
    }

    public static Descriptors.FieldDescriptor messageField() {
        return field(MESSAGE_FIELD);
    }

    public static Descriptors.FieldDescriptor timestampField() {
        return field(TIMESTAMP_FIELD);
    }

    public static Descriptors.FieldDescriptor singularField() {
        return field(MESSAGE_FIELD);
    }

    public static Descriptors.FieldDescriptor repeatedField() {
        return field(REPEATED_FIELD);
    }

    public static Descriptors.FieldDescriptor mapField() {
        return field(MAP_FIELD);
    }

    private static Descriptors.FieldDescriptor field(FieldContainerEntry entry) {
        String fieldName = entry.protoName();
        Descriptors.FieldDescriptor field = io.spine.tools.mc.js.code.given.Given.message().findFieldByName(fieldName);
        return field;
    }
}
