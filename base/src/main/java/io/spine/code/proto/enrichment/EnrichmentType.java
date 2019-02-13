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

package io.spine.code.proto.enrichment;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Descriptors.Descriptor;
import io.spine.code.proto.MessageType;
import io.spine.code.proto.ref.TypeRef;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;

/**
 * An enrichment type is a message which is added to a context of another message
 * to extends its information for the benefit of message handlers.
 *
 * <p>An enrichment message declaration has:
 * <ol>
 *   <li>The {@link io.spine.option.OptionsProto#enrichmentFor
 *       (enrichment_for)} option with a {@linkplain TypeRef reference} to one or more message types
 *       this message enriches.
 *   <li>One or more fields with the {@link io.spine.option.OptionsProto#by (by)} option with
 *       the value {@linkplain FieldRef referencing} one ore more fields of source message types.
 * </ol>
 *
 * <p>If one of the above is missing, the enrichment declaration is not valid.
 */
public final class EnrichmentType extends MessageType {

    private final ImmutableList<TypeRef> sourceTypes;

    private EnrichmentType(Descriptor descriptor) {
        super(descriptor);
        this.sourceTypes = sourceTypesOf(descriptor);
    }

    /**
     * Obtains type references from the passed descriptor.
     */
    private static ImmutableList<TypeRef> sourceTypesOf(Descriptor descriptor) {
        List<String> sourceRefs = EnrichmentForOption.parse(descriptor.toProto());
        checkArgument(
                !sourceRefs.isEmpty(),
                "Cannot create an enrichment type for `%s` which does not have the" +
                        " `(enrichment_for)` option.", descriptor.getFullName()
        );
        ImmutableList<TypeRef> result =
                sourceRefs.stream()
                          .map(TypeRef::parse)
                          .collect(toImmutableList());
        return result;
    }

    /**
     * Verifies if the passed type is the source for this enrichment type.
     */
    public boolean isSource(Descriptor message) {
        boolean result = sourceTypes.stream()
                                    .anyMatch(r -> r.test(message));
        //TODO:2019-02-13:alexander.yevsyukov: This also should take into account field references.
        return result;
    }
}
