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
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Descriptors.Descriptor;
import io.spine.code.proto.MessageType;
import io.spine.code.proto.ref.TypeRef;
import io.spine.type.KnownTypes;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

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
    private final ImmutableList<FieldDef> fields;

    private EnrichmentType(Descriptor type) {
        super(type);
        this.sourceTypes = sourceTypesOf(type);
        this.fields = fieldDefinitionsOf(type);
    }

    /**
     * Obtains an enrichment type for the passed name.
     */
    public static EnrichmentType from(MessageType type) {
        checkNotNull(type);
        Descriptor descriptor = type.descriptor();
        EnrichmentType result = new EnrichmentType(descriptor);
        return result;
    }

    /**
     * Obtains type references to the enrichable messages.
     */
    private static ImmutableList<TypeRef> sourceTypesOf(Descriptor type) {
        List<String> sourceRefs = EnrichmentForOption.parse(type.toProto());
        checkArgument(
                !sourceRefs.isEmpty(),
                "Cannot create an enrichment type for `%s` which does not have the" +
                        " `(enrichment_for)` option.", type.getFullName()
        );
        ImmutableList<TypeRef> result =
                sourceRefs.stream()
                          .map(TypeRef::parse)
                          .collect(toImmutableList());
        return result;
    }

    /**
     * Obtains sources for creating fields.
     */
    private static ImmutableList<FieldDef> fieldDefinitionsOf(Descriptor type) {
        ImmutableList<FieldDef> result =
                type.getFields()
                    .stream()
                    .map(FieldDef::new)
                    .collect(toImmutableList());
        return result;
    }

    /**
     * Verifies if the passed type is the source for this enrichment type.
     */
    private boolean isSource(Descriptor message) {
        if (sourceTypes.stream()
                       .noneMatch(r -> r.test(message))) {
            return false;
        }
        boolean result = fields.stream()
                               .anyMatch(s -> s.matchesType(message));
        return result;
    }

    /**
     * Obtains all known message types for which this type can serve as an enrichment.
     */
    public ImmutableSet<MessageType> knownSources() {
        ImmutableSet<MessageType> result =
                KnownTypes.instance()
                          .asTypeSet()
                          .messageTypes()
                          .stream()
                          .filter(m -> isSource(m.descriptor()))
                          .collect(toImmutableSet());
        return result;
    }
}
