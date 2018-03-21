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

package io.spine.tools.proto;

import com.google.common.base.Splitter;
import com.google.protobuf.Descriptors.Descriptor;
import io.spine.value.StringTypeValue;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A fully-qualified Protobuf type name.
 *
 * @author Alexander Yevsyukov
 */
public class TypeName extends StringTypeValue {

    /**
     * The separator character for package names in a fully qualified proto type name.
     */
    static final char PACKAGE_SEPARATOR = '.';

    /**
     * The character to separate a nested type from the outer type name.
     */
    static final char NESTED_TYPE_SEPARATOR = '.';

    /**
     * The splitter to separate package and type names in fully-qualified type names.
     */
    private static final Splitter packageSplitter = Splitter.on(PACKAGE_SEPARATOR);

    private TypeName(String value) {
        super(value);
    }

    private static TypeName create(String value) {
        return new TypeName(value);
    }

    /**
     * Creates new instance by the passed type name value.
     */
    public static TypeName of(String typeName) {
        checkNotNull(typeName);
        checkArgument(!typeName.isEmpty());
        return create(typeName);
    }

    /**
     * Obtains type name for the message type by its descriptor.
     */
    public static TypeName from(Descriptor descriptor) {
        checkNotNull(descriptor);
        return of(descriptor.getFullName());
    }

    /**
     * Returns the unqualified name of the Protobuf type, for example: {@code StringValue}.
     */
    public String getSimpleName() {
        final String typeName = value();
        final List<String> tokens = packageSplitter.splitToList(typeName);
        final String result = tokens.get(tokens.size() - 1);
        return result;
    }
}
