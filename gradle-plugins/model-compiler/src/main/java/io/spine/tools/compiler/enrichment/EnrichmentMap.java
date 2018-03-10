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
import com.google.common.collect.Multimap;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import io.spine.option.RawListValue;
import io.spine.type.TypeName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static io.spine.option.OptionsProto.enrichment;
import static io.spine.option.OptionsProto.enrichmentFor;
import static io.spine.option.RawListValue.getValueSeparator;
import static io.spine.tools.compiler.enrichment.EnrichmentFinder.PROTO_TYPE_SEPARATOR;

/**
 * Composes enrichment map for multiple message declarations.
 *
 * @author Alexander Litus
 * @author Alex Tymchenko
 * @author Alexander Yevsyukov
 */
class EnrichmentMap {

    private static final String EMPTY_TYPE_NAME = "";

    private final String packagePrefix;
    private final TypeNameValue eventTypeParser;
    private final TypeNameValue enrichmentTypeParser;

    EnrichmentMap(String packagePrefix) {
        this.packagePrefix = packagePrefix;
        this.eventTypeParser = new TypeNameValue(enrichmentFor, packagePrefix);
        this.enrichmentTypeParser = new TypeNameValue(enrichment, packagePrefix);
    }

    Map<String, String> allOf(Iterable<DescriptorProto> messages) {
        final HashMultimap<String, String> multimap = HashMultimap.create();
        for (DescriptorProto msg : messages) {
            handleMessage(multimap, msg);
        }
        log().debug("Found enrichments: {}", multimap.toString());
        final Map<String, String> merged = merge(multimap);
        return merged;
    }

    /**
     * Transforms the passed multimap with possible several entries per key, into
     * a map where several values from the passed multimap are joined into
     * a single value.
     *
     * <p>The values are joined with the
     * {@linkplain RawListValue#VALUE_SEPARATOR value separator}.
     *
     * <p>Merging may be required when the wildcard {@code by} option values are handled,
     * i.e. when processing a single enrichment type as a map key, but multiple target
     * event types as values.
     */
    private static Map<String, String> merge(HashMultimap<String, String> source) {
        log().debug("Merging duplicating entries");
        final ImmutableMap.Builder<String, String> mergedResult = ImmutableMap.builder();
        for (String key : source.keySet()) {
            final Set<String> valuesPerKey = source.get(key);
            /* Empty type name might be present in the values.
               If so, remove it from the set */
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

    private static void put(Map.Entry<String, String> entry, Multimap<String, String> targetMap) {
        // Put key and value separately to avoid an error.
        targetMap.put(entry.getKey(), entry.getValue());
    }

    @SuppressWarnings("MethodWithMultipleLoops")  // It's fine as we don't expect too many items.
    private void handleMessage(Multimap<String, String> targetMap, DescriptorProto msg) {
        final Map<String, String> entries = scanMsg(msg);
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            put(entry, targetMap);
        }
        if (!entries.isEmpty()) {
            return;
        }
        final Map<String, String> entryFromField = scanFields(msg);
        if (entryFromField.size() > 0) {
            for (Map.Entry<String, String> entry : entryFromField.entrySet()) {
                put(entry, targetMap);
            }
            return;
        }
        final Map.Entry<String, String> entryFromInnerMsg = scanInnerMessages(msg);
        if (entryFromInnerMsg != null) {
            put(entryFromInnerMsg, targetMap);
            log().debug("Found enrichment: {} -> {}",
                        entryFromInnerMsg.getKey(),
                        entryFromInnerMsg.getValue());
        } else {
            log().debug("No enrichment or event annotations found for message {}", msg.getName());
        }
    }

    @SuppressWarnings("MethodWithMoreThanThreeNegations")
    private Map<String, String> scanMsg(DescriptorProto msg) {
        final ImmutableMap.Builder<String, String> result = ImmutableMap.builder();
        final String messageName = packagePrefix + msg.getName();

        final Logger log = log();
        // Treating current {@code msg} as an enrichment object.
        log.debug("Scanning message {} for the enrichment annotations", messageName);
        final Collection<TypeName> eventTypes = eventTypeParser.parseUnknownOption(msg);
        if (!eventTypes.isEmpty()) {
            final String mergedValue = Joiner.on(getValueSeparator())
                                             .join(eventTypes);
            log.debug("Found target events: {}", mergedValue);
            result.put(messageName, mergedValue);
        } else {
            log.debug("No target events found");
        }

        // Treating current {@code msg} as a target for enrichment (e.g. Spine event).
        log.debug("Scanning message {} for the enrichment target annotations", messageName);
        final Collection<TypeName> enrichmentTypes = enrichmentTypeParser.parseUnknownOption(msg);
        if (!enrichmentTypes.isEmpty()) {
            log.debug("Found enrichments for event {}: {}", messageName, enrichmentTypes);
            for (TypeName enrichmentType : enrichmentTypes) {
                final String typeNameValue = enrichmentType.value();
                result.put(typeNameValue, messageName);
            }
        } else {
            log.debug("No enrichments for event {} found", messageName);
        }

        return result.build();
    }

    private Map<String, String> scanFields(DescriptorProto msg) {
        final String msgName = msg.getName();
        log().debug("Scanning fields of message {} for the enrichment annotations", msgName);
        final Map<String, String> enrichmentsMap = new HashMap<>();
        for (FieldDescriptorProto field : msg.getFieldList()) {
            if (ByOption.isSetFor(field)) {
                final ByOption by = new ByOption(packagePrefix, msg, field);
                final Map.Entry<String, String> foundEvents = by.collect();
                enrichmentsMap.put(foundEvents.getKey(), foundEvents.getValue());
            }
        }
        return enrichmentsMap;
    }


    @SuppressWarnings("MethodWithMultipleLoops")    // It's fine in this case.
    private Map.Entry<String, String> scanInnerMessages(DescriptorProto msg) {
        final Logger log = log();
        log.debug("Scanning inner messages of {} message for the annotations", msg.getName());
        for (DescriptorProto innerMsg : msg.getNestedTypeList()) {
            for (FieldDescriptorProto field : innerMsg.getFieldList()) {
                if (ByOption.isSetFor(field)) {
                    final String outerEventName = packagePrefix + msg.getName();
                    final String enrichmentName =
                            outerEventName +
                                    PROTO_TYPE_SEPARATOR +
                                    innerMsg.getName();
                    log.debug("'by' option found on field {} targeting outer event {}",
                              field.getName(),
                              outerEventName);
                    return new AbstractMap.SimpleEntry<>(enrichmentName, outerEventName);
                }
            }
        }
        return null;
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
