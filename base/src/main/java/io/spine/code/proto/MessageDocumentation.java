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

import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.SourceCodeInfo;
import io.spine.logging.Logging;

import java.util.Optional;

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
@Immutable
public final class MessageDocumentation implements Logging {

    private final MessageType type;

    MessageDocumentation(MessageType type) {
        this.type = type;
    }

    /**
     * Obtains the comments going before a rejection declaration.
     *
     * @return the comments text or {@code Optional.empty()} if there are no comments
     */
    public Optional<String> leadingComments() {
        LocationPath messagePath = type.path();
        if (type.isTopLevel()) {
            return leadingComments(messagePath);
        }
        //TODO:2018-12-20:alexander.yevsyukov: Handle nested types.
        return Optional.empty();
    }


    /**
     * Obtains a leading comments by the {@link LocationPath}.
     *
     * @param locationPath
     *         the location path to get leading comments
     * @return the leading comments or empty {@code Optional} if there are no such comments
     */
    Optional<String> leadingComments(LocationPath locationPath) {
        FileDescriptorProto file = type.descriptor()
                                       .getFile()
                                       .toProto();
        if (!file.hasSourceCodeInfo()) {
            _warn("Unable to obtain proto source code info. " +
                            "Please configure the Gradle Protobuf plugin as follows:%n%s",
                    "`task.descriptorSetOptions.includeSourceInfo = true`.");
            return Optional.empty();
        }

        SourceCodeInfo.Location location = locationPath.toLocation(file);
        return location.hasLeadingComments()
               ? Optional.of(location.getLeadingComments())
               : Optional.empty();
    }
}
