/*
 * Copyright 2020, TeamDev. All rights reserved.
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

import io.spine.people.PersonName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.query.given.RecordQueryBuilderTestEnv.PersonNameColumns.honorificPrefix;
import static io.spine.query.given.RecordQueryBuilderTestEnv.manufacturerId;

@DisplayName("`RecordQueryBuilder` should")
class RecordQueryBuilderTest {

    @Nested
    @DisplayName("create `RecordQuery` instances")
    final class CreateQuery {

        @DisplayName("by a single identifier value")
        @Test
        void byId() {
            ManufacturerId expectedId = manufacturerId();
            RecordQuery<ManufacturerId, Manufacturer> query =
                    RecordQuery.<ManufacturerId, Manufacturer>newBuilder(Manufacturer.class)
                            .id()
                            .is(expectedId)
                            .build();
            assertThat(query).isNotNull();

            Subject<ManufacturerId, Manufacturer> subject = query.subject();
            assertThat(subject.predicates()).isEmpty();

            IdParameter<ManufacturerId> actualIdParam = subject.id();
            assertThat(actualIdParam.values()).containsExactly(expectedId);
        }
    }

    @Test
    @DisplayName("create `EntityQuery` instances")
    void createEntityQueries() {
        RecordQuery<Object, PersonName> query =
                RecordQuery.newBuilder(PersonName.class)
                           .where(honorificPrefix)
                           .is("Mr.")
                           .build();
    }
}
