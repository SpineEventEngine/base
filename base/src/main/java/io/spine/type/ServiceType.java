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

package io.spine.type;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;
import io.spine.annotation.Internal;
import io.spine.code.java.ClassName;
import io.spine.code.proto.TypeSet;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A Protobuf service type as declared in a proto file.
 */
@Internal
public final class ServiceType extends Type<ServiceDescriptor, ServiceDescriptorProto> {

    private ServiceType(ServiceDescriptor descriptor) {
        super(descriptor, false);
    }

    /**
     * Creates a new instance of {@code ServiceType} from the given service descriptor.
     *
     * @param descriptor
     *         the service descriptor
     * @return new instance of {@code ServiceType}
     */
    public static ServiceType of(ServiceDescriptor descriptor) {
        checkNotNull(descriptor);
        return new ServiceType(descriptor);
    }

    /**
     * Collects all service types declared in the passed file.
     */
    public static TypeSet allFrom(FileDescriptor file) {
        checkNotNull(file);
        TypeSet.Builder result = TypeSet.newBuilder();
        for (ServiceDescriptor type : file.getServices()) {
            result.add(of(type));
        }
        return result.build();
    }

    @Override
    public ServiceDescriptorProto toProto() {
        return descriptor().toProto();
    }

    @Override
    public TypeUrl url() {
        return TypeUrl.from(descriptor());
    }

    @Override
    public ClassName javaClassName() {
        return ClassName.from(descriptor());
    }

    @Override
    public Optional<Type<Descriptor, DescriptorProto>> containingType() {
        // Services are not allowed to be nested in Protobuf.
        return Optional.empty();
    }
}
