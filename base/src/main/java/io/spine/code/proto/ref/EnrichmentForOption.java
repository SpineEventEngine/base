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

package io.spine.code.proto.ref;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos.DescriptorProto;

import java.util.Optional;

import static io.spine.option.Options.option;
import static io.spine.option.OptionsProto.enrichmentFor;

/**
 * Parses {@code (enrichment_for)} option value of an enrichment type definition.
 *
 * <p>The option may have one or more reference to a type separated with commas.
 */
public final class EnrichmentForOption {

    /** Splits type references separated with commas. */
    private static final Splitter splitter = Splitter.on(',');

    /** Prevents instantiation of this utility class. */
    private EnrichmentForOption() {
    }

    /**
     * Obtains parses the option, which may have comma-separated values.
     *
     * @return a list with found values, or an empty list if the message does not
     *         have the option defined
     */
    public static ImmutableList<String> parse(DescriptorProto message) {
        Optional<String> value = option(message, enrichmentFor);
        ImmutableList<String> result =
                value.map(s -> ImmutableList.copyOf(splitter.split(s)))
                     .orElse(ImmutableList.of());
        return result;
    }
}
