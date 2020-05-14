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

import io.spine.gradle.internal.Deps
import java.nio.file.Files

group = "io.spine.tools"

dependencies {
    implementation(project(":tool-base"))
    implementation(project(":protoc-api"))
    implementation(project(":validation-generator"))
    implementation(Deps.gen.javaPoet)

    testImplementation(project(":base"))
    testImplementation(project(":testlib"))
    testImplementation(project(":mute-logging"))
    Deps.test.truth.forEach { testImplementation(it) }
}

tasks.jar {
    dependsOn(":protoc-api:jar",
              ":tool-base:jar",
              ":validation-generator:jar")

    manifest {
        attributes(mapOf("Main-Class" to "io.spine.tools.protoc.Plugin"))
    }
    // Assemble "Fat-JAR" artifact containing all the dependencies.
    from(configurations.runtimeClasspath.get().map {
        when {
            it.isDirectory() -> it
            else -> zipTree(it)
        }
    })
}

val shellRunner = injectVersion(file("plugin_runner.sh"))
val batchRunner = injectVersion(file("plugin_runner.bat"))

artifacts {
    archives(shellRunner) {
        classifier = "script"
    }
    archives(batchRunner) {
        classifier = "script"
    }
}

/**
 * Inserts the current Spine version into the given file replacing the {@code {@literal {version}}}
 * string.
 *
 * <p>This insertion point is conventional for the runner scripts for the Spine protoc plugin.
 *
 * <p>Before the runner script is published, the version must be injected into it.
 *
 * <p>The standard Grade filtering mechanism (involving the Copy task) cannot be used in this case
 * since the injection should be performed on the configuration stage.
 *
 * @param scriptFile the script file to modify
 * @return the new script file to publish
 */
fun injectVersion(scriptFile: File): File {
    var text = scriptFile.readText()
    text = text.replace("{version}", project.version as String)
    val extension = when {
        scriptFile.name.endsWith(".sh") -> ".sh"
        else -> ".bat"
    }
    val tempFile = Files.createTempFile("build", extension)
    tempFile.toFile().writeText(text)
    return project.file(tempFile.toAbsolutePath())
}
