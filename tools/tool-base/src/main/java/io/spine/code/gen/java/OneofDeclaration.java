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

package io.spine.code.gen.java;

import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Descriptors.OneofDescriptor;
import io.spine.code.java.ClassName;
import io.spine.code.java.SimpleClassName;
import io.spine.code.proto.FieldName;
import io.spine.type.MessageType;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.lang.String.format;

/**
 * A declaration of a {@code oneof} field.
 */
public final class OneofDeclaration {

    private final OneofDescriptor oneof;
    private final MessageType declaringType;

    public OneofDeclaration(OneofDescriptor oneof, MessageType type) {
        this.oneof = checkNotNull(oneof);
        this.declaringType = checkNotNull(type);
    }

    public static ImmutableSet<OneofDeclaration> allFromType(MessageType declaringType) {
        checkNotNull(declaringType);
        ImmutableSet<OneofDeclaration> result =
                declaringType.descriptor()
                             .getOneofs()
                             .stream()
                             .map(oneof -> new OneofDeclaration(oneof, declaringType))
                             .collect(toImmutableSet());
        return result;
    }

    /**
     * Obtains the name of the {@code oneof} field.
     */
    public FieldName name() {
        return FieldName.of(oneof.getName());
    }

    /**
     * Obtains the name of an enum which represents cases of the {@code oneof} field.
     *
     * <p>Such an enum should be nested in the declaring message class.
     *
     * <p>If the declaring message class name is {@code com.acme.cms.Customer} and the {@code oneof}
     * name is {@code auth_provider}, the resulting class name would be
     * {@code com.acme.cms.Customer$AuthProviderCase}.
     *
     * @return the case enum FQN
     */
    public ClassName javaCaseEnum() {
        ClassName declaringClassName = declaringType.javaClassName();
        io.spine.code.gen.java.FieldName oneofName =
                io.spine.code.gen.java.FieldName.from(name());
        SimpleClassName enumName = SimpleClassName.create(format("%sCase", oneofName.capitalize()));
        return declaringClassName.withNested(enumName);
    }
}
