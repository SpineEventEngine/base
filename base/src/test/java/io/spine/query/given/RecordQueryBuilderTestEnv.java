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

package io.spine.query.given;

import com.google.protobuf.FieldMask;
import com.google.protobuf.Timestamp;
import io.spine.query.Manufacturer;
import io.spine.query.ManufacturerId;
import io.spine.query.RecordColumn;
import io.spine.query.RecordQuery;
import io.spine.query.RecordQueryBuilder;
import io.spine.query.Subject;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.TestValues.randomString;
import static io.spine.testing.Tests.nullRef;

/**
 * Test environment data for {@link io.spine.query.RecordQueryBuilderTest RecordQueryBuilderTest}.
 */
public final class RecordQueryBuilderTestEnv {

    private RecordQueryBuilderTestEnv() {
    }

    /**
     * Creates a new instance of the {@link RecordQueryBuilder}
     * for the {@link Manufacturer} record message.
     */
    public static RecordQueryBuilder<ManufacturerId, Manufacturer> manufacturerBuilder() {
        return RecordQuery.newBuilder(Manufacturer.class);
    }

    /**
     * Generates a random {@link ManufacturerId}.
     */
    public static ManufacturerId manufacturerId() {
        return ManufacturerId.newBuilder()
                             .setUuid(randomString())
                             .build();
    }

    /**
     * Asserts that the given query has no ordering, field mask and limit parameters set.
     */
    public static void assertNoOrderingMaskLimit(RecordQuery<ManufacturerId, Manufacturer> query) {
        assertThat(query.ordering()).isEmpty();
        assertThat(query.mask()).isEqualTo(FieldMask.getDefaultInstance());
        assertThat(query.limit()).isEqualTo(nullRef());
    }

    /**
     * Checks that the query is not {@code null} as well as has no predicates and returns it.
     */
    public static Subject<ManufacturerId, Manufacturer>
    subjectWithNoPredicates(RecordQuery<ManufacturerId, Manufacturer> query) {
        assertThat(query).isNotNull();
        Subject<ManufacturerId, Manufacturer> subject = query.subject();
        assertThat(subject.predicates()).isEmpty();
        return subject;
    }

    /**
     * Defines the columns for {@link Manufacturer} message record.
     */
    public static final class ManufacturerColumns {

        public static final RecordColumn<Manufacturer, String> isin =
                new RecordColumn<>("isin", String.class, (r) -> r.getIsin()
                                                                 .getValue());

        public static final RecordColumn<Manufacturer, Timestamp> whenFounded =
                new RecordColumn<>("when_founded", Timestamp.class, Manufacturer::getWhenFounded);

        public static final RecordColumn<Manufacturer, Boolean> isTraded =
                new RecordColumn<>("is_traded",
                                   Boolean.class,
                                   (r) -> !r.getStockSymbolList()
                                            .isEmpty());

        private ManufacturerColumns() {
        }
    }
}
