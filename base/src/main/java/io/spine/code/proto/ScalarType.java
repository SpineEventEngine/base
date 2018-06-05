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

import com.google.protobuf.ByteString;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type;

import static java.lang.String.format;

/**
 * Enumeration of the Protobuf scalar value types and corresponding Java types.
 *
 * <p>{@link Type#TYPE_GROUP
 * Type.TYPE_GROUP} are NOT supported, so do not create an associated Java type for it.
 *
 * @author Dmytro Grankin
 * @see <a href="https://developers.google.com/protocol-buffers/docs/proto3#scalar">
 * Protobuf scalar types</a>
 */
public enum ScalarType {
    DOUBLE(Type.TYPE_DOUBLE, double.class),
    FLOAT(Type.TYPE_FLOAT, float.class),
    INT32(Type.TYPE_INT32, int.class),
    INT64(Type.TYPE_INT64, long.class),
    UINT32(Type.TYPE_UINT32, int.class),
    UINT64(Type.TYPE_UINT64, long.class),
    SINT32(Type.TYPE_SINT32, int.class),
    SINT64(Type.TYPE_SINT64, long.class),
    FIXED32(Type.TYPE_FIXED32, int.class),
    FIXED64(Type.TYPE_FIXED64, long.class),
    SFIXED32(Type.TYPE_SFIXED32, int.class),
    SFIXED64(Type.TYPE_SFIXED64, long.class),
    BOOL(Type.TYPE_BOOL, boolean.class),
    STRING(Type.TYPE_STRING, String.class),
    BYTES(Type.TYPE_BYTES, ByteString.class);

    private final Type protoScalarType;
    private final Class<?> javaClass;

    ScalarType(Type protoScalarType, Class<?> javaClass) {
        this.protoScalarType = protoScalarType;
        this.javaClass = javaClass;
    }

    /**
     * Returns the name of the corresponding Java type for the Protobuf scalar type.
     *
     * @param protoScalar the Protobuf scalar type
     * @return the name of the corresponding Java type
     */
    public static String getJavaTypeName(Type protoScalar) {
        for (ScalarType scalarType : ScalarType.values()) {
            if (scalarType.protoScalarType == protoScalar) {
                return scalarType.javaClass.getName();
            }
        }

        final String msg = format("Protobuf type \"%s\" is not a scalar value type.", protoScalar);
        throw new IllegalStateException(msg);
    }

    public Type getProtoScalarType() {
        return protoScalarType;
    }
}
