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

package io.spine.code.proto;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.SourceCodeInfo;
import com.google.protobuf.Descriptors.Descriptor;

import java.util.List;
import java.util.Optional;

import static io.spine.util.Exceptions.newIllegalStateException;
import static java.lang.String.format;

/**
 * The documentation of a message in a {@code .proto} file.
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
public class MessageDocumentation {

    private final MessageType declaration;

    public MessageDocumentation(MessageType declaration) {
        this.declaration = declaration;
    }

    /**
     * Obtains the comments going before a rejection declaration.
     *
     * @return the comments text or {@code Optional.empty()} if there are no comments
     */
    public Optional<String> leadingComments() {
        LocationPath messagePath = messagePath();
        if (declaration.isTopLevel()) {
            return leadingComments(messagePath);
        }
        //TODO:2018-12-20:alexander.yevsyukov: Handle nested types.
        return Optional.empty();
    }

    /**
     * Obtains the leading comments for the field.
     *
     * @param field
     *         the descriptor of the field
     * @return the field leading comments or {@code Optional.empty()} if there are no comments
     */
    public Optional<String> fieldLeadingComments(FieldDescriptorProto field) {
        //TODO:2018-12-20:alexander.yevsyukov: Handle nested types.
        if (declaration.isNested()) {
            return Optional.empty();
        }

        LocationPath fieldPath = fieldPath(field);
        return leadingComments(fieldPath);
    }

    /**
     * Obtains a leading comments by the {@link LocationPath}.
     *
     * @param locationPath
     *         the location path to get leading comments
     * @return the leading comments or empty {@code Optional} if there are no such comments
     */
    private Optional<String> leadingComments(LocationPath locationPath) {
        if (!declaration.descriptor()
                        .getFile()
                        .toProto()
                        .hasSourceCodeInfo()) {
            throw newIllegalStateException(
                    "Unable to obtain proto source code info. " +
                    "Please configure the Gradle Protobuf plugin as follows:%n%s",
                    "`task.descriptorSetOptions.includeSourceInfo = true`."
            );
        }

        SourceCodeInfo.Location location = toLocation(locationPath);
        return location.hasLeadingComments()
               ? Optional.of(location.getLeadingComments())
               : Optional.empty();
    }

    /**
     * Returns the message location path for a top-level message definition.
     *
     * @return the message location path
     */
    private LocationPath messagePath() {
        LocationPath path = new LocationPath();
        path.add(FileDescriptorProto.MESSAGE_TYPE_FIELD_NUMBER);
        if (declaration.isTopLevel()) {
            path.add(getTopLevelMessageIndex());
        }
        return path;
    }

    private int getTopLevelMessageIndex() {
        Descriptor descriptor = declaration.descriptor();

        List<DescriptorProto> messages = descriptor.getFile()
                                                   .toProto()
                                                   .getMessageTypeList();
        for (DescriptorProto currentMessage : messages) {
            if (currentMessage.equals(descriptor.toProto())) {
                return messages.indexOf(descriptor.toProto());
            }
        }

        String msg = format("Unable to locate message `%s` in the file file `%s`.",
                            descriptor.toProto()
                                      .getName(),
                            descriptor.toProto()
                                      .getName()
        );
        throw new IllegalStateException(msg);
    }

    /**
     * Returns the field {@link LocationPath} for a top-level message definition.
     *
     * <p>Protobuf extensions are not supported.
     *
     * @param field
     *         the field to get location path
     * @return the field location path
     */
    private LocationPath fieldPath(FieldDescriptorProto field) {
        LocationPath locationPath = new LocationPath();

        locationPath.addAll(messagePath());
        locationPath.add(DescriptorProto.FIELD_FIELD_NUMBER);
        locationPath.add(getFieldIndex(field));
        return locationPath;
    }

    private int getFieldIndex(FieldDescriptorProto field) {
        return declaration.descriptor()
                          .toProto()
                          .getFieldList()
                          .indexOf(field);
    }

    /**
     * Converts {@link LocationPath} related to the message to {@link SourceCodeInfo.Location}.
     *
     * @param locationPath
     *         the location path
     * @return the location for the path
     */
    private SourceCodeInfo.Location toLocation(LocationPath locationPath) {
        FileDescriptorProto declarationFile = declaration.descriptor()
                                                         .getFile()
                                                         .toProto();
        for (SourceCodeInfo.Location location : declarationFile.getSourceCodeInfo()
                                                               .getLocationList()) {
            if (location.getPathList()
                        .equals(locationPath.getPath())) {
                return location;
            }
        }

        String msg = format("The location with %s path should be present in \"%s\".",
                            locationPath, declarationFile.getName());
        throw new IllegalStateException(msg);
    }
}
