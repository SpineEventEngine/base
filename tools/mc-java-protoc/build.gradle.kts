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

import io.spine.internal.dependency.JavaPoet
import io.spine.internal.dependency.JavaX

group = "io.spine.tools"

dependencies {
    implementation(project(":tool-base"))
    implementation(project(":plugin-base"))
    implementation(project(":mc-java-validation"))
    implementation(JavaPoet.lib)
    implementation(JavaX.annotations)

    testImplementation(project(":base"))
    testImplementation(project(":testlib"))
    testImplementation(project(":mute-logging"))
    testImplementation(project(":mc-java"))
}

//TODO:2021-07-22:alexander.yevsyukov: Turn to WARN and investigate duplicates.
// see https://github.com/SpineEventEngine/base/issues/657
val dupStrategy = DuplicatesStrategy.INCLUDE

tasks.jar {
    dependsOn(
            ":tool-base:jar",
            ":mc-java-validation:jar"
    )

    // See https://stackoverflow.com/questions/35704403/what-are-the-eclipsef-rsa-and-eclipsef-sf-in-a-java-jar-file
    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")

    manifest {
        attributes(mapOf("Main-Class" to "io.spine.tools.mc.java.protoc.Plugin"))
    }
    // Assemble "Fat-JAR" artifact containing all the dependencies.
    from(configurations.runtimeClasspath.get().map {
        when {
            it.isDirectory -> it
            else -> zipTree(it)
        }
    })
    // We should provide a classifier or else Protobuf Gradle plugin will substitute it with
    // an OS-specific one.
    archiveClassifier.set("exe")

    duplicatesStrategy = dupStrategy
}

tasks.sourceJar.get().duplicatesStrategy = dupStrategy
