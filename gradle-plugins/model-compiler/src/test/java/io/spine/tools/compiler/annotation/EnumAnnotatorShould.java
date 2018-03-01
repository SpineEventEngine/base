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

package io.spine.tools.compiler.annotation;

import com.google.protobuf.DescriptorProtos.EnumDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import io.spine.annotation.Experimental;
import io.spine.ui.Language;
import org.junit.Test;

import java.util.Collections;

import static io.spine.test.compiler.annotation.EnumAnnotatorShouldProto.experimentalType;
import static org.junit.Assert.assertFalse;

/**
 * @author Dmytro Grankin
 */
public class EnumAnnotatorShould {

    private static final String INVALID_GEN_PROTO_DIR = "";

    private final EnumAnnotator annotator = new EnumAnnotator(Experimental.class,
                                                              experimentalType,
                                                              Collections.<FileDescriptorProto>emptySet(),
                                                              INVALID_GEN_PROTO_DIR);

    @Test
    public void do_nothing_if_no_descriptors_specified() {
        annotator.annotate();
    }

    @Test
    public void not_annotate_enum_without_option() {
        final EnumDescriptorProto descriptorWithoutOption = Language.getDescriptor()
                                                                    .toProto();
        final boolean shouldAnnotate = annotator.shouldAnnotate(descriptorWithoutOption);
        assertFalse(shouldAnnotate);
    }
}
