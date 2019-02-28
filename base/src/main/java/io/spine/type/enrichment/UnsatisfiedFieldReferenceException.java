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

package io.spine.type.enrichment;

import com.google.common.collect.ImmutableList;
import io.spine.string.Diags;
import io.spine.type.MessageType;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static io.spine.string.Diags.join;
import static io.spine.type.MessageType.fullNameComparator;
import static java.lang.String.format;

/**
 * Thrown if a field reference does not match any of the referenced types.
 */
final class UnsatisfiedFieldReferenceException extends IllegalStateException {

    private static final long serialVersionUID = 0L;

    UnsatisfiedFieldReferenceException(EnrichmentType enrichmentType,
                                       List<FieldRef> fieldReferences,
                                       String typeReference,
                                       Collection<MessageType> resolvedTypes) {
        super(createMessage(enrichmentType, fieldReferences, typeReference, resolvedTypes));
    }

    private static String createMessage(EnrichmentType enrichmentType,
                                        List<FieldRef> fieldReferences,
                                        String typeReference,
                                        Collection<MessageType> resolvedTypes) {
        String prefix = fieldReferences.size() == 1
            ? "The field reference %s declared in the enrichment type `%s` does not match"
            : "Field references (%s) declared in the enrichment type `%s` do not match";

        String fmt = prefix + " any of the types referenced via `%s`." +
                " The type reference resolved to: %s.";
        String result = format(
                fmt,
                fmtFieldRefs(fieldReferences),
                enrichmentType,
                typeReference,
                fmtTypes(resolvedTypes)
        );
        return result;
    }

    private static String fmtFieldRefs(List<FieldRef> fieldReferences) {
        ImmutableList<String> fieldRefs =
                fieldReferences.stream()
                               .map(Diags::backtick)
                               .collect(toImmutableList());
        return join(fieldRefs);
    }

    private static String fmtTypes(Collection<MessageType> resolvedTypes) {
        ImmutableList<String> backticked =
                resolvedTypes.stream()
                             .sorted(fullNameComparator())
                             .map(Diags::backtick)
                             .collect(toImmutableList());
        return join(backticked);
    }

}
