/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.tools.gradle;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import com.google.common.truth.Truth;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.tools.gradle.given.ThirdPartyDependencyTestEnv.BASE;
import static io.spine.tools.gradle.given.ThirdPartyDependencyTestEnv.EXAMPLE;
import static io.spine.tools.gradle.given.ThirdPartyDependencyTestEnv.EXAMPLE_GROUP;
import static io.spine.tools.gradle.given.ThirdPartyDependencyTestEnv.SPINE_GROUP;
import static java.lang.String.format;

class ThirdPartyDependencyTest {

    @Test
    @DisplayName("not accept nulls on construction")
    void nulls() {
        new NullPointerTester()
                .testAllPublicConstructors(ThirdPartyDependency.class);
    }

    @Test
    @DisplayName("provide equals(..) and hashCode()")
    void equality() {
        new EqualsTester()
                .addEqualityGroup(new ThirdPartyDependency(EXAMPLE_GROUP, EXAMPLE),
                                  new ThirdPartyDependency(EXAMPLE_GROUP, EXAMPLE))
                .addEqualityGroup(new ThirdPartyDependency(SPINE_GROUP, BASE))
                .addEqualityGroup(new ThirdPartyDependency(EXAMPLE_GROUP, BASE))
                .addEqualityGroup(new ThirdPartyDependency(SPINE_GROUP, EXAMPLE))
                .testEquals();
    }

    @Test
    @DisplayName("create artifact with the given version")
    void createArtifacts() {
        String version = "42.0";
        Dependency dependency = new ThirdPartyDependency(EXAMPLE_GROUP, EXAMPLE);
        Artifact artifact = dependency.ofVersion(version);
        String expectedArtifactNotation = format("%s:%s:%s", EXAMPLE_GROUP, EXAMPLE, version);
        Truth.assertThat(artifact.notation()).isEqualTo(expectedArtifactNotation);
    }
}
