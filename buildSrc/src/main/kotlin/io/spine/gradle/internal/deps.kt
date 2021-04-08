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

package io.spine.gradle.internal

import java.net.URI
import java.io.File
import java.util.*
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.dsl.RepositoryHandler

/*
 * This file describes shared dependencies of Spine sub-projects.
 *
 * Inspired by dependency management of the Uber's NullAway project:
 *  https://github.com/uber/NullAway/blob/master/gradle/dependencies.gradle
 */

/**
 * A Maven repository.
 */
data class Repository(
    val releases: String,
    val snapshots: String,
    private val credentialsFile: String? = null,
    private val credentials: Credentials? = null,
    val name: String = "Maven repository `$releases`"
) {

    /**
     * Obtains the publishing password credentials to this repository.
     *
     * If the credentials are represented by a `.properties` file, reads the file and parses
     * the credentials. The file must have properties `user.name` and `user.password`, which store
     * the username and the password for the Maven repository auth.
     */
    fun credentials(project: Project): Credentials? {
        if (credentials != null) {
            return credentials
        }
        credentialsFile!!
        val log = project.logger
        log.info("Using credentials from `$credentialsFile`.")
        val file = project.rootProject.file(credentialsFile)
        if (!file.exists()) {
            return null
        }
        val creds = file.readCredentials()
        log.info("Publishing build as `${creds.username}`.")
        return creds
    }

    private fun File.readCredentials(): Credentials {
        val properties = Properties()
        properties.load(inputStream())
        val username = properties.getProperty("user.name")
        val password = properties.getProperty("user.password")
        return Credentials(username, password)
    }

    override fun toString(): String {
        return name
    }
}

/**
 * Password credentials for a Maven repository.
 */
data class Credentials(
    val username: String?,
    val password: String?
)

/**
 * Repositories to which we may publish. Normally, only one repository will be used.
 *
 * See `publish.gradle` for details of the publishing process.
 */
object PublishingRepos {

    @Suppress("HttpUrlsUsage") // HTTPS is not supported by this repository.
    val mavenTeamDev = Repository(
        name = "maven.teamdev.com",
        releases = "http://maven.teamdev.com/repository/spine",
        snapshots = "http://maven.teamdev.com/repository/spine-snapshots",
        credentialsFile = "credentials.properties"
    )
    val cloudRepo = Repository(
        name = "CloudRepo",
        releases = "https://spine.mycloudrepo.io/public/repositories/releases",
        snapshots = "https://spine.mycloudrepo.io/public/repositories/snapshots",
        credentialsFile = "cloudrepo.properties"
    )

    fun gitHub(repoName: String): Repository {
        return Repository(
            name = "GitHub Packages",
            releases = "https://maven.pkg.github.com/SpineEventEngine/$repoName",
            snapshots = "https://maven.pkg.github.com/SpineEventEngine/$repoName",
            credentials = Credentials(
                username = System.getenv("GITHUB_ACTOR"),
                // This is a trick. Gradle only supports password or AWS credentials. Thus,
                // we pass the GitHub token as a "password".
                // https://docs.github.com/en/actions/guides/publishing-java-packages-with-gradle#publishing-packages-to-github-packages
                password = System.getenv("GITHUB_TOKEN")
            )
        )
    }
}

// Specific repositories.
@Suppress("unused")
object Repos {
    val oldSpine: String = PublishingRepos.mavenTeamDev.releases
    val oldSpineSnapshots: String = PublishingRepos.mavenTeamDev.snapshots

    val spine: String = PublishingRepos.cloudRepo.releases
    val spineSnapshots: String = PublishingRepos.cloudRepo.snapshots

    const val sonatypeSnapshots: String = "https://oss.sonatype.org/content/repositories/snapshots"
    const val gradlePlugins = "https://plugins.gradle.org/m2/"
}

/**
 * Versions of one-line dependencies.
 *
 * For versions of other dependencies please see `version` properties of objects declared below.
 *
 * See also: https://github.com/SpineEventEngine/config/issues/171
 */

// https://www.mojohaus.org/animal-sniffer/animal-sniffer-maven-plugin/
object AnimalSniffer {
    private const val version = "1.19"
    const val lib = "org.codehaus.mojo:animal-sniffer-annotations:${version}"
}

/**
 * Assertion library for tests in Kotlin
 *
 * [AssertK](https://github.com/willowtreeapps/assertk)
 */
object AssertK {
    private const val version = "0.23.1"
    const val libJvm = "com.willowtreeapps.assertk:assertk-jvm:${version}"
}

// https://github.com/google/auto
object AutoCommon {
    private const val version = "1.0"
    const val lib = "com.google.auto:auto-common:${version}"
}

// https://github.com/google/auto
object AutoValue {
    private const val version = "1.8"
    const val annotations = "com.google.auto.value:auto-value-annotations:${version}"
}

// https://github.com/google/auto
object AutoService {
    private const val version = "1.0"
    const val annotations = "com.google.auto.service:auto-service-annotations:${version}"
    const val processor   = "com.google.auto.service:auto-service:${version}"
}

// https://cloud.google.com/java/docs/reference
object AppEngine {
    private const val version = "1.9.82"
    private const val gradlePluginVersion = "2.2.0"

    const val sdk          = "com.google.appengine:appengine-api-1.0-sdk:${version}"
    const val gradlePlugin = "com.google.cloud.tools:appengine-gradle-plugin:${gradlePluginVersion}"
}

// https://www.bouncycastle.org/java.html
object BouncyCastle {
    const val libPkcsJdk15 = "org.bouncycastle:bcpkix-jdk15on:1.68"
}

// https://checkerframework.org/
object CheckerFramework {
    private const val version = "3.12.0"
    const val annotations = "org.checkerframework:checker-qual:${version}"
    @Suppress("unused")
    val dataflow = listOf(
        "org.checkerframework:dataflow:${version}",
        "org.checkerframework:javacutil:${version}"
    )
    /**
     * This is discontinued artifact, which we do not use directly.
     * This is a transitive dependency for us, which we force in
     * [DependencyResolution.forceConfiguration]
     */
    const val compatQual = "org.checkerframework:checker-compat-qual:2.5.5"
}

// https://checkstyle.sourceforge.io/
// See `config/gradle.checkstyle.gradle`.
@Suppress("unused")
object CheckStyle {
    const val version = "8.29"
}

/**
 * Commons CLI is a transitive dependency which we don't use directly.
 * We `force` it in [DependencyResolution.forceConfiguration].
 *
 * [Commons CLI]](https://commons.apache.org/proper/commons-cli/)
 */
object CommonsCli {
    private const val version = "1.4"
    const val lib = "commons-cli:commons-cli:${version}"
}

/**
 * Commons Logging is a transitive dependency which we don't use directly.
 * We `force` it in [DependencyResolution.forceConfiguration].
 *
 * [Commons Logging](https://commons.apache.org/proper/commons-logging/)
 */
object CommonsLogging {
    private const val version = "1.2"
    const val lib = "commons-logging:commons-logging:${version}"
}

// https://errorprone.info/
object ErrorProne {
    private const val version = "2.6.0"
    @Suppress("MemberVisibilityCanBePrivate")
    const val gradlePluginVersion = "1.3.0"
    // https://github.com/tbroyer/gradle-errorprone-plugin/blob/v0.8/build.gradle.kts
    private const val javacPluginVersion = "9+181-r4173-1"

    val annotations = listOf(
        "com.google.errorprone:error_prone_annotations:${version}",
        "com.google.errorprone:error_prone_type_annotations:${version}"
    )
    const val core = "com.google.errorprone:error_prone_core:${version}"
    @Suppress("unused")
    const val checkApi = "com.google.errorprone:error_prone_check_api:${version}"
    const val testHelpers = "com.google.errorprone:error_prone_test_helpers:${version}"

    const val javacPlugin  = "com.google.errorprone:javac:${javacPluginVersion}"
    const val gradlePlugin = "net.ltgt.gradle:gradle-errorprone-plugin:${gradlePluginVersion}"
}

/**
 * The FindBugs project is dead since 2017. It has a successor called SpotBugs, but we don't use it.
 * We use ErrorProne for static analysis instead. The only reason for having this dependency is
 * the annotations for null-checking introduced by JSR-305. These annotations are troublesome,
 * but no alternatives are known for some of them so far.  Please see
 * [this issue](https://github.com/SpineEventEngine/base/issues/108) for more details.
 */
object FindBugs {
    private const val version = "3.0.2"
    const val annotations = "com.google.code.findbugs:jsr305:${version}"
}

// https://firebase.google.com/docs/admin/setup#java
object Firebase {
    private const val adminVersion = "6.12.2"
    const val admin = "com.google.firebase:firebase-admin:${adminVersion}"
}

// https://github.com/google/flogger
object Flogger {
    internal const val version = "0.6"
    const val lib     = "com.google.flogger:flogger:${version}"
    @Suppress("unused")
    object Runtime {
        const val systemBackend = "com.google.flogger:flogger-system-backend:${version}"
        const val log4J         = "com.google.flogger:flogger-log4j:${version}"
        const val slf4J         = "com.google.flogger:slf4j-backend-factory:${version}"
    }
}

// https://github.com/google/guava
object Guava {
    private const val version = "30.1.1-jre"
    const val lib     = "com.google.guava:guava:${version}"
    const val testLib = "com.google.guava:guava-testlib:${version}"
}

// https://github.com/grpc/grpc-java
@Suppress("unused")
object Grpc {
    @Suppress("MemberVisibilityCanBePrivate")
    const val version     = "1.35.1"
    const val core        = "io.grpc:grpc-core:${version}"
    const val stub        = "io.grpc:grpc-stub:${version}"
    const val okHttp      = "io.grpc:grpc-okhttp:${version}"
    const val protobuf    = "io.grpc:grpc-protobuf:${version}"
    const val netty       = "io.grpc:grpc-netty:${version}"
    const val nettyShaded = "io.grpc:grpc-netty-shaded:${version}"
    const val context     = "io.grpc:grpc-context:${version}"
}

/**
 * Gson is a transitive dependency which we don't use directly.
 * We `force` it in [DependencyResolution.forceConfiguration()].
 *
 * [Gson](https://github.com/google/gson)
 */
object Gson {
    private const val version = "2.8.6"
    const val lib = "com.google.code.gson:gson:${version}"
}

/**
 * Google implementations of HTTP client.
 */
object HttpClient {
    const val google = "com.google.http-client:google-http-client:1.39.1"
    const val apache = "com.google.http-client:google-http-client-apache:2.1.2"
}

/**
 * J2ObjC is a transitive dependency which we don't use directly.
 * We `force` it in [DependencyResolution.forceConfiguration()].
 *
 * [J2ObjC](https://developers.google.com/j2objc)
 */
object J2ObjC {
    private const val version = "1.3"
    const val lib = "com.google.j2objc:j2objc-annotations:${version}"
}

// https://github.com/FasterXML/jackson-databind
object Jackson {
    private const val version = "2.9.10.5"
    const val databind = "com.fasterxml.jackson.core:jackson-databind:${version}"
}

// https://github.com/square/javapoet
object JavaPoet {
    private const val version = "1.13.0"
    const val lib = "com.squareup:javapoet:${version}"
}

// This artifact which used to be a part of J2EE moved under Eclipse EE4J project.
// https://github.com/eclipse-ee4j/common-annotations-api
object JavaX {
    const val annotations = "javax.annotation:javax.annotation-api:1.3.2"
}

// https://junit.org/junit5/
object JUnit {
    private const val version            = "5.7.1"
    private const val platformVersion    = "1.7.1"
    private const val legacyVersion      = "4.13.1"

    // https://github.com/apiguardian-team/apiguardian
    private const val apiGuardianVersion = "1.1.1"
    // https://github.com/junit-pioneer/junit-pioneer
    private const val pioneerVersion     = "1.3.8"

    const val legacy = "junit:junit:${legacyVersion}"
    val api = listOf(
        "org.apiguardian:apiguardian-api:${apiGuardianVersion}",
        "org.junit.jupiter:junit-jupiter-api:${version}",
        "org.junit.jupiter:junit-jupiter-params:${version}"
    )
    const val runner  = "org.junit.jupiter:junit-jupiter-engine:${version}"
    const val pioneer = "org.junit-pioneer:junit-pioneer:${pioneerVersion}"
    const val platformCommons = "org.junit.platform:junit-platform-commons:${platformVersion}"
    const val platformLauncher = "org.junit.platform:junit-platform-launcher:${platformVersion}"
}

/**
 * A JSON parser in Kotlin
 *
 * [Klaxon](https://github.com/cbeust/klaxon)
 */
object Klaxon {
    private const val version = "5.4"
    const val lib = "com.beust:klaxon:${version}"
}

// https://github.com/JetBrains/kotlin
// https://github.com/Kotlin
object Kotlin {
    @Suppress("MemberVisibilityCanBePrivate") // used directly from outside
    const val version      = "1.5.0-M2"
    const val reflect      = "org.jetbrains.kotlin:kotlin-reflect:${version}"
    const val stdLib       = "org.jetbrains.kotlin:kotlin-stdlib:${version}"
    const val stdLibCommon = "org.jetbrains.kotlin:kotlin-stdlib-common:${version}"
    const val stdLibJdk8   = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${version}"
}

// https://github.com/jk1/Gradle-License-Report
object LicenseReport {
    private const val version = "1.16"
    const val gradlePlugin = "com.github.jk1:gradle-license-report:${version}"
}

/**
 * A Java implementation of JSON Web Token (JWT) - RFC 7519.
 *
 * [Java JWT](https://github.com/auth0/java-jwt)
 */
object JavaJwt {
    private const val version = "3.14.0"
    const val lib = "com.auth0:java-jwt:${version}"
}

/**
 * Okio is a transitive dependency which we don't use directly.
 * We `force` it in [DependencyResolution.forceConfiguration].
 */
object Okio {
    // This is the last version before next major.
    private const val version = "1.17.5"
    const val lib = "com.squareup.okio:okio:${version}"
}

// https://pmd.github.io/
@Suppress("unused") // Will be used when `config/gradle/pmd.gradle` migrates to Kotlin.
object Pmd {
    const val version = "6.33.0"
}

// https://github.com/protocolbuffers/protobuf
@Suppress("MemberVisibilityCanBePrivate") // used directly from outside
object Protobuf {
    const val version    = "3.15.7"
    const val gradlePluginVersion = "0.8.13"
    val libs = listOf(
        "com.google.protobuf:protobuf-java:${version}",
        "com.google.protobuf:protobuf-java-util:${version}"
    )
    const val compiler = "com.google.protobuf:protoc:${version}"
    const val gradlePlugin = "com.google.protobuf:protobuf-gradle-plugin:${gradlePluginVersion}"
}

/**
 * Plexus Utils is a transitive dependency which we don't use directly.
 * We `force` it in [DependencyResolution.forceConfiguration].
 *
 * [Plexus Utils](https://codehaus-plexus.github.io/plexus-utils/)
 */
object Plexus {
    private const val version = "3.3.0"
    const val utils = "org.codehaus.plexus:plexus-utils:${version}"
}

// https://github.com/forge/roaster
object Roaster {
    private const val version = "2.22.2.Final"
    const val api = "org.jboss.forge.roaster:roaster-api:${version}"
    const val jdt = "org.jboss.forge.roaster:roaster-jdt:${version}"
}

/**
 * Spine used to log with SLF4J. Now we use Flogger. Whenever a choice comes up, we recommend to
 * use the latter.
 *
 * Some third-party libraries may clash with different versions of the library. Thus, we specify
 * this version and force it via [forceConfiguration(..)][DependencyResolution.forceConfiguration].
 */
@Deprecated("Use Flogger over SLF4J.", replaceWith = ReplaceWith("flogger"))
object Slf4J {
    private const val version = "1.7.30"
    const val lib = "org.slf4j:slf4j-api:${version}"
    const val jdk14 = "org.slf4j:slf4j-jdk14:${version}"
}

// https://github.com/google/truth
object Truth {
    private const val version = "1.1.2"
    val libs = listOf(
        "com.google.truth:truth:${version}",
        "com.google.truth.extensions:truth-java8-extension:${version}",
        "com.google.truth.extensions:truth-proto-extension:${version}"
    )
}

/*
 * Objects below gather dependencies declared above into the groups by purpose.
 */

object GradlePlugins {
    const val errorProne  = ErrorProne.gradlePlugin
    const val protobuf    = Protobuf.gradlePlugin
    const val appengine   = AppEngine.gradlePlugin
    @Suppress("unused")
    val licenseReport = LicenseReport.gradlePlugin
}

@Suppress("unused")
object Build {
    const val animalSniffer = AnimalSniffer.lib
    const val autoCommon = AutoCommon.lib
    val autoService = AutoService
    const val appEngine = AppEngine.sdk
    val checker = CheckerFramework
    val errorProne = ErrorProne
    const val firebaseAdmin = Firebase.admin
    val flogger = Flogger
    val guava = Guava
    const val googleHttpClient = HttpClient.google
    const val googleHttpClientApache = HttpClient.apache
    val gradlePlugins = GradlePlugins
    const val jacksonDatabind = Jackson.databind
    const val jsr305Annotations = FindBugs.annotations
    val kotlin = Kotlin
    val protobuf = Protobuf
    val roaster = Roaster

    val ci = "true".equals(System.getenv("CI"))

    @Deprecated("Use Flogger over SLF4J.", replaceWith = ReplaceWith("flogger"))
    @Suppress("DEPRECATION")
    val slf4j = Slf4J.lib
}

object Gen {
    const val javaPoet = JavaPoet.lib
    const val javaxAnnotation = JavaX.annotations
}

@Suppress("unused")
object Publishing {
    const val klaxon = Klaxon.lib
    const val oauthJwt = JavaJwt.lib
    const val bouncyCastlePkcs = BouncyCastle.libPkcsJdk15
    const val assertK = AssertK.libJvm
}

object Runtime {
    val flogger = Flogger.Runtime
}

object Test {
    const val guavaTestlib = Guava.testLib
    val truth = Truth

    val junit = JUnit
    const val junit4 = JUnit.legacy

    @Deprecated("Please do not use.")
    const val mockito = "org.mockito:mockito-core:2.12.0"

    @Deprecated("Please use Google Truth instead")
    const val hamcrest = "org.hamcrest:hamcrest-all:1.3"

    @Deprecated(
        "Use Flogger over SLF4J.",
        replaceWith = ReplaceWith("Deps.runtime.floggerSystemBackend")
    )
    @Suppress("DEPRECATION")
    const val slf4j = Slf4J.jdk14
}

@Suppress("unused")
object Scripts {
    private const val commonPath = "/config/gradle/"

    fun testArtifacts(p: Project)          = p.script("test-artifacts.gradle")
    fun testOutput(p: Project)             = p.script("test-output.gradle")
    fun slowTests(p: Project)              = p.script("slow-tests.gradle")
    fun javadocOptions(p: Project)         = p.script("javadoc-options.gradle")
    fun filterInternalJavadocs(p: Project) = p.script("filter-internal-javadoc.gradle")
    fun jacoco(p: Project)                 = p.script("jacoco.gradle")
    fun publish(p: Project)                = p.script("publish.gradle")
    fun publishProto(p: Project)           = p.script("publish-proto.gradle")
    fun javacArgs(p: Project)              = p.script("javac-args.gradle")
    fun jsBuildTasks(p: Project)           = p.script("js/build-tasks.gradle")
    fun jsConfigureProto(p: Project)       = p.script("js/configure-proto.gradle")
    fun npmPublishTasks(p: Project)        = p.script("js/npm-publish-tasks.gradle")
    fun npmCli(p: Project)                 = p.script("js/npm-cli.gradle")
    fun updatePackageVersion(p: Project)   = p.script("js/update-package-version.gradle")
    fun dartBuildTasks(p: Project)         = p.script("dart/build-tasks.gradle")
    fun pubPublishTasks(p: Project)        = p.script("dart/pub-publish-tasks.gradle")
    fun pmd(p: Project)                    = p.script("pmd.gradle")
    fun checkstyle(p: Project)             = p.script("checkstyle.gradle")
    fun runBuild(p: Project)               = p.script("run-build.gradle")
    fun modelCompiler(p: Project)          = p.script("model-compiler.gradle")
    fun licenseReportCommon(p: Project)    = p.script("license-report-common.gradle")
    fun projectLicenseReport(p: Project)   = p.script("license-report-project.gradle")
    fun repoLicenseReport(p: Project)      = p.script("license-report-repo.gradle")
    fun generatePom(p: Project)            = p.script("generate-pom.gradle")
    fun updateGitHubPages(p: Project)      = p.script("update-gh-pages.gradle")

    private fun Project.script(name: String) = "${rootDir}${commonPath}${name}"
}

object Deps {
    val build = Build
    val grpc = Grpc
    val gen = Gen
    val runtime = Runtime
    val test = Test
    val scripts = Scripts
    val publishing = Publishing
}

object DependencyResolution {

    fun forceConfiguration(configurations: ConfigurationContainer) {
        configurations.all {
            resolutionStrategy {
                failOnVersionConflict()
                cacheChangingModulesFor(0, "seconds")

                @Suppress("DEPRECATION") // Force SLF4J version.
                Deps.build.apply {
                    force(
                        animalSniffer,
                        autoCommon,
                        autoService.annotations,
                        checker.annotations,
                        errorProne.annotations,
                        guava.lib,
                        jsr305Annotations,
                        kotlin.reflect,
                        kotlin.stdLib,
                        kotlin.stdLibCommon,
                        kotlin.stdLibJdk8,
                        protobuf.libs,
                        protobuf.gradlePlugin,
                        slf4j
                    )
                }

                Deps.test.apply {
                    force(
                        guavaTestlib,
                        junit.api,
                        junit.platformCommons,
                        junit.platformLauncher,
                        junit4,
                        truth.libs
                    )
                }

                // Force transitive dependencies of 3rd party components that we don't use directly.
                force(
                    AutoValue.annotations,
                    Gson.lib,
                    J2ObjC.lib,
                    Plexus.utils,
                    Okio.lib,
                    CommonsCli.lib,
                    CheckerFramework.compatQual,
                    CommonsLogging.lib
                )
            }
        }
    }

    fun excludeProtobufLite(configurations: ConfigurationContainer) {
        excludeProtoLite(configurations, "runtime")
        excludeProtoLite(configurations, "testRuntime")
    }

    private fun excludeProtoLite(
        configurations: ConfigurationContainer,
        configurationName: String
    ) {
        configurations
            .named(configurationName).get()
            .exclude(mapOf("group" to "com.google.protobuf", "module" to "protobuf-lite"))
    }

    fun defaultRepositories(repositories: RepositoryHandler) {
        repositories.mavenLocal()
        repositories.maven {
            url = URI(Repos.spine)
            content {
                includeGroup("io.spine")
                includeGroup("io.spine.tools")
                includeGroup("io.spine.gcloud")
            }
        }
        repositories.maven {
            url = URI(Repos.spineSnapshots)
            content {
                includeGroup("io.spine")
                includeGroup("io.spine.tools")
                includeGroup("io.spine.gcloud")
            }
        }
        repositories.mavenCentral()
        repositories.maven {
            url = URI(Repos.gradlePlugins)
        }
    }
}
