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

import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import io.spine.code.proto.FieldReference;
import io.spine.logging.Logging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * Provides mapping from a enrichment type name to names of enriched types.
 */
final class Entry implements Logging {

    private final DescriptorProto message;
    private final FieldDescriptorProto field;
    private final String packagePrefix;

    Entry(DescriptorProto message, FieldDescriptorProto field, String packagePrefix) {
        this.message = message;
        this.field = field;
        this.packagePrefix = packagePrefix;
    }

    String enrichmentType() {
        String result = packagePrefix + message.getName();
        return result;
    }

    String sourceTypes() {
        Collection<String> sourceTypeNames = parse();
        String result = group(sourceTypeNames);
        return result;
    }

    /**
     * Obtains the list with fully-qualified names of source event types for
     * obtaining the value of the given enrichment field.
     */
    private List<String> parse() {
        List<FieldReference> fieldRefs = FieldReference.allFrom(field);

        ImmutableList.Builder<String> result = ImmutableList.builder();
        for (FieldReference fieldRef : fieldRefs) {
            if (fieldRef.isWildcard() && fieldRefs.size() > 1) {
                // Multiple argument `by` annotation can not contain wildcard reference onto
                // the source type if the type was not specified with a `enrichment_for` annotation.
                throw invalidByOptionUsage();
            }

            if (fieldRef.isInner()) {
                // The short form type names are handled as inner types.
                continue;
            }

            String fullTypeName = fieldRef.typeName();
            result.add(fullTypeName);
        }

        return result.build();
    }

    private String group(Collection<String> srcTypes) {
        String fieldName = field.getName();
        List<String> list = new ArrayList<>();
        for (String srcType : srcTypes) {
            if (srcType == null || srcType.trim()
                                          .isEmpty()) {
                throw invalidByOptionValue();
            }
            _debug("'by' option found on field {} targeting {}", fieldName, srcType);
            if (FieldReference.isWildcard(srcType)) {
                _warn("Skipping a wildcard event");
                /* Ignore the wildcard `by` options, as we don't know
                   the target event type in this case. */
                continue;
            }
            list.add(srcType);
        }
        list.sort(Comparator.naturalOrder());
        String result = TypeNameParser.joiner.join(list);
        return result;
    }

    private IllegalStateException invalidByOptionValue() {
        throw newIllegalStateException(
                "The message field `%s` has invalid 'by' option value, " +
                        "which must be a fully-qualified field reference.",
                message.getName()
        );
    }

    private IllegalStateException invalidByOptionUsage() {
        throw newIllegalStateException(
                "Field of message `%s` has invalid 'by' option value. " +
                        "Wildcard type is not allowed with multiple arguments. " +
                        "Please, specify the type either with `by` or " +
                        "with `enrichment_for` annotation.",
                field.getName()
        );
    }
}
