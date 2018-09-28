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

import org.junit.Test;

import java.util.Collections;

import static io.spine.tools.compiler.annotation.FieldAnnotator.shouldAnnotateMethod;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FieldAnnotatorShould {

    @Test
    public void annotate_accessor_for_field() {
        String fieldName = "FieldName";
        String methodName = getAccessorName(fieldName);
        assertTrue(shouldAnnotateMethod(methodName, fieldName, Collections.<String>emptyList()));
    }

    @Test
    public void not_annotate_accessor_for_field_that_does_not_require_it() {
        String requiresAnnotation = "GoodBoy";
        String notRequiresAnnotation = requiresAnnotation + "AdditionalPart";
        String methodName = getAccessorName(notRequiresAnnotation);
        assertFalse(shouldAnnotateMethod(methodName, requiresAnnotation,
                                         singleton(notRequiresAnnotation)));
    }

    @Test
    public void filter_fields_that_does_not_require_annotation_by_name_length() {
        String requiresAnnotation = "FriendList";
        String notRequiresAnnotation = requiresAnnotation.substring(0, 4);
        String methodName = getAccessorName(requiresAnnotation);
        assertTrue(shouldAnnotateMethod(methodName, requiresAnnotation,
                                        singleton(notRequiresAnnotation)));
    }

    private static String getAccessorName(String fieldName) {
        return "has" + fieldName;
    }
}
