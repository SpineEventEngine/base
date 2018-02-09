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

package io.spine.tools.proto;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.NullPointerTester;
import org.junit.Test;

import java.util.List;

import static io.spine.tools.proto.FileName.toCamelCase;
import static org.junit.Assert.assertEquals;

/**
 * @author Alexander Yevsyukov
 */
public class FileNameShould {

    @Test
    public void pass_null_tolerance_check() {
        new NullPointerTester().testStaticMethods(FileName.class,
                                                  NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    public void return_words() {
        final List<String> words = FieldName.of("some_proto_file_name").words();

        assertEquals(ImmutableList.of("some", "proto", "file", "name"), words);
    }

    @SuppressWarnings("DuplicateStringLiteralInspection")
    @Test
    public void calculate_outer_class_name() {
        assertEquals("Rejections", toCamelCase("rejections"));
        assertEquals("ManyRejections", toCamelCase("many_rejections"));
        assertEquals("ManyMoreRejections", toCamelCase("many_more_rejections"));
    }
}
