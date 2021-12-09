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

package io.spine.base;

import io.spine.annotation.GeneratedMixin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.google.common.truth.Truth.assertThat;

@DisplayName("GeneratedMixin annotation should")
class GeneratedMixinTest {

    @Test
    @DisplayName("have `SOURCE` retention policy")
    void retention() {
       assertThat(annotation(Retention.class).value())
               .isEqualTo(RetentionPolicy.SOURCE);
    }

    @Test
    @DisplayName("have `TYPE` target")
    void target() {
        var assertTargets = assertThat(annotation(Target.class).value()).asList();

        assertTargets.hasSize(1);
        assertTargets.contains(ElementType.TYPE);
    }

    @Test
    @DisplayName("be `Documented`")
    void documented() {
        assertThat(annotation(Documented.class)).isNotNull();
    }

    @Test
    @DisplayName("be `Inherited`")
    void inherited() {
        assertThat(annotation(Inherited.class)).isNotNull();
    }

    private static <A extends Annotation> A annotation(Class<A> annotationClass) {
        return GeneratedMixin.class.getAnnotation(annotationClass);
    }
}
