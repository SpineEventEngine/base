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

package io.spine.tools.protodoc;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Dmytro Grankin
 */
public class PreTagFormattingShould {

    private static final String RAW_PROTO_DOC =
            "/** <pre> Doc goes here </pre> <code>string field = 1;</code> */";
    private static final String PROCESSED_PROTO_DOC =
            "/**  Doc goes here  <code>string field = 1;</code> */";

    private static final String RAW_PROTO_DOC_WITH_PRE_TAG =
            "/** Doc header <pre> Preformated doc </pre> <code>string field = 1;</code> */";


    private final FormattingAction formatting = new PreTagFormatting();

    @Test
    public void remove_tags_generated_by_proto_compiler() {
        final String result = formatting.execute(RAW_PROTO_DOC);
        assertEquals(PROCESSED_PROTO_DOC, result);
    }

    @Test
    public void not_remove_user_pre_tags_from_doc() {
        assertEquals(RAW_PROTO_DOC_WITH_PRE_TAG, formatting.execute(RAW_PROTO_DOC_WITH_PRE_TAG));
    }

    @Test
    public void not_format_javadoc_without_pre_tags() {
        final String javadoc = "/** smth */";
        assertEquals(javadoc, formatting.execute(javadoc));
    }
}
