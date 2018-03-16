/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

package io.spine.tools.java;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.EnumDescriptorProto;
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto;
import io.spine.tools.AbstractFileName;

import static io.spine.tools.util.CodePreconditions.checkNotEmptyOrBlank;

/**
 * A Java file name.
 *
 * @author Alexander Yevsyukov
 */
public final class FileName extends AbstractFileName<FileName> {

    private static final String GRPC_CLASSNAME_SUFFIX = "Grpc";
    private static final String EXTENSION = ".java";

    private FileName(String value) {
        super(value);
    }

    /**
     * Obtains the name for the passed type.
     */
    public static FileName forType(String typeName) {
        checkNotEmptyOrBlank(typeName);
        final FileName result = new FileName(typeName + EXTENSION);
        return result;
    }

    /**
     * Obtains file name for the specified message.
     *
     * @param message
     *        a descriptor of the message
     * @param orBuilder
     *        if {@code true} the file would represent a descendant of
     *        {@link com.google.protobuf.MessageOrBuilder MessageOrBuilder}
     * @return new instance
     */
    public static FileName forMessage(DescriptorProto message, boolean orBuilder) {
        final String typeName = message.getName();
        final String javaType = orBuilder
                ? SimpleClassName.messageOrBuilder(typeName).value()
                : typeName;
        final FileName result = forType(javaType);
        return result;
    }

    /**
     * Obtains file name for the passed enum.
     */
    public static FileName forEnum(EnumDescriptorProto enumType) {
        return forType(enumType.getName());
    }

    /**
     * Obtains file name for the specified service.
     */
    public static FileName forService(ServiceDescriptorProto service) {
        return forType(service.getName() + GRPC_CLASSNAME_SUFFIX);
    }
}
