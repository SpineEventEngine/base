/*
 * Copyright 2019, TeamDev. All rights reserved.
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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import io.spine.code.proto.ref.DirectTypeRef;
import io.spine.code.proto.ref.EnrichmentForOption;
import io.spine.code.proto.ref.EnrichmentOption;
import io.spine.code.proto.ref.TypeRef;
import io.spine.option.OptionsProto;

import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Comparator.naturalOrder;

/**
 * Helper class for obtaining type references from message options.
 */
final class TypeRefs {

    /**
     * If {@code true} the parses would analyze {@code (enrichment_for)},
     * otherwise the {@code (enrichment)} option. Please not that the latter is deprecated
     * and this field and the related code should be eliminated.
     */
    private final boolean forOption;

    /**
     * The prefix to be added to a reference if it's not a fully-qualified one.
     */
    private final String packagePrefix;

    /**
     * Obtains the parser for the {@link OptionsProto#enrichmentFor} option values.
     */
    static TypeRefs enrichmentForOption(String packagePrefix) {
        return new TypeRefs(true, packagePrefix);
    }

    /**
     * Obtains the instance for the {@link OptionsProto#enrichment} option values.
     */
    static TypeRefs enrichmentOption(String packagePrefix) {
        return new TypeRefs(false, packagePrefix);
    }

    private TypeRefs(boolean forOption, String packagePrefix) {
        this.forOption = forOption;
        this.packagePrefix = packagePrefix;
    }

    /**
     * Parses the the given message descriptor and it into separate type references.
     *
     * <p>If a type name is not fully-qualified, the {@code packagePrefix} is added to it.
     *
     * @param descriptor the descriptor to parse
     * @return the list of parsed type references or an empty list if the option is absent or empty
     */
    ImmutableList<String> parse(DescriptorProto descriptor) {
        List<String> parts = forOption
                             ? EnrichmentForOption.parse(descriptor)
                             : EnrichmentOption.parse(descriptor);
        ImmutableList<String> result =
                parts  .stream()
                       .map(this::toQualified)
                       .sorted(naturalOrder())
                       .collect(toImmutableList());
        return result;
    }

    @VisibleForTesting
    String toQualified(String value) {
        TypeRef ref = TypeRef.parse(value);
        if (ref instanceof DirectTypeRef) {
            DirectTypeRef directRef = (DirectTypeRef) ref;
            if (directRef.packageName()
                         .isPresent()) {
                return value;
            }
            return packagePrefix + directRef.nestedTypeName();
        }
        return value;
    }
}
