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

import com.google.common.testing.NullPointerTester;
import com.google.common.util.concurrent.SettableFuture;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newLinkedList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Dmytro Dashenkov
 */
public class MessageIOShould {

    @Test
    public void not_accept_nulls() {
        final NullPointerTester tester = new NullPointerTester()
                .setDefault(CodeGeneratorResponse.class, CodeGeneratorResponse.getDefaultInstance())
                .setDefault(InputStream.class, new StubInputStream())
                .setDefault(OutputStream.class, new StubOutputStream());
        tester.testAllPublicConstructors(MessageIO.class);
        tester.testAllPublicInstanceMethods(MessageIO.class);
    }

    @Test
    public void read_message() {
        final CodeGeneratorRequest expected = request();
        final StubInputStream in = in();
        in.emit(expected);
        final MessageIO io = new MessageIO(in, out());
        final CodeGeneratorRequest actual = io.readRequest();
        assertEquals(expected, actual);
    }

    @Test
    public void write_message() {
        final CodeGeneratorResponse expected = response();
        final StubOutputStream out = out();
        final MessageIO io = new MessageIO(in(), out);
        io.writeResponse(expected);
        final CodeGeneratorResponse actual = out.getData();
        assertEquals(expected, actual);
    }

    private static StubInputStream in() {
        return new StubInputStream();
    }

    private static StubOutputStream out() {
        return new StubOutputStream();
    }

    private static CodeGeneratorRequest request() {
        return CodeGeneratorRequest.getDefaultInstance();
    }

    private static CodeGeneratorResponse response() {
        return CodeGeneratorResponse.getDefaultInstance();
    }

    private static class StubInputStream extends InputStream {

        private final SettableFuture<byte[]> data = SettableFuture.create();

        private int index = -1;

        private void emit(CodeGeneratorRequest data) {
            checkState(!this.data.isDone());

            final byte[] bytes = data.toByteArray();
            this.data.set(bytes);
        }

        @Override
        public int read() throws IOException {
            final byte[] bytes;
            try {
                bytes = data.get();
            } catch (InterruptedException | ExecutionException e) {
                fail(e.getMessage());
                return -1;
            }
            if (bytes.length >= index) {
                return -1;
            }
            final int result = bytes[index];
            index++;
            return result;
        }
    }

    private static class StubOutputStream extends OutputStream {

        private final List<Byte> bytes = newLinkedList();

        @Override
        public void write(int b) throws IOException {
            @SuppressWarnings("NumericCastThatLosesPrecision") final byte dataPiece = (byte) b;
            bytes.add(dataPiece);
        }

        private CodeGeneratorResponse getData() {
            final byte[] rawData = new byte[bytes.size()];
            for (int i = 0; i < bytes.size(); i++) {
                final Byte dataPiece = bytes.get(i);
                rawData[i] = dataPiece;
            }
            final CodeGeneratorResponse data;
            try {
                data = CodeGeneratorResponse.parseFrom(rawData);
            } catch (InvalidProtocolBufferException e) {
                throw new IllegalStateException(e);
            }
            return data;
        }
    }
}
