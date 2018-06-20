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

import com.google.common.base.Objects;
import com.google.protobuf.Descriptors.GenericDescriptor;
import com.google.protobuf.Message;
import io.spine.type.ClassName;
import io.spine.type.TypeName;
import io.spine.type.TypeUrl;
import io.spine.type.UnknownTypeException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A Protobuf type.
 *
 * @param <T> the type of the type descriptor
 * @param <P> the type of the proto message of the descriptor
 * @author Alexander Yevsyukov
 */
public abstract class Type<T extends GenericDescriptor, P extends Message> {

    private final T descriptor;
    private final P proto;
    private final ClassName className;
    private final TypeUrl url;

    protected Type(T descriptor, P descriptorProto, ClassName javaClassName, TypeUrl url) {
        this.descriptor = checkNotNull(descriptor);
        this.proto = checkNotNull(descriptorProto);
        this.url = url;
        this.className = javaClassName;
    }

    /**
     * Obtains the descriptor of the type.
     */
    public T descriptor() {
        return this.descriptor;
    }

    /**
     * Obtains the proto message of the type descriptor.
     */
    public P toProto() {
        return this.proto;
    }

    /**
     * Obtains the {@linkplain TypeName name} of this type.
     */
    public TypeName name() {
        return url.toName();
    }

    /**
     * Obtains the {@link TypeUrl} of this type.
     */
    public TypeUrl url() {
        return url;
    }

    /**
     * Loads the Java class representing this Protobuf type.
     */
    public Class<?> javaClass() {
        try {
            return Class.forName(className.value());
        } catch (ClassNotFoundException e) {
            throw new UnknownTypeException(className.value(), e);
        }
    }

    /**
     * Obtains the name of the Java class representing this Protobuf type.
     */
    public ClassName javaClassName() {
        return className;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Type<?, ?> type = (Type<?, ?>) o;
        return Objects.equal(proto, type.proto);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(proto);
    }
}
