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

package io.spine.tools.compiler.annotation;

import com.google.protobuf.DescriptorProtos.EnumDescriptorProto;
import io.spine.annotation.Experimental;
import io.spine.ui.Language;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static io.spine.test.compiler.annotation.EnumAnnotatorShouldProto.experimentalType;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DisplayName("EnumAnnotator should")
class EnumAnnotatorTest {

    private static final String INVALID_GEN_PROTO_DIR = "";

    private final EnumAnnotator annotator = new EnumAnnotator(Experimental.class,
                                                              experimentalType,
                                                              Collections.emptySet(),
                                                              INVALID_GEN_PROTO_DIR);

    @Test
    @DisplayName("do nothing if no descriptors specified")
    void do_nothing_if_no_descriptors_specified() {
        annotator.annotate();
    }

    @Test
    @DisplayName("not annotate enum without option")
    void not_annotate_enum_without_option() {
        EnumDescriptorProto descriptorWithoutOption = Language.getDescriptor()
                                                              .toProto();
        boolean shouldAnnotate = annotator.shouldAnnotate(descriptorWithoutOption);
        assertFalse(shouldAnnotate);
    }
}
