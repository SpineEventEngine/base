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

package io.spine.tools.protojs.field.parser.primitive.given;

import com.google.protobuf.Descriptors.FieldDescriptor.Type;

import static io.spine.tools.protojs.given.Given.bytesField;
import static io.spine.tools.protojs.given.Given.floatField;
import static io.spine.tools.protojs.given.Given.int32Field;
import static io.spine.tools.protojs.given.Given.int64Field;

/**
 * The test env for tests related to {@code PrimitiveParser} implementations.
 *
 * @author Dmytro Kuzmin
 */
public final class PrimitiveParserTestEnv {

    /** Prevents instantiation of this utility class. */
    private PrimitiveParserTestEnv() {
    }

    public static Type int32Type() {
        return int32Field().getType();
    }

    public static Type int64Type() {
        return int64Field().getType();
    }

    public static Type floatType() {
        return floatField().getType();
    }

    public static Type bytesType() {
        return bytesField().getType();
    }
}
