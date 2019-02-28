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
import com.google.protobuf.Descriptors;
import io.spine.type.KnownTypes;
import io.spine.type.MessageType;

import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

/**
 * Validates an enrichment type providing diagnostics on errors in declarations if
 * the type is invalid.
 */
final class Inspector {

    private final EnrichmentType type;

    Inspector(EnrichmentType type) {
        this.type = type;
    }

    /**
     * Verifies the declaration of the enrichment type.
     *
     * @throws NoMatchingTypeException
     *          if there are no types matching the type reference found in
     *          the {@code (enrichment_for)} of the enrichment type declaration
     */
    void check() {
        Set<MessageType> resolvedTypes = checkSourceTypeRef();
        checkFieldReferences(resolvedTypes);
    }

    /**
     * None of the types matches either the type reference <em>or</em> the field references,
     * as checked by {@link EnrichmentType#isSource(Descriptors.Descriptor)},
     * which filters the types by the combination.
     *
     * <p>In order to report correct diagnostics this method verifies matching types. If there
     * is at least one matching type, it is returned as the result of this method.
     * Otherwise, {@link IllegalStateException} is thrown with corresponding error message.
     */
    private Set<MessageType> checkSourceTypeRef() {
        Set<MessageType> matchingTypes =
                KnownTypes.instance()
                          .asTypeSet()
                          .messageTypes()
                          .stream()
                          .filter(m -> type.matchesSourceRef(m.descriptor()))
                          .collect(toImmutableSet());

        if (matchingTypes.isEmpty()) {
            throw new NoMatchingTypeException(typeRef());
        }
        return matchingTypes;
    }

    private void checkFieldReferences(Set<MessageType> types) {
        for (FieldDef field : type.fieldDefs()) {
            ImmutableList<FieldRef> unresolved = field.findUnresolved(types);
            if (!unresolved.isEmpty()) {
                throw new UnsatisfiedFieldReferenceException(type, unresolved, typeRef(), types);
            }
        }
    }

    private String typeRef() {
        return EnrichmentForOption.value(type.descriptor());
    }
}
