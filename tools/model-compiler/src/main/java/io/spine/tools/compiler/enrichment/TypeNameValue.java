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

package io.spine.tools.compiler.enrichment;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.MessageOptions;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import io.spine.option.RawListValue;
import io.spine.option.UnknownOptions;
import io.spine.type.TypeName;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A parser for the options, that represent {@linkplain RawListValue raw list} of type names.
 *
 * @author Dmytro Grankin
 */
class TypeNameValue extends RawListValue<MessageOptions, DescriptorProto, TypeName> {

    /**
     * A package prefix to supply unqualified type names.
     */
    private final String packagePrefix;

    TypeNameValue(GeneratedExtension<MessageOptions, String> option, String packagePrefix) {
        super(option);
        this.packagePrefix = checkNotNull(packagePrefix);
    }

    @Override
    protected String get(DescriptorProto descriptor) {
        return UnknownOptions.get(descriptor, getOptionNumber());
    }

    /**
     * Supplies and wraps the specified type name with the package prefix if it is not present.
     *
     * @param rawTypeName the raw type name to normalize
     * @return the normalized {@code TypeName}
     */
    @Override
    protected TypeName asElement(String rawTypeName) {
        final boolean isFqn = rawTypeName.contains(String.valueOf(TypeName.PACKAGE_SEPARATOR));
        final String typeNameValue = isFqn
                                     ? rawTypeName
                                     : packagePrefix + rawTypeName;
        return TypeName.of(typeNameValue);
    }
}
