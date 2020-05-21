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

import io.spine.gradle.internal.Repos
import java.net.URI

// Common build file for the tests with same configuration

buildscript {

    // NOTE: this file is copied from the root project in the test setup.
    apply(from = "$rootDir/test-env.gradle")
    apply(from = "${extra["enclosingRootDir"]}/version.gradle.kts")

    repositories {
        mavenLocal()
        jcenter()

        maven { url = java.net.URI(io.spine.gradle.internal.Repos.spine) }
    }

    dependencies {
        classpath(io.spine.gradle.internal.Deps.build.protobuf)

        // Exclude `guava:18.0` as a transitive dependency by Protobuf Gradle plugin.
        classpath(io.spine.gradle.internal.Deps.build.gradlePlugins.protobuf) {
            exclude(group = "com.google.guava")
        }
        classpath("io.spine.tools:spine-model-compiler:${spineVersion}")
    }
}

plugin {
    java
    id("com.google.protobuf")
    id("io.spine.tools.spine-model-compiler")
}

// NOTE: this file is copied from the root project in the test setup.
apply {
    from("$rootDir/test-env.gradle")
    from("${extra["enclosingRootDir"]}/config/gradle/model-compiler.gradle")
}

group = "io.spine.test"
version = "3.14"

repositories {
    mavenLocal()
    jcenter()

    maven { url = URI(Repos.spine) }
}

dependencies {
    implementation("io.spine:spine-base:$spineVersion")
}

sourceSets {
    main {
        proto.srcDir("$projectDir/src/main/proto")
        java.srcDirs("$projectDir/generated/main/java", "$projectDir/generated/main/spine")
        resources.srcDir("$projectDir/generated/main/resources")
    }
}
