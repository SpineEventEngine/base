/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.reflect;

import io.spine.reflect.given.MethodHolder;
import io.spine.reflect.given.ObjMethodHolder;
import kotlin.reflect.KParameter.Kind;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;

@DisplayName("``J2Kt`` should")
class J2KtTest {

    @Test
    @DisplayName("find a static method in a class")
    void findStatic() throws NoSuchMethodException {
        var name = "staticMethod";
        var method = MethodHolder.class.getDeclaredMethod(name, int.class);
        var found = J2Kt.findKotlinMethod(method);
        assertThat(found)
                .isPresent();
        var ktMethod = found.get();
        assertThat(ktMethod.getName())
                .isEqualTo(name);
        var params = ktMethod.getParameters();
        assertThat(params)
                .hasSize(2);
        assertThat(params.get(0).getKind())
                .isEqualTo(Kind.INSTANCE);
        assertThat(params.get(1).getKind())
                .isEqualTo(Kind.VALUE);
    }

    @Test
    @DisplayName("find a instance method a single argument in a class")
    void findInstance() throws NoSuchMethodException {
        var name = "instanceMethod";
        var method = MethodHolder.class.getDeclaredMethod(name, String.class);
        var found = J2Kt.findKotlinMethod(method);
        assertThat(found)
                .isPresent();
        var ktMethod = found.get();
        assertThat(ktMethod.getName())
                .isEqualTo(name);
        var params = ktMethod.getParameters();
        assertThat(params)
                .hasSize(2);
        assertThat(params.get(0).getKind())
                .isEqualTo(Kind.INSTANCE);
        assertThat(params.get(1).getKind())
                .isEqualTo(Kind.VALUE);
    }

    @Test
    @DisplayName("find a instance method with no arguments in a class")
    void findInstanceNoArg() throws NoSuchMethodException {
        var name = "noParamMethod";
        var method = MethodHolder.class.getDeclaredMethod(name);
        var found = J2Kt.findKotlinMethod(method);
        assertThat(found)
                .isPresent();
        var ktMethod = found.get();
        assertThat(ktMethod.getName())
                .isEqualTo(name);
        var params = ktMethod.getParameters();
        assertThat(params)
                .hasSize(1);
        assertThat(params.get(0).getKind())
                .isEqualTo(Kind.INSTANCE);
    }

    @Test
    @DisplayName("find a static method in an object")
    void findStaticInObj() throws NoSuchMethodException {
        var name = "staticObjMethod";
        var method = ObjMethodHolder.class.getDeclaredMethod(name);
        var found = J2Kt.findKotlinMethod(method);
        assertThat(found)
                .isPresent();
        var ktMethod = found.get();
        assertThat(ktMethod.getName())
                .isEqualTo(name);
        var params = ktMethod.getParameters();
        assertThat(params)
                .hasSize(1);
        assertThat(params.get(0).getKind())
                .isEqualTo(Kind.INSTANCE);
    }

    @Test
    @DisplayName("find a instance method in an object")
    void findInstanceInObj() throws NoSuchMethodException {
        var name = "instanceObjMethod";
        var method = ObjMethodHolder.class.getDeclaredMethod(name);
        var found = J2Kt.findKotlinMethod(method);
        assertThat(found)
                .isPresent();
        var ktMethod = found.get();
        assertThat(ktMethod.getName())
                .isEqualTo(name);
        var params = ktMethod.getParameters();
        assertThat(params)
                .hasSize(1);
        assertThat(params.get(0).getKind())
                .isEqualTo(Kind.INSTANCE);
    }
}
