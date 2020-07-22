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

import com.google.protobuf.Timestamp;
import io.spine.people.PersonName;
import io.spine.query.Manufacturer;
import io.spine.query.ManufacturerId;
import io.spine.query.RecordColumn;

import static io.spine.testing.TestValues.randomString;

/**
 * Test environment data for {@link io.spine.query.RecordQueryBuilderTest RecordQueryBuilderTest}.
 */
public final class RecordQueryBuilderTestEnv {

    private RecordQueryBuilderTestEnv() {
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
     * Defines the columns for {@link PersonName} message record.
     */
    public static final class PersonNameColumns {

        public static final RecordColumn<PersonName, String> honorificPrefix =
                new RecordColumn<>("honorific_prefix", String.class,
                                   PersonName::getFamilyName);

        private PersonNameColumns() {
        }
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
