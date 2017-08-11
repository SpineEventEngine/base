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

package io.spine.tools.protoc;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse;
import io.spine.annotation.Internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A helper class for reading instances of {@link CodeGeneratorRequest} and writing instances of
 * {@link CodeGeneratorResponse} from and into the given data streams.
 *
 * <p>Neither of the method of this class ever {@linkplain java.io.Closeable#close() close} or
 * {@linkplain OutputStream#flush() flush} the underlying streams.
 *
 * <p>If any {@linkplain IOException I/O failure} happens, an {@link IllegalStateException} is
 * thrown by any of the methods of this class.
 *
 * @author Dmytro Dashenkov
 */
@Internal
final class MessageIO {

    private final InputStream in;
    private final OutputStream out;

    MessageIO(InputStream in, OutputStream out) {
        this.in = checkNotNull(in);
        this.out = checkNotNull(out);
    }

    final CodeGeneratorRequest readRequest() {
        final CodedInputStream stream = CodedInputStream.newInstance(in);
        try {
            final CodeGeneratorRequest request = CodeGeneratorRequest.parseFrom(stream);
            return request;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    final void writeResponse(CodeGeneratorResponse response) {
        checkNotNull(response);
        final CodedOutputStream stream = CodedOutputStream.newInstance(out);
        try {
            response.writeTo(stream);
            stream.flush();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
