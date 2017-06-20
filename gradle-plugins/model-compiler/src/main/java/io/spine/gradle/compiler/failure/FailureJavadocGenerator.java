/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

package io.spine.gradle.compiler.failure;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.SourceCodeInfo.Location;
import io.spine.gradle.compiler.javadoc.JavadocEscaper;
import io.spine.gradle.compiler.message.LocationPath;
import io.spine.gradle.compiler.util.JavaCode;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * A generator for the failure Javadocs content.
 *
 * <p>Could be used only if protobuf plugin configured properly:
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
 * @author Dmytro Grankin
 * @see <a href="https://github.com/google/protobuf-gradle-plugin/blob/master/README.md#generate-descriptor-set-files">
 * Protobuf plugin configuration</a>
 */
public class FailureJavadocGenerator {

    @VisibleForTesting
    protected static final String OPENING_PRE = "<pre>";

    //TODO:2017-03-24:dmytro.grankin: Replace hardcoded line separator by system-independent
    // after https://github.com/square/javapoet/issues/552 fix.
    @SuppressWarnings("HardcodedLineSeparator")
    private static final String LINE_SEPARATOR = "\n";

    private final FailureMetadata failureMetadata;

    public FailureJavadocGenerator(FailureMetadata failureMetadata) {
        this.failureMetadata = failureMetadata;
    }

    /**
     * Generates a Javadoc content for the failure.
     *
     * @return the class-level Javadoc content
     */
    @SuppressWarnings("StringBufferWithoutInitialCapacity") // Cannot make valuable initialization
    public String generateClassJavadoc() {
        final Optional<String> leadingComments = getFailureLeadingComments();
        final StringBuilder builder = new StringBuilder();

        if (leadingComments.isPresent()) {
            builder.append(OPENING_PRE)
                   .append(LINE_SEPARATOR)
                   .append(JavadocEscaper.escape(leadingComments.get()))
                   .append("</pre>")
                   .append(LINE_SEPARATOR)
                   .append(LINE_SEPARATOR);
        }

        builder.append("Failure based on protobuf type {@code ")
               .append(failureMetadata.getJavaPackage())
               .append('.')
               .append(failureMetadata.getClassName())
               .append('}')
               .append(LINE_SEPARATOR);
        return builder.toString();
    }

    /**
     * Generates a Javadoc content for the failure constructor.
     *
     * @return the constructor Javadoc content
     */
    public String generateConstructorJavadoc() {
        final StringBuilder builder = new StringBuilder("Creates a new instance.");
        final Map<FieldDescriptorProto, String> commentedFields = getCommentedFields();

        if (!commentedFields.isEmpty()) {
            int maxFieldLength = getMaxFieldNameLength(commentedFields.keySet());

            builder.append(LINE_SEPARATOR)
                   .append(LINE_SEPARATOR);
            for (Entry<FieldDescriptorProto, String> commentedField : commentedFields.entrySet()) {
                final String fieldName = JavaCode.toJavaFieldName(commentedField.getKey()
                                                                                .getName(), false);
                final int commentOffset = maxFieldLength - fieldName.length() + 1;
                builder.append("@param ")
                       .append(fieldName)
                       .append(Strings.repeat(" ", commentOffset))
                       .append(JavadocEscaper.escape(commentedField.getValue()));
            }
        }

        return builder.toString();
    }

    /**
     * Returns the failure field leading comments.
     *
     * @param field the failure field
     * @return the field leading comments or empty {@code Optional} if there are no such comments
     */
    private Optional<String> getFieldLeadingComments(FieldDescriptorProto field) {
        final LocationPath fieldPath = getFieldLocationPath(field);
        return getLeadingComments(fieldPath);
    }

    /**
     * Returns the failure leading comments.
     *
     * @return the failure leading comments or empty {@code Optional} if there are no such comments
     */
    private Optional<String> getFailureLeadingComments() {
        final LocationPath messagePath = getMessageLocationPath();
        return getLeadingComments(messagePath);
    }

    /**
     * Obtains a leading comments by the {@link LocationPath}.
     *
     * @param locationPath the location path to get leading comments
     * @return the leading comments or empty {@code Optional} if there are no such comments
     */
    private Optional<String> getLeadingComments(LocationPath locationPath) {
        if (!failureMetadata.getFileDescriptor()
                            .hasSourceCodeInfo()) {
            final String errMsg = "To enable failure generation, please configure the Gradle " +
                    "Protobuf plugin as follows: `task.descriptorSetOptions.includeSourceInfo = true`.";
            throw new IllegalStateException(errMsg);
        }

        final Location location = getLocation(locationPath);
        return location.hasLeadingComments()
               ? Optional.of(location.getLeadingComments())
               : Optional.<String>absent();
    }

    /**
     * Returns the message location path for a top-level message definition.
     *
     * @return the message location path
     */
    private LocationPath getMessageLocationPath() {
        return new LocationPath(
                Arrays.asList(
                        FileDescriptorProto.MESSAGE_TYPE_FIELD_NUMBER,
                        getTopLevelMessageIndex())
        );
    }

    /**
     * Returns the field {@link LocationPath} for a top-level message definition.
     *
     * <p>Protobuf extensions are not supported.
     *
     * @param field the field to get location path
     * @return the field location path
     */
    private LocationPath getFieldLocationPath(FieldDescriptorProto field) {
        final LocationPath locationPath = new LocationPath();

        locationPath.addAll(getMessageLocationPath());
        locationPath.add(DescriptorProto.FIELD_FIELD_NUMBER);
        locationPath.add(getFieldIndex(field));
        return locationPath;
    }

    private int getTopLevelMessageIndex() {
        final List<DescriptorProto> messages = failureMetadata.getFileDescriptor()
                                                              .getMessageTypeList();
        for (DescriptorProto currentMessage : messages) {
            if (currentMessage.equals(failureMetadata.getDescriptor())) {
                return messages.indexOf(failureMetadata.getDescriptor());
            }
        }

        final String msg = format("The failure file \"%s\" should contain \"%s\" failure.",
                                  failureMetadata.getFileDescriptor()
                                                 .getName(),
                                  failureMetadata.getDescriptor()
                                                 .getName());
        throw new IllegalStateException(msg);
    }

    private int getFieldIndex(FieldDescriptorProto field) {
        return failureMetadata.getDescriptor()
                              .getFieldList()
                              .indexOf(field);
    }

    /**
     * Returns the {@link Location} for the {@link LocationPath}.
     *
     * @param locationPath the location path
     * @return the location for the path
     */
    private Location getLocation(LocationPath locationPath) {
        for (Location location : failureMetadata.getFileDescriptor()
                                                .getSourceCodeInfo()
                                                .getLocationList()) {
            if (location.getPathList()
                        .equals(locationPath.getPath())) {
                return location;
            }
        }

        final String msg = format("The location with %s path should be present in \"%s\".",
                                  locationPath,
                                  failureMetadata.getFileDescriptor()
                                                 .getName());
        throw new IllegalStateException(msg);
    }

    /**
     * Returns field-to-comment map in order of {@linkplain FieldDescriptorProto fields}
     * declaration in the failure.
     *
     * @return the commented fields
     */
    private Map<FieldDescriptorProto, String> getCommentedFields() {
        final Map<FieldDescriptorProto, String> commentedFields = new LinkedHashMap<>();

        for (FieldDescriptorProto field : failureMetadata.getDescriptor()
                                                         .getFieldList()) {
            final Optional<String> leadingComments = getFieldLeadingComments(field);
            if (leadingComments.isPresent()) {
                commentedFields.put(field, leadingComments.get());
            }
        }

        return commentedFields;
    }

    /**
     * Returns a max field name length among the non-empty
     * {@linkplain FieldDescriptorProto field} collection.
     *
     * @param fields the non-empty fields collection
     * @return the max name length
     */
    private static int getMaxFieldNameLength(Iterable<FieldDescriptorProto> fields) {
        final Ordering<FieldDescriptorProto> ordering = new Ordering<FieldDescriptorProto>() {
            @Override
            public int compare(@Nullable FieldDescriptorProto left,
                               @Nullable FieldDescriptorProto right) {
                checkNotNull(left);
                checkNotNull(right);

                return Ints.compare(left.getName()
                                        .length(), right.getName()
                                                        .length());
            }
        };

        final FieldDescriptorProto longestNameField = ordering.max(fields);
        return longestNameField.getName()
                               .length();
    }
}
