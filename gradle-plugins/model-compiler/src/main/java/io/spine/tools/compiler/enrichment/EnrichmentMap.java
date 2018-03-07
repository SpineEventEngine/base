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

package io.spine.tools.compiler.enrichment;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

import static io.spine.option.RawListParser.getValueSeparator;

/**
 * @author Alexander Yevsyukov
 */
class EnrichmentMap {

    private static final String EMPTY_TYPE_NAME = "";

    private EnrichmentMap() {
    }

    /**
     * Merge duplicate values into a single value for the same key.
     *
     * <p>The values are joined with the
     * {@linkplain io.spine.option.RawListParser#VALUE_SEPARATOR value separator}.
     *
     * <p>Merging may be required when the wildcard {@code by} option values are handled,
     * i.e. when processing a single enrichment type as a map key, but multiple target
     * event types as values.
     */
    static Map<String, String> mergeDuplicateValues(HashMultimap<String, String> source) {
        log().debug("Merging duplicate properties in enrichments.proto");
        final ImmutableMap.Builder<String, String> mergedResult = ImmutableMap.builder();
        for (String key : source.keySet()) {
            final Set<String> valuesPerKey = source.get(key);
            // Empty type name might be present in the values
            // If so, remove it from the set
            valuesPerKey.remove(EMPTY_TYPE_NAME);

            final String mergedValue;
            if (valuesPerKey.size() > 1) {
                mergedValue = Joiner.on(getValueSeparator())
                                    .join(valuesPerKey);
            } else {
                mergedValue = valuesPerKey.iterator()
                                          .next();
            }
            mergedResult.put(key, mergedValue);
        }

        return mergedResult.build();
    }

    private enum LogSingleton {
        INSTANCE;

        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(EnrichmentMap.class);
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }
}
