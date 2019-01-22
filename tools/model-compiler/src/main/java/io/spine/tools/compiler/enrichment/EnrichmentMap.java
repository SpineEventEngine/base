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

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import io.spine.logging.Logging;
import io.spine.type.TypeName;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static io.spine.option.OptionsProto.enrichment;
import static io.spine.option.OptionsProto.enrichmentFor;

/**
 * Composes enrichment map for multiple message declarations.
 */
final class EnrichmentMap implements Logging {

    private static final String EMPTY_TYPE_NAME = "";

    /** Joins several values into one delimited string. */
    private static final Joiner joiner = Joiner.on(',');

    private final String packagePrefix;
    private final TypeNameParser eventType;
    private final TypeNameParser enrichmentType;

    EnrichmentMap(String packagePrefix) {
        this.packagePrefix = packagePrefix;
        this.eventType = new TypeNameParser(enrichmentFor, packagePrefix);
        this.enrichmentType = new TypeNameParser(enrichment, packagePrefix);
    }

    /**
     * Obtains enrichment information found in the passed messages.
     *
     * <p>A key in the returned map is a type name of an enrichment.
     *
     * <p>A value, is a string with one or more event types that are enriched with the
     * type from the key.
     */
    Map<String, String> allOf(Iterable<DescriptorProto> messages) {
        HashMultimap<String, String> multimap = HashMultimap.create();
        for (DescriptorProto msg : messages) {
            handleMessage(multimap, msg);
        }
        _debug("Found enrichments: {}", multimap);
        Map<String, String> merged = merge(multimap);
        return merged;
    }

    /**
     * Transforms the passed multimap with possible several entries per key, into
     * a map where several values from the passed multimap are joined into
     * a single value.
     *
     * <p>Merging may be required when the wildcard {@code by} option values are handled,
     * i.e. when processing a single enrichment type as a map key, but multiple target
     * event types as values.
     */
    private Map<String, String> merge(HashMultimap<String, String> source) {
        _debug("Merging duplicating entries");
        ImmutableMap.Builder<String, String> mergedResult = ImmutableMap.builder();
        for (String key : source.keySet()) {
            Set<String> valuesPerKey = source.get(key);
            /* Empty type name might be present in the values.
               If so, remove it from the set */
            valuesPerKey.remove(EMPTY_TYPE_NAME);

            String mergedValue;
            if (valuesPerKey.size() > 1) {
                mergedValue = joiner.join(valuesPerKey);
            } else {
                mergedValue = valuesPerKey.iterator()
                                          .next();
            }
            mergedResult.put(key, mergedValue);
        }

        return mergedResult.build();
    }

    private static void put(Map.Entry<String, String> entry, Multimap<String, String> targetMap) {
        // Put key and value separately to avoid an error.
        targetMap.put(entry.getKey(), entry.getValue());
    }

    @SuppressWarnings("MethodWithMultipleLoops")  // It's fine as we don't expect too many items.
    private void handleMessage(Multimap<String, String> targetMap, DescriptorProto msg) {
        Map<String, String> entries = scanMsg(msg);
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            put(entry, targetMap);
        }
        if (!entries.isEmpty()) {
            return;
        }
        Map<String, String> entryFromField = scanFields(msg);
        if (entryFromField.size() > 0) {
            for (Map.Entry<String, String> entry : entryFromField.entrySet()) {
                put(entry, targetMap);
            }
            return;
        }
        Map.Entry<String, String> entryFromInnerMsg = scanInnerMessages(msg);
        if (entryFromInnerMsg != null) {
            put(entryFromInnerMsg, targetMap);
            _debug("Found enrichment: {} -> {}",
                   entryFromInnerMsg.getKey(),
                   entryFromInnerMsg.getValue());
        } else {
            _debug("No enrichment or event annotations found for message {}", msg.getName());
        }
    }

    private Map<String, String> scanMsg(DescriptorProto msg) {
        ImmutableMap.Builder<String, String> result = ImmutableMap.builder();
        String messageName = packagePrefix + msg.getName();

        // Treating current {@code msg} as an enrichment object.
        _debug("Scanning message {} for the enrichment annotations", messageName);
        Collection<TypeName> eventTypes = eventType.parse(msg);
        if (!eventTypes.isEmpty()) {
            String mergedValue = joiner.join(eventTypes);
            _debug("Found target events: {}", mergedValue);
            result.put(messageName, mergedValue);
        } else {
            _debug("No target events found");
        }

        // Treating current {@code msg} as a target for enrichment (e.g. Spine event).
        _debug("Scanning message {} for the enrichment target annotations", messageName);
        Collection<TypeName> enrichmentTypes = enrichmentType.parse(msg);
        if (!enrichmentTypes.isEmpty()) {
            _debug("Found enrichments for event {}: {}", messageName, enrichmentTypes);
            for (TypeName enrichmentType : enrichmentTypes) {
                String typeNameValue = enrichmentType.value();
                result.put(typeNameValue, messageName);
            }
        } else {
            _debug("No enrichments for event {} found", messageName);
        }

        return result.build();
    }

    private Map<String, String> scanFields(DescriptorProto msg) {
        String msgName = msg.getName();
        _debug("Scanning fields of message {} for the enrichment annotations", msgName);
        Map<String, String> enrichmentsMap = new HashMap<>();
        for (FieldDescriptorProto field : msg.getFieldList()) {
            if (ByOption.isSetFor(field)) {
                ByOption by = new ByOption(packagePrefix, msg, field);
                Map.Entry<String, String> foundEvents = by.collect();
                enrichmentsMap.put(foundEvents.getKey(), foundEvents.getValue());
            }
        }
        return enrichmentsMap;
    }

    @SuppressWarnings("MethodWithMultipleLoops")    // It's fine in this case.
    private Map.Entry<String, String> scanInnerMessages(DescriptorProto msg) {
        _debug("Scanning inner messages of {} message for the annotations", msg.getName());
        for (DescriptorProto innerMsg : msg.getNestedTypeList()) {
            for (FieldDescriptorProto field : innerMsg.getFieldList()) {
                if (ByOption.isSetFor(field)) {
                    String outerEventName = packagePrefix + msg.getName();
                    String enrichmentName =
                            outerEventName +
                                    TypeName.NESTED_TYPE_SEPARATOR +
                                    innerMsg.getName();
                    _debug("'by' option found on field {} targeting outer event {}",
                           field.getName(),
                           outerEventName);
                    return new AbstractMap.SimpleEntry<>(enrichmentName, outerEventName);
                }
            }
        }
        return null;
    }
}
