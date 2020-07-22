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

import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import io.spine.query.given.RecordQueryBuilderTestEnv;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.truth.Truth.assertThat;
import static io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.isTraded;
import static io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.isin;
import static io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.whenFounded;
import static io.spine.query.given.RecordQueryBuilderTestEnv.manufacturerBuilder;
import static io.spine.query.given.RecordQueryBuilderTestEnv.manufacturerId;
import static io.spine.query.given.RecordQueryBuilderTestEnv.subjectWithNoPredicates;

@DisplayName("`RecordQueryBuilder` should")
class RecordQueryBuilderTest {

    private static final Timestamp THURSDAY = Timestamps.fromSeconds(0);

    @Nested
    @DisplayName("create `RecordQuery` instances")
    final class CreateQuery {

        @Test
        @DisplayName("with no parameters")
        void empty() {
            RecordQuery<ManufacturerId, Manufacturer> actual = manufacturerBuilder().build();
            Subject<ManufacturerId, Manufacturer> subject = subjectWithNoPredicates(actual);
            assertThat(subject.id()
                              .values()).isEmpty();
            RecordQueryBuilderTestEnv.assertNoOrderingMaskLimit(actual);
        }

        @Test
        @DisplayName("which hold the type of the queried record")
        void withRecordType() {
            RecordQuery<ManufacturerId, Manufacturer> query = manufacturerBuilder().build();
            Subject<ManufacturerId, Manufacturer> subject = query.subject();
            assertThat(subject.recordType()).isEqualTo(Manufacturer.class);
        }

        @Test
        @DisplayName("by a single identifier value")
        void byId() {
            ManufacturerId expectedId = manufacturerId();
            RecordQuery<ManufacturerId, Manufacturer> query =
                    manufacturerBuilder()
                            .id()
                            .is(expectedId)
                            .build();
            Subject<ManufacturerId, Manufacturer> subject = subjectWithNoPredicates(query);

            IdParameter<ManufacturerId> actualIdParam = subject.id();
            assertThat(actualIdParam.values()).containsExactly(expectedId);
        }

        @Test
        @DisplayName("by several identifier values")
        void bySeveralIds() {
            ImmutableSet<ManufacturerId> expectedValues =
                    IntStream.range(0, 24)
                             .mapToObj((i) -> manufacturerId())
                             .collect(toImmutableSet());
            RecordQuery<ManufacturerId, Manufacturer> query =
                    manufacturerBuilder()
                            .id()
                            .with(expectedValues)
                            .build();
            Subject<ManufacturerId, Manufacturer> subject = subjectWithNoPredicates(query);

            IdParameter<ManufacturerId> actualIdParam = subject.id();
            assertThat(actualIdParam.values()).isEqualTo(expectedValues);
        }

        @Test
        @DisplayName("by the values of several columns")
        void byColumnValues() {
            RecordQueryBuilder<ManufacturerId, Manufacturer> query =
                    manufacturerBuilder().where(isin)
                                         .isNot("JP 3633400001")
                                         .where(whenFounded)
                                         .isLessOrEqualTo(THURSDAY)
                                         .where(isTraded)
                                         .is(true);
        }

        @Test
        @DisplayName("by the value of either of the columns")
        void byEitherColumn() {
            RecordQuery<ManufacturerId, Manufacturer> builder =
                    manufacturerBuilder()
                            .either((r) -> r.where(isin)
                                            .is("JP 3899800001"),
                                    (r) -> r.where(isTraded)
                                            .is(true))
                            .where(whenFounded)
                            .isLessThan(THURSDAY)
                            .build();
        }

        @Test
        @DisplayName("with the field mask")
        void withFieldMask() {
        }

        @Test
        @DisplayName("ordered by several fields")
        void withOrdering() {
        }

        @Test
        @DisplayName("ordered by several fields with the record limit")
        void withLimitAndOrdering() {
        }

        @Test
        @DisplayName("which return the same `Builder` instance if asked")
        void returnSameBuilder() {
            RecordQueryBuilder<ManufacturerId, Manufacturer> builder =
                    manufacturerBuilder().where(whenFounded)
                                         .isGreaterThan(THURSDAY)
                                         .where(isin)
                                         .isNot("JP 49869009911")
                                         .orderBy(whenFounded,Direction.ASC)
                                         .limit(150);
            RecordQuery<ManufacturerId, Manufacturer> query = builder.build();
            RecordQueryBuilder<ManufacturerId, Manufacturer> actualBuilder = query.toBuilder();
            assertThat(actualBuilder).isSameInstanceAs(builder);
        }
    }
}
