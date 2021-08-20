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

package io.spine.query;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.NullPointerTester;
import com.google.errorprone.annotations.Immutable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.truth.Truth.assertThat;
import static io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.is_traded;
import static io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.isin;
import static io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.stock_count;

@DisplayName("`Columns` should")
class ColumnsTest {

    /**
     * Checks that {@code Columns} is immutable.
     *
     * @implNote In this test we just ensure this type is marked with {@code Immutable}.
     *         The rest is done by Error Prone.
     */
    @Test
    @DisplayName("be immutable")
    void beImmutable() {
        Annotation[] declaredAnnotations = Columns.class.getDeclaredAnnotations();
        ImmutableSet<Class<? extends Annotation>> annotationTypes =
                ImmutableSet.copyOf(declaredAnnotations)
                            .stream()
                            .map(Annotation::annotationType)
                            .collect(toImmutableSet());
        assertThat(annotationTypes)
                .contains(Immutable.class);
    }

    @Test
    @DisplayName("create new instances from the passed `RecordColumn`s")
    void createNewInstances() {
        Columns<Manufacturer> columns = Columns.of(is_traded, isin, stock_count);
        assertThat(columns)
                .containsExactly(is_traded, isin, stock_count);
    }

    @Test
    @DisplayName("not accept `null` arguments")
    void notAcceptNulls() {
        new NullPointerTester()
                .testAllPublicStaticMethods(Columns.class);
    }
}
