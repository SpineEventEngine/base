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

package io.spine.tools;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * @author Alexander Yevsyukov
 */
public class IndentShould {

    @Test(expected = IllegalArgumentException.class)
    public void prohibit_negative_value() {
        Indent.of(-1);
    }

    @Test
    public void allow_zero_indent() {
        final Indent zeroIndent = Indent.of(0);
        assertEquals(0, zeroIndent.getSize());
        assertTrue(zeroIndent.toString()
                             .isEmpty());
    }

    @Test
    public void allow_custom_size() {
        final Indent ofThree = Indent.of(3);
        assertEquals(3, ofThree.getSize());
        assertEquals("   ", ofThree.toString());
    }

    @Test
    public void return_popular_constants() {
        assertEquals(2, Indent.of2()
                              .getSize());
        assertEquals(4, Indent.of4()
                              .getSize());
    }

    @Test
    public void return_constants_by_popular_values() {
        assertSame(Indent.of2(), Indent.of(2));
        assertSame(Indent.of4(), Indent.of(4));
    }
}
