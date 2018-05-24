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

package io.spine.test;

import com.google.protobuf.Any;
import org.junit.Test;

import java.lang.reflect.Constructor;

import static org.junit.Assert.assertEquals;

public class ReflectiveBuilderShould {

    @Test
    public void has_result_class() {
        ReflectiveBuilder<Any> builder = new DummyBuilder();
        assertEquals(Any.class, builder.getResultClass());
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = NullPointerException.class)
    public void prohibit_null_arg() {
        new ReflectiveBuilder<Void>((null)) {
            @SuppressWarnings("ReturnOfNull")
            @Override
            protected Constructor<Void> getConstructor() {
                return null;
            }

            @Override
            public Void build() {
                return null;
            }
        };
    }

    @SuppressWarnings("ReturnOfNull") // OK for this test harness.
    private static class DummyBuilder extends ReflectiveBuilder<Any> {

        private DummyBuilder() {
            super(Any.class);
        }

        @Override
        protected Constructor<Any> getConstructor() {
            return null;
        }

        @Override
        public Any build() {
            return null;
        }
    }
}
