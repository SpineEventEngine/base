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

package io.spine.tools.java;

import com.google.common.base.Optional;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import com.google.protobuf.TimestampOrBuilder;
import io.spine.tools.proto.FileSet;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link SimpleClassName}.
 *
 * <p>Even though the code where {@link SimpleClassName} resides no longer depends on the
 * {@code base} module, this test uses descriptors copied from the {@code base} stored in
 * resources. That's why the {@code ErrorProto} descriptor is available for these tests.
 *
 * @author Alexander Yevsyukov
 */
public class SimpleClassNameShould {

    private static final FileSet mainSet = FileSet.loadMain();
    private static final String ERROR_PROTO = "ErrorProto";

    private FileDescriptor errorProto;

    @SuppressWarnings("ConstantConditions") /* The file is present in resources. */
    @Before
    public void setUp() {
        errorProto = mainSet.tryFind("spine/base/error.proto")
                            .get();
    }

    @Test
    public void obtain_outer_class_name() {
        assertEquals(ERROR_PROTO, SimpleClassName.outerOf(errorProto.toProto())
                                                 .value());
    }

    @Test
    public void obtain_declared_outer_class_name() {
        final Optional<SimpleClassName> className =
                SimpleClassName.declaredOuterClassName(errorProto.toProto());

        assertTrue(className.isPresent());
        assertEquals(ERROR_PROTO, className.get()
                                           .value());
    }

    @Test
    public void obtain_default_builder_class_name() {
        assertTrue(SimpleClassName.ofBuilder()
                                  .value()
                                  .contains(Message.Builder.class.getSimpleName()));
    }

    @Test
    public void obtains_name_for_message_or_builder() {
        assertEquals(TimestampOrBuilder.class.getSimpleName(),
                     SimpleClassName.messageOrBuilder(Timestamp.class.getSimpleName()).value());
    }

    @Test
    public void obtains_value_by_descriptor() {
        assertEquals(Timestamp.class.getSimpleName(),
                     SimpleClassName.ofMessage(Timestamp.getDescriptor())
                                    .value());
    }

    @Test
    public void convert_to_file_name() {
        final SimpleClassName className = SimpleClassName.ofMessage(Timestamp.getDescriptor());
        assertTrue(className.toFileName()
                            .value()
                            .contains(Timestamp.class.getSimpleName()));
    }
}
