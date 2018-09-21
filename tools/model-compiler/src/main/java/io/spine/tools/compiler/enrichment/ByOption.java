/*
 * Copyright 2018, TeamDev. All rights reserved.
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

import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import io.spine.logging.Logging;
import io.spine.option.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static io.spine.option.OptionsProto.by;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * Obtains event names from the {@code "by"} field option of a message.
 *
 * @author Alexander Litus
 * @author Alexander Yevsyukov
 */
class ByOption implements Logging {

    private final String packagePrefix;
    private final DescriptorProto message;
    private final FieldDescriptorProto field;

    ByOption(String packagePrefix, DescriptorProto message, FieldDescriptorProto field) {
        this.packagePrefix = packagePrefix;
        this.message = message;
        this.field = field;
    }

    static boolean isSetFor(FieldDescriptorProto field) {
        return Options.option(field, by)
                      .isPresent();
    }

    Map.Entry<String, String> collect() {
        Collection<String> eventNamesFromBy = parse();
        Map.Entry<String, String> result = group(eventNamesFromBy);
        return result;
    }

    /**
     * Obtains the list with fully-qualified names of target event types for
     * the given field.
     */
    private List<String> parse() {
        List<FieldReference> fieldRefs = FieldReference.allFrom(field);

        ImmutableList.Builder<String> result = ImmutableList.builder();
        for (FieldReference fieldRef : fieldRefs) {
            if (fieldRef.isWildcard() && fieldRefs.size() > 1) {
                // Multiple argument `by` annotation can not contain wildcard reference onto
                // the event type if the type was not specified with a `enrichment_for` annotation.
                throw invalidByOptionUsage(field.getName());
            }

            if (fieldRef.isInner()) {
                // The short form type names are handled as inner types.
                continue;
            }

            String fullTypeName = fieldRef.getType();
            result.add(fullTypeName);
        }

        return result.build();
    }

    private Map.Entry<String, String> group(Collection<String> events) {
        String enrichment = message.getName();
        String fieldName = field.getName();
        Logger log = log();
        Collection<String> eventGroup = new HashSet<>(events.size());
        for (String eventName : events) {
            if (eventName == null || eventName.trim()
                                              .isEmpty()) {
                throw invalidByOptionValue(enrichment);
            }
            log.debug("'by' option found on field {} targeting {}", fieldName, eventName);

            if (FieldReference.ANY_BY_OPTION_TARGET.equals(eventName)) {
                log.warn("Skipping a wildcard event");
                /* Ignore the wildcard `by` options, as we don't know
                   the target event type in this case. */
                continue;
            }
            eventGroup.add(eventName);
        }
        String enrichmentName = packagePrefix + enrichment;
        String eventGroupString = TypeNameParser.joiner.join(eventGroup);
        Map.Entry<String, String> result =
                new AbstractMap.SimpleEntry<>(enrichmentName, eventGroupString);

        return result;
    }

    private static IllegalStateException invalidByOptionValue(String msgName) {
        throw newIllegalStateException(
                "The message field `%s` has invalid 'by' option value, " +
                        "which must be a fully-qualified field reference.",
                msgName
        );
    }

    private static IllegalStateException invalidByOptionUsage(String msgName) {
        throw newIllegalStateException(
                "Field of message `%s` has invalid 'by' option value. " +
                        "Wildcard type is not allowed with multiple arguments. " +
                        "Please, specify the type either with `by` or " +
                        "with `enrichment_for` annotation.",
                msgName
        );
    }
}
