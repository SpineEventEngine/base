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

package io.spine.code.proto;

import com.google.common.truth.Truth8;
import com.google.protobuf.Descriptors.Descriptor;
import io.spine.option.EntityOption;
import io.spine.option.EntityOption.Kind;
import io.spine.option.EntityOption.Visibility;
import io.spine.test.code.proto.EsoEntity;
import io.spine.test.code.proto.EsoPublicProjection;
import io.spine.test.code.proto.EsoSecretAggregate;
import io.spine.test.code.proto.EsoSubscribablePm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.option.EntityOption.Kind.AGGREGATE;
import static io.spine.option.EntityOption.Kind.ENTITY;
import static io.spine.option.EntityOption.Kind.PROCESS_MANAGER;
import static io.spine.option.EntityOption.Kind.PROJECTION;
import static io.spine.option.EntityOption.Visibility.FULL;
import static io.spine.option.EntityOption.Visibility.NONE;
import static io.spine.option.EntityOption.Visibility.QUERY;
import static io.spine.option.EntityOption.Visibility.SUBSCRIBE;

@DisplayName("EntityStateOption should")
class EntityStateOptionTest {

    @Test
    @DisplayName("obtain value for an ENTITY kind")
    void entity() {
        assertOption(EsoEntity.getDescriptor(), ENTITY, QUERY);
    }

    @Test
    @DisplayName("obtain value for an AGGREGATE kind")
    void aggregate() {
        assertOption(EsoSecretAggregate.getDescriptor(), AGGREGATE, NONE);
    }

    @Test
    @DisplayName("obtain value for an PROCESS_MANAGER kind")
    void processManager() {
        assertOption(EsoSubscribablePm.getDescriptor(), PROCESS_MANAGER, SUBSCRIBE);
    }

    @Test
    @DisplayName("obtain value for an PROJECTION kind")
    void projection() {
        assertOption(EsoPublicProjection.getDescriptor(), PROJECTION, FULL);
    }

    void assertOption(Descriptor type, Kind kind, Visibility visibility) {
        Optional<EntityOption> found = EntityStateOption.valueOf(type);
        Truth8.assertThat(found).isPresent();
        @SuppressWarnings("OptionalGetWithoutIsPresent") // checked above.
        EntityOption option = found.get();
        assertThat(option.getKind())
                .isEqualTo(kind);
        assertThat(option.getVisibility())
                .isEqualTo(visibility);
    }
}
