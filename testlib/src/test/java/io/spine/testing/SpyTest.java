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

package io.spine.testing;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DisplayName("Spy should")
class SpyTest {

    private static final String FIELD_NAME = "list";

    private List<String> list;

    @BeforeEach
    void setUp() {
        list = Lists.newArrayList("a", "b", "c");
    }

    @AfterEach
    void tearDown() {
        list = null;
    }

    @Test
    @DisplayName("inject by a class")
    void inject_by_class() {
        List spy = Spy.ofClass(List.class)
                      .on(this);
        assertSpy(spy);
    }

    @Test
    @DisplayName("inject by a name")
    void inject_by_name() {
        List spy = Spy.ofClass(List.class)
                      .on(this, FIELD_NAME);
        assertSpy(spy);
    }

    @Test
    @DisplayName("propagate an exception")
    void propagate_exception() {
        Spy<Number> spy = Spy.ofClass(Number.class);
        assertThrows(IllegalArgumentException.class,
                     () -> spy.on(this, FIELD_NAME));
    }

    private void assertSpy(List spy) {
        assertNotNull(spy);

        // Verify that the field is injected.
        assertSame(list, spy);

        // Check that we got a Mockito spy.
        assertEquals(3, list.size());
        verify(spy, times(1)).size();
    }
}
