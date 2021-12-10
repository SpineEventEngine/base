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

package io.spine.validate;

import com.google.common.collect.ImmutableSet;
import io.spine.test.type.Uri;
import io.spine.test.validation.AMessage;
import io.spine.test.validation.AnExternalConstraint;
import io.spine.type.MessageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

@DisplayName("`ExternalConstraints` should")
final class ExternalConstraintsTest {

    @DisplayName("update rules from types")
    @Test
    void updateRulesFromTypes() {
        var ruleType = new MessageType(AnExternalConstraint.getDescriptor());
        ExternalConstraints.updateFrom(ImmutableSet.of(ruleType));
        assertThat(ExternalConstraints.all()).hasSize(6);
    }

    @Test
    @DisplayName("tell if an external constraint is defined for a field")
    void checkIfDefined() {
        var defined = ExternalConstraints.isDefinedFor(AMessage.getDescriptor(), "field");
        assertThat(defined).isTrue();
    }

    @Test
    @DisplayName("tell if an external constraint is NOT defined for a field")
    void checkIfNotDefined() {
        var defined = ExternalConstraints.isDefinedFor(Uri.getDescriptor(), "host");
        assertThat(defined).isFalse();
    }
}
