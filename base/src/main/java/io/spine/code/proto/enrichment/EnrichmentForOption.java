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

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.MessageOptions;
import com.google.protobuf.Descriptors.Descriptor;
import io.spine.code.proto.PackageName;
import io.spine.code.proto.StringOption;
import io.spine.code.proto.ref.TypeRef;
import io.spine.option.OptionsProto;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static io.spine.util.Exceptions.newIllegalArgumentException;

/**
 * Parses {@code (enrichment_for)} option value of an enrichment type definition.
 *
 * <p>The option may have one or more reference to a type separated with commas.
 */
public final class EnrichmentForOption extends StringOption<Collection<TypeRef>,
        Descriptor, MessageOptions> {

    /** Splits type references separated with commas. */
    private static final Splitter splitter = Splitter.on(',')
                                                     .trimResults();

    /** Prevents instantiation of this utility class. */
    public EnrichmentForOption() {
        super(OptionsProto.enrichmentFor);
    }

    /**
     * Obtains parses the option, which may have comma-separated values.
     *
     * @return a list with found values, or an empty list if the message does not
     *         have the option defined
     */
    public static ImmutableList<String> parse(DescriptorProto message) {
        EnrichmentForOption enrichmentFor = new EnrichmentForOption();
        Optional<String> value = enrichmentFor.valueFrom(message);
        ImmutableList<String> result =
                value.map(s -> ImmutableList.copyOf(splitter.split(s)))
                     .orElse(ImmutableList.of());
        return result;
    }

    private Optional<String> valueFrom(DescriptorProto object) {
        MessageOptions options = object.getOptions();
        return options.hasExtension(extension())
               ? Optional.of(options.getExtension(extension()))
               : Optional.empty();
    }

    @Override
    protected MessageOptions optionsFrom(Descriptor object) {
        return object.getOptions();
    }

    /**
     * {@inheritDoc}
     *
     * Obtains type references to the enrichable messages.
     */
    @Override
    protected Collection<TypeRef> parsedValueFrom(Descriptor type) {
        String rawString = valueFrom(type).orElseThrow(
                () -> newIllegalArgumentException(
                        "Cannot create an enrichment type for `%s` which does not have the" +
                                " `(enrichment_for)` option.", type.getFullName()));
        List<String> rawTypeRefs = splitter.splitToList(rawString);
        PackageName thisPackage = PackageName.of(type);
        ImmutableList<TypeRef> result = rawTypeRefs.stream()
                                                   .map(TypeRef::parse)
                                                   .map(ref -> ref.ensurePackage(thisPackage))
                                                   .collect(toImmutableList());
        return result;
    }

    /**
     * Obtains the {@code enrichment_for} option value from the specified message.
     *
     * @param message
     *         message that bears the {@code enrichment_for} option.
     * @return parsed type references, contained in the {@code enrichment_for} option value.
     * @apiNote this method is just a shorthand for
     *         <pre>
     *             {@code
     *             EnrichmentForOption option = new EnrichmentForOption();
     *             option.parsedValueFrom(messageDescriptor);
     *             }
     *         </pre>
     *         to avoid creating an addition instance.
     */
    static Collection<TypeRef> typeRefsFrom(Descriptor message) {
        EnrichmentForOption option = new EnrichmentForOption();
        return option.parsedValueFrom(message);
    }
}
