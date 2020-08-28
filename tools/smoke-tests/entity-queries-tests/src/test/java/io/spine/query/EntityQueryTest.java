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

import io.spine.query.given.EntityQueryTestEnv;
import io.spine.tools.query.ProjectId;
import io.spine.tools.query.ProjectView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

@DisplayName("`EntityQuery` should")
final class EntityQueryTest {

    @Test
    @DisplayName("allow to copy itself")
    void copyToEntityQuery() {
        ProjectView.Query original = EntityQueryTestEnv.givenQuery();
        ProjectView.QueryBuilder destinationBuilder = ProjectView.newQuery();
        original.copyTo(destinationBuilder);

        ProjectView.Query copy = destinationBuilder.build();
        assertThat(copy).isEqualTo(original);
    }
    @Test
    @DisplayName("allow the conversion to a `RecordQuery`")
    void convertToRecordQuery() {
        ProjectView.Query original = EntityQueryTestEnv.givenQuery();
        RecordQuery<ProjectId, ProjectView> copy = original.toRecordQuery();

        assertThat(copy.subject()).isEqualTo(original.subject());
        assertThat(copy.mask()).isEqualTo(original.mask());
        assertThat(copy.limit()).isEqualTo(original.limit());
        assertThat(copy.ordering()).isEqualTo(original.ordering());
    }
}
