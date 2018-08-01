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

package io.spine.code.proto;

import com.google.common.collect.ImmutableList;
import io.spine.value.StringTypeValue;
import org.junit.Test;

import java.util.List;

import static io.spine.code.proto.CamelCase.convert;
import static io.spine.testing.Tests.assertHasPrivateParameterlessCtor;
import static org.junit.Assert.assertEquals;

/**
 * @author Alexander Yevsyukov
 */
public class CamelCaseShould {

    @Test
    public void have_utility_ctor() {
        assertHasPrivateParameterlessCtor(CamelCase.class);
    }

    @Test
    public void capitalize_words() {
        assertEquals("CapitalizeWords", convert(new UnderName("capitalize_words")));
    }

    @Test
    public void do_not_lowercase_words() {
        assertEquals("TestHTTPRequest", convert(new UnderName("test_HTTP_request")));
    }

    /**
     * A test value object.
     */
    private static class UnderName extends StringTypeValue implements UnderscoredName {

        private static final long serialVersionUID = 0L;

        protected UnderName(String value) {
            super(value);
        }

        @Override
        public List<String> words() {
            return ImmutableList.copyOf(value().split(WORD_SEPARATOR));
        }
    }
}
