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
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import io.spine.option.TypeNameParser;
import io.spine.type.TypeName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.spine.option.OptionsProto.enrichment;
import static io.spine.option.OptionsProto.enrichmentFor;
import static io.spine.option.RawListParser.getValueSeparator;

/**
 * Finds event enrichment Protobuf definitions.
 *
 * @author Alexander Litus
 * @author Alex Tymchenko
 */
public class EnrichmentFinder {

    static final String PROTO_TYPE_SEPARATOR = ".";

    private final FileDescriptorProto file;
    private final String packagePrefix;
    private final TypeNameParser eventTypeParser;
    private final TypeNameParser enrichmentTypeParser;

    /**
     * Creates a new instance.
     *
     * @param file a file to search enrichments in
     */
    EnrichmentFinder(FileDescriptorProto file) {
        this.file = file;
        this.packagePrefix = file.getPackage() + PROTO_TYPE_SEPARATOR;
        this.eventTypeParser = new TypeNameParser(enrichmentFor, packagePrefix);
        this.enrichmentTypeParser = new TypeNameParser(enrichment, packagePrefix);
    }

    /**
     * Finds event enrichment Protobuf definitions in the file.
     *
     * @return a map from enrichment type name to event to enrich type name
     */
    Map<String, String> findEnrichments() {
        final Logger log = log();
        log.debug("Looking up for the enrichments in {}", file.getName());

        final List<DescriptorProto> messages = file.getMessageTypeList();
        final Map<String, String> result = findEnrichments(messages);
        return result;
    }

    private Map<String, String> findEnrichments(List<DescriptorProto> messages) {
        final HashMultimap<String, String> multimap = HashMultimap.create();
        for (DescriptorProto msg : messages) {
            putEntry(multimap, msg);
        }
        log().debug("Found enrichments: {}", multimap.toString());
        final Map<String, String> merged = EnrichmentMap.mergeDuplicateValues(multimap);
        return merged;
    }

    private static void put(Map.Entry<String, String> entry, Multimap<String, String> targetMap) {
        // Put key and value separately to avoid an error.
        targetMap.put(entry.getKey(), entry.getValue());
    }

    @SuppressWarnings("MethodWithMultipleLoops")  // It's fine as we don't expect too many items.
    private void putEntry(Multimap<String, String> targetMap, DescriptorProto msg) {
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

    private static Logger log() {
        return LoggerSingleton.INSTANCE.logger;
    }

    private enum LoggerSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger logger = LoggerFactory.getLogger(EnrichmentFinder.class);
    }
}
