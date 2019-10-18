/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.generate.dart;

import com.google.common.testing.NullPointerTester;
import io.spine.io.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;

@DisplayName("`CodeTemplate` should")
class CodeTemplateTest {

    private static final Resource resource = Resource.file("test.template");

    @Test
    @DisplayName("not accept nulls in constructor")
    void nullsInConstructor() {
        new NullPointerTester()
                .testAllPublicConstructors(CodeTemplate.class);
    }

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void nulls() {
        new NullPointerTester()
                .testAllPublicInstanceMethods(new CodeTemplate(resource));
    }

    @Test
    @DisplayName("replace a single insertion point")
    void replaceOne() {
        CodeTemplate template = new CodeTemplate(resource);
        template.replace(InsertionPoints.END_OF_LINE.name(),
                         InsertionPoints.END_OF_LINE.replaceWith);
        String contents = template.compile()
                                  .contents();
        assertThat(contents).contains(InsertionPoints.END_OF_LINE.replaceWith);
    }

    @Test
    @DisplayName("replace all insertion points")
    void replaceAll() {
        CodeTemplate template = new CodeTemplate(resource);
        template.replace(InsertionPoints.END_OF_LINE.name(),
                         InsertionPoints.END_OF_LINE.replaceWith);
        template.replace(InsertionPoints.MID_LINE.name(),
                         InsertionPoints.MID_LINE.replaceWith);
        String contents = template.compile()
                                  .contents();
        for (InsertionPoints point : InsertionPoints.values()) {
            assertThat(contents).contains(point.replaceWith);
        }
    }

    /**
     * Insertion points defined in the {@link #resource}.
     */
    private enum InsertionPoints {
        END_OF_LINE("######"),
        MID_LINE("$$$$$$");

        private final String replaceWith;

        InsertionPoints(String value) {
            replaceWith = value;
        }
    }
}
