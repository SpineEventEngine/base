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

package io.spine.gradle.compiler.annotation;

import com.google.protobuf.DescriptorProtos;
import io.spine.annotation.Experimental;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

import static io.spine.test.compiler.annotation.EnumAnnotatorShouldProto.experimentalType;

/**
 * @author Dmytro Grankin
 */
public class EnumAnnotatorShould {

    @Test
    public void do_nothing_if_no_descriptors_specified() {
        final Set<DescriptorProtos.FileDescriptorProto> emptyDescriptors = Collections.emptySet();
        final String invalidGenProtobufPath = "";
        final EnumAnnotator enumAnnotator = new EnumAnnotator(Experimental.class,
                                                              experimentalType,
                                                              emptyDescriptors,
                                                              invalidGenProtobufPath);
        enumAnnotator.annotate();
    }
}
