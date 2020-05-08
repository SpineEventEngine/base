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

package io.spine.gradle.internal

import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.dsl.RepositoryHandler
import java.net.URI

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

/*
 * This file describes shared dependencies of Spine sub-projects.
 *
 * Inspired by dependency management of the Uber's NullAway project:
 *  https://github.com/uber/NullAway/blob/master/gradle/dependencies.gradle
 */

// Repositories to which we may publish. Normally, only one repository will be used.
// See `publish.gradle.kts` for details of the publishing process.

data class Repository(val releases: String,
                      val snapshots: String,
                      val credentials: String)

object PublishingRepos {
    val mavenTeamDev = Repository(
            releases = "http://maven.teamdev.com/repository/spine",
            snapshots = "http://maven.teamdev.com/repository/spine-snapshots",
            credentials = "credentials.properties"
    )
    val cloudRepo = Repository(
            releases = "https://spine.mycloudrepo.io/public/repositories/releases",
            snapshots = "https://spine.mycloudrepo.io/public/repositories/snapshots",
            credentials = "cloudrepo.properties"
    )
}

// Specific repositories.
object Repos {
    val oldSpine: String = PublishingRepos.mavenTeamDev.releases
    val oldSpineSnapshots: String = PublishingRepos.mavenTeamDev.snapshots

    val spine: String = PublishingRepos.cloudRepo.releases
    val spineSnapshots: String = PublishingRepos.cloudRepo.snapshots

    val sonatypeSnapshots: String = "https://oss.sonatype.org/content/repositories/snapshots"
    val gradlePlugins = "https://plugins.gradle.org/m2/"
}

object Versions {
    val slf4j            = "1.7.29" // deprecated, remove after full migration
    val checkerFramework = "3.3.0"
    val errorProne       = "2.3.4"
    val errorProneJavac  = "9+181-r4173-1" // taken from here: https://github.com/tbroyer/gradle-errorprone-plugin/blob/v0.8/build.gradle.kts
    val errorPronePlugin = "1.1.1"
    val pmd              = "6.20.0"
    val checkstyle       = "8.29"
    val protobufPlugin   = "0.8.12"
    val appengineApi     = "1.9.79"
    val appenginePlugin  = "2.2.0"
    val findBugs         = "3.0.2"
    val guava            = "29.0-jre"
    val protobuf         = "3.11.4"
    val grpc             = "1.28.1"
    val flogger          = "0.5.1"
    val junit4           = "4.12"
    val junit5           = "5.6.2"
    val junitPlatform    = "1.6.2"
    val junitPioneer     = "0.4.2"
    val truth            = "1.0.1"
    val httpClient       = "1.34.2"
    val apacheHttpClient = "2.1.2"
    val firebaseAdmin    = "6.12.2"
    val roaster          = "2.21.2.Final"
    val licensePlugin    = "1.13"
    val javaPoet         = "1.12.1"
    val autoService      = "1.0-rc6"
    val autoCommon       = "0.10"
    val jackson          = "2.9.10.4"
    val animalSniffer    = "1.18"
    val apiguardian      = "1.1.0"
}

object GradlePlugins {
    val errorProne      = "net.ltgt.gradle:gradle-errorprone-plugin:${Versions.errorPronePlugin}"
    val protobuf        = "com.google.protobuf:protobuf-gradle-plugin:${Versions.protobufPlugin}"
    val appengine       = "com.google.cloud.tools:appengine-gradle-plugin:${Versions.appenginePlugin}"
    val licenseReport   = "com.github.jk1:gradle-license-report:${Versions.licensePlugin}"
}

object AutoService {
    val annotations = "com.google.auto.service:auto-service-annotations:${Versions.autoService}"
    val processor = "com.google.auto.service:auto-service:${Versions.autoService}"
}

object Build {
    val errorProneJavac        = "com.google.errorprone:javac:${Versions.errorProneJavac}"
    val errorProneAnnotations = listOf(
            "com.google.errorprone:error_prone_annotations:${Versions.errorProne}",
            "com.google.errorprone:error_prone_type_annotations:${Versions.errorProne}"
    )
    val errorProneCheckApi     = "com.google.errorprone:error_prone_check_api:${Versions.errorProne}"
    val errorProneCore         = "com.google.errorprone:error_prone_core:${Versions.errorProne}"
    val errorProneTestHelpers  = "com.google.errorprone:error_prone_test_helpers:${Versions.errorProne}"
    val checkerAnnotations     = "org.checkerframework:checker-qual:${Versions.checkerFramework}"
    val checkerDataflow        = listOf(
            "org.checkerframework:dataflow:${Versions.checkerFramework}",
            "org.checkerframework:javacutil:${Versions.checkerFramework}"
    )
    val autoCommon             = "com.google.auto:auto-common:${Versions.autoCommon}"
    val autoService = AutoService
    val jsr305Annotations      = "com.google.code.findbugs:jsr305:${Versions.findBugs}"
    val guava                  = "com.google.guava:guava:${Versions.guava}"
    val flogger                = "com.google.flogger:flogger:${Versions.flogger}"
    val slf4j                  = "org.slf4j:slf4j-api:${Versions.slf4j}"
    val protobuf = listOf(
            "com.google.protobuf:protobuf-java:${Versions.protobuf}",
            "com.google.protobuf:protobuf-java-util:${Versions.protobuf}"
    )
    val protoc                 = "com.google.protobuf:protoc:${Versions.protobuf}"
    val googleHttpClient       = "com.google.http-client:google-http-client:${Versions.httpClient}"
    val googleHttpClientApache = "com.google.http-client:google-http-client-apache:${Versions.apacheHttpClient}"
    val appengineApi           = "com.google.appengine:appengine-api-1.0-sdk:${Versions.appengineApi}"
    val firebaseAdmin          = "com.google.firebase:firebase-admin:${Versions.firebaseAdmin}"
    val jacksonDatabind        = "com.fasterxml.jackson.core:jackson-databind:${Versions.jackson}"
    val roasterApi             = "org.jboss.forge.roaster:roaster-api:${Versions.roaster}"
    val roasterJdt             = "org.jboss.forge.roaster:roaster-jdt:${Versions.roaster}"
    val animalSniffer          = "org.codehaus.mojo:animal-sniffer-annotations:${Versions.animalSniffer}"
    val ci = "true".equals(System.getenv("CI"))
    val gradlePlugins = GradlePlugins
}

object Gen {
    val javaPoet = "com.squareup:javapoet:${Versions.javaPoet}"
}

object Grpc {
    val grpcCore               = "io.grpc:grpc-core:${Versions.grpc}"
    val grpcStub               = "io.grpc:grpc-stub:${Versions.grpc}"
    val grpcOkHttp             = "io.grpc:grpc-okhttp:${Versions.grpc}"
    val grpcProtobuf           = "io.grpc:grpc-protobuf:${Versions.grpc}"
    val grpcNetty              = "io.grpc:grpc-netty:${Versions.grpc}"
    val grpcNettyShaded        = "io.grpc:grpc-netty-shaded:${Versions.grpc}"
    val grpcContext            = "io.grpc:grpc-context:${Versions.grpc}"
}
 
object Runtime {
    val floggerSystemBackend = "com.google.flogger:flogger-system-backend:${Versions.flogger}"
    val floggerLog4J         = "com.google.flogger:flogger-log4j:${Versions.flogger}"
    val floggerSlf4J         = "com.google.flogger:slf4j-backend-factory:${Versions.flogger}"
}

object Test {
    val junit4        = "junit:junit:${Versions.junit4}"
    val junit5Api = listOf(
            "org.junit.jupiter:junit-jupiter-api:${Versions.junit5}",
            "org.junit.jupiter:junit-jupiter-params:${Versions.junit5}",
            "org.apiguardian:apiguardian-api:${Versions.apiguardian}"
    )
    val junit5Runner  = "org.junit.jupiter:junit-jupiter-engine:${Versions.junit5}"
    val junitPioneer  = "org.junit-pioneer:junit-pioneer:${Versions.junitPioneer}"
    val slf4j         = "org.slf4j:slf4j-jdk14:${Versions.slf4j}"
    val guavaTestlib  = "com.google.guava:guava-testlib:${Versions.guava}"
    val mockito       = "org.mockito:mockito-core:2.12.0"
    val hamcrest      = "org.hamcrest:hamcrest-all:1.3"
    val truth = listOf(
            "com.google.truth:truth:${Versions.truth}",
            "com.google.truth.extensions:truth-java8-extension:${Versions.truth}",
            "com.google.truth.extensions:truth-proto-extension:${Versions.truth}"
    )
}

object Scripts {
    fun testArtifacts(p: Project)          = "${p.rootDir}/config/gradle/test-artifacts.gradle"
    fun testOutput(p: Project)             = "${p.rootDir}/config/gradle/test-output.gradle"
    fun slowTests(p: Project)              = "${p.rootDir}/config/gradle/slow-tests.gradle"
    fun javadocOptions(p: Project)         = "${p.rootDir}/config/gradle/javadoc-options.gradle"
    fun filterInternalJavadocs(p: Project) = "${p.rootDir}/config/gradle/filter-internal-javadoc.gradle"
    fun jacoco(p: Project)                 = "${p.rootDir}/config/gradle/jacoco.gradle"
    fun publish(p: Project)                = "${p.rootDir}/config/gradle/publish.gradle"
    fun publishProto(p: Project)           = "${p.rootDir}/config/gradle/publish-proto.gradle"
    fun javacArgs(p: Project)              = "${p.rootDir}/config/gradle/javac-args.gradle"
    fun jsBuildTasks(p: Project)           = "${p.rootDir}/config/gradle/js/build-tasks.gradle"
    fun jsConfigureProto(p: Project)       = "${p.rootDir}/config/gradle/js/configure-proto.gradle"
    fun npmPublishTasks(p: Project)        = "${p.rootDir}/config/gradle/js/npm-publish-tasks.gradle"
    fun npmCli(p: Project)                 = "${p.rootDir}/config/gradle/js/npm-cli.gradle"
    fun updatePackageVersion(p: Project)   = "${p.rootDir}/config/gradle/js/update-package-version.gradle"
    fun dartBuildTasks(p: Project)         = "${p.rootDir}/config/gradle/dart/build-tasks.gradle"
    fun pubPublishTasks(p: Project)        = "${p.rootDir}/config/gradle/dart/pub-publish-tasks.gradle"
    fun pmd(p: Project)                    = "${p.rootDir}/config/gradle/pmd.gradle"
    fun checkstyle(p: Project)             = "${p.rootDir}/config/gradle/checkstyle.gradle"
    fun runBuild(p: Project)               = "${p.rootDir}/config/gradle/run-build.gradle"
    fun modelCompiler(p: Project)          = "${p.rootDir}/config/gradle/model-compiler.gradle"
    fun licenseReportCommon(p: Project)    = "${p.rootDir}/config/gradle/license-report-common.gradle"
    fun projectLicenseReport(p: Project)   = "${p.rootDir}/config/gradle/license-report-project.gradle"
    fun repoLicenseReport(p: Project)      = "${p.rootDir}/config/gradle/license-report-repo.gradle"
    fun generatePom(p: Project)            = "${p.rootDir}/config/gradle/generate-pom.gradle"
    fun updateGitHubPages(p: Project)      = "${p.rootDir}/config/gradle/update-gh-pages.gradle"
}

object Deps {
    val build = Build
    val grpc = Grpc
    val gen = Gen
    val runtime = Runtime
    val test = Test
    val versions = Versions
    val scripts = Scripts
}

object DependencyResolution {

    fun forceConfiguration(configurations: ConfigurationContainer) {
        configurations.all { config ->
            config.resolutionStrategy { strategy ->
                strategy.failOnVersionConflict()
                strategy.cacheChangingModulesFor(0, "seconds")
                strategy.force(
                        Deps.build.slf4j,
                        Deps.build.errorProneAnnotations,
                        Deps.build.jsr305Annotations,
                        Deps.build.checkerAnnotations,
                        Deps.build.autoCommon,
                        Deps.build.guava,
                        Deps.build.animalSniffer,
                        Deps.build.protobuf,
                        Deps.test.guavaTestlib,
                        Deps.test.truth,
                        Deps.test.junit5Api,
                        Deps.test.junit4,

                        // Transitive dependencies of 3rd party components that we don"t use directly.
                        "com.google.code.gson:gson:2.8.6",
                        "com.google.j2objc:j2objc-annotations:1.3",
                        "org.codehaus.plexus:plexus-utils:3.3.0",
                        "com.squareup.okio:okio:1.17.5", // Last version before next major.
                        "commons-cli:commons-cli:1.4",

                        // Force discontinued transitive dependency until everybody migrates off it.
                        "org.checkerframework:checker-compat-qual:2.5.5",

                        "commons-logging:commons-logging:1.2",

                        // Force the Gradle Protobuf plugin version.
                        Deps.build.gradlePlugins.protobuf
                )
            }
        }
    }

    fun defaultRepositories(repositories: RepositoryHandler) {
        repositories.mavenLocal()
        repositories.maven { repository ->
            repository.url = URI(Repos.spine)
            repository.content { descriptor ->
                descriptor.includeGroup("io.spine")
                descriptor.includeGroup("io.spine.tools")
                descriptor.includeGroup("io.spine.gcloud")
            }
        }
        repositories.jcenter()
        repositories.maven { repository ->
            repository.url = URI(Repos.gradlePlugins)
        }
    }
}



