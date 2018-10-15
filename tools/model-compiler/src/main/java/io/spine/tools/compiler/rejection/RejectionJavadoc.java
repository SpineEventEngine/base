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

package io.spine.tools.compiler.rejection;

import com.google.common.base.Strings;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import io.spine.code.javadoc.JavadocEscaper;
import io.spine.code.proto.FieldName;
import io.spine.code.proto.RejectionDeclaration;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Generates Javadoc for a rejection.
 *
 * <p>Requires the following Protobuf plugin configuration:
 * <pre> {@code
 * generateProtoTasks {
 *     all().each { final task ->
 *         // If true, the descriptor set will contain line number information
 *         // and comments. Default is false.
 *         task.descriptorSetOptions.includeSourceInfo = true
 *         // ...
 *     }
 * }
 * }</pre>
 *
 * @see <a href="https://github.com/google/protobuf-gradle-plugin/blob/master/README.md#generate-descriptor-set-files">
 *         Protobuf plugin configuration</a>
 */
class RejectionJavadoc {

    private static final String OPENING_PRE = "<pre>";
    private static final String CLOSING_PRE = "</pre>";
    /*
      TODO:2017-03-24:dmytro.grankin: Replace hardcoded line separator by system-independent
      after https://github.com/square/javapoet/issues/552 is fixed.
    */
    @SuppressWarnings("HardcodedLineSeparator")
    private static final String LINE_SEPARATOR = "\n";

    private final RejectionDeclaration declaration;
    private final RejectionDocumentation documentation;

    RejectionJavadoc(RejectionDeclaration declaration, RejectionDocumentation documentation) {
        this.declaration = declaration;
        this.documentation = documentation;
    }

    /**
     * Generates a Javadoc content for the rejection.
     *
     * @return the class-level Javadoc content
     */
    String forClass() {
        Optional<String> leadingComments = documentation.leadingComments();
        StringBuilder builder = new StringBuilder(256);

        leadingComments.ifPresent(s -> builder.append(OPENING_PRE)
                                              .append(LINE_SEPARATOR)
                                              .append(JavadocEscaper.escape(s))
                                              .append(CLOSING_PRE)
                                              .append(LINE_SEPARATOR)
                                              .append(LINE_SEPARATOR));

        builder.append("Rejection based on proto type {@code ")
               .append(declaration.getJavaPackage())
               .append('.')
               .append(declaration.getSimpleJavaClassName())
               .append('}')
               .append(LINE_SEPARATOR);
        return builder.toString();
    }

    /**
     * Generates a Javadoc content for the rejection constructor.
     *
     * @return the constructor Javadoc content
     */
    String forConstructor() {
        StringBuilder builder = new StringBuilder("Creates a new instance.");
        Map<FieldDescriptorProto, String> commentedFields = documentation.commentedFields();

        if (!commentedFields.isEmpty()) {
            int maxFieldLength = getMaxFieldNameLength(commentedFields.keySet());

            builder.append(LINE_SEPARATOR)
                   .append(LINE_SEPARATOR);
            for (Entry<FieldDescriptorProto, String> commentedField : commentedFields.entrySet()) {
                String fieldName = FieldName.of(commentedField.getKey()
                                                              .getName())
                                            .javaCase();
                int commentOffset = maxFieldLength - fieldName.length() + 1;
                builder.append("@param ")
                       .append(fieldName)
                       .append(Strings.repeat(" ", commentOffset))
                       .append(JavadocEscaper.escape(commentedField.getValue()));
            }
        }

        return builder.toString();
    }

    /**
     * Returns a max field name length among the non-empty
     * {@linkplain FieldDescriptorProto field} collection.
     *
     * @param fields
     *         the non-empty fields collection
     * @return the max name length
     */
    private static int getMaxFieldNameLength(Iterable<FieldDescriptorProto> fields) {
        Ordering<FieldDescriptorProto> ordering = new Ordering<FieldDescriptorProto>() {
            @Override
            public int compare(@Nullable FieldDescriptorProto left,
                               @Nullable FieldDescriptorProto right) {
                checkNotNull(left);
                checkNotNull(right);

                int result = Ints.compare(left.getName()
                                              .length(),
                                          right.getName()
                                               .length());
                return result;
            }
        };

        FieldDescriptorProto longestNameField = ordering.max(fields);
        return longestNameField.getName()
                               .length();
    }
}
