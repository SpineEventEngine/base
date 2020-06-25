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
import org.junit.jupiter.api.Test;

@DisplayName("`RecordQueryBuilder` should")
public class RecordQueryBuilderTest {

    static class PersonNameColumns {

        private PersonNameColumns() {
        }

        static RecordColumn<PersonName, String> honorificPrefix() {
            return new RecordColumn<>("honorific_prefix", String.class,
                                      PersonName::getFamilyName);
        }
    }

    @Test
    @DisplayName("create complex queries for Protobuf messages")
    void createComplexQueries() {
        RecordQuery<Object, PersonName> query =
                RecordQuery.newBuilder(PersonName.class)
                           .where(PersonNameColumns.honorificPrefix())
                           .is("Mr.")
                           .build();
    }

    @Test
    @DisplayName("create `EntityQuery` instances")
    void createEntityQueries() {
        RecordQuery<Object, PersonName> query =
                RecordQuery.newBuilder(PersonName.class)
                           .where(PersonNameColumns.honorificPrefix())
                           .is("Mr.")
                           .build();
    }
}
