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

package io.spine.tools.protodoc;

import org.junit.Test;

import static io.spine.tools.protodoc.PreTagFormatting.CLOSING_PRE;
import static io.spine.tools.protodoc.PreTagFormatting.OPENING_PRE;
import static org.junit.Assert.assertEquals;

/**
 * @author Dmytro Grankin
 */
public class PreTagFormattingShould {

    private final FormattingAction formatting = new PreTagFormatting();

    @Test
    public void remove_tags_generated_by_proto_compiler() {
        final String tagsInsideGeneratedTags = OPENING_PRE + CLOSING_PRE;
        final String source = OPENING_PRE + tagsInsideGeneratedTags + CLOSING_PRE;
        final String result = formatting.execute(source);
        assertEquals(tagsInsideGeneratedTags, result);
    }

    @Test
    public void not_format_javadoc_without_pre_tags() {
        final String javadoc = "/** smth */";
        assertEquals(javadoc, formatting.execute(javadoc));
    }
}
