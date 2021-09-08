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

package io.spine.internal.gradle

import io.spine.internal.gradle.PublishingRepos.gitHub
import java.io.File
import java.net.URI
import java.util.*
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.kotlin.dsl.apply

/**
 * A Maven repository.
 */
data class Repository(
    val releases: String,
    val snapshots: String,
    private val credentialsFile: String? = null,
    private val credentialValues: ((Project) -> Credentials?)? = null,
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
        if (credentialValues != null) {
            return credentialValues.invoke(project)
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

    private const val CLOUD_ARTIFACT_REGISTRY = "artifactregistry://europe-maven.pkg.dev/spine-event-engine"

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

    val cloudArtifactRegistry = Repository(
        releases = "$CLOUD_ARTIFACT_REGISTRY/releases",
        snapshots = "$CLOUD_ARTIFACT_REGISTRY/snapshots",
        credentialValues = this::setUpGooglePlugin
    )

    private fun setUpGooglePlugin(p: Project): Credentials {
        p.rootProject.allprojects {
            apply(plugin = "com.google.cloud.artifactregistry.gradle-plugin")
        }
        return Credentials("", "")
//        return try {
//            val googleCreds = DefaultCredentialProvider()
//            val creds = googleCreds.credential as GoogleCredentials
//            creds.refreshIfExpired()
//            Credentials("oauth2accesstoken", creds.accessToken.tokenValue)
//        } catch (e: IOException) {
//            p.logger.info("Unable to fetch credentials for Google Cloud Artifact Registry." +
//                    " Reason: '${e.message}'." +
//                    " See debug output for details.")
//            null
//        }
    }

    fun gitHub(repoName: String): Repository {
        var githubActor: String? = System.getenv("GITHUB_ACTOR")
        githubActor = if (githubActor.isNullOrEmpty()) {
            "developers@spine.io"
        } else {
            githubActor
        }

        return Repository(
            name = "GitHub Packages",
            releases = "https://maven.pkg.github.com/SpineEventEngine/$repoName",
            snapshots = "https://maven.pkg.github.com/SpineEventEngine/$repoName",
            credentialValues = { project ->
                Credentials(
                    username = githubActor,
                    // This is a trick. Gradle only supports password or AWS credentials. Thus,
                    // we pass the GitHub token as a "password".
                    // https://docs.github.com/en/actions/guides/publishing-java-packages-with-gradle#publishing-packages-to-github-packages
                    password = readGitHubToken(project)
                )
            }
        )
    }

    private fun readGitHubToken(project: Project): String {
        val githubToken: String? = System.getenv("GITHUB_TOKEN")
        return if (githubToken.isNullOrEmpty()) {
            // Use the personal access token for the `developers@spine.io` account.
            // Only has the permission to read public GitHub packages.
            val targetDir = "${project.buildDir}/token"
            project.file(targetDir).mkdirs()
            val fileToUnzip = "${project.rootDir}/buildSrc/aus.weis"

            project.logger.info("GitHub Packages: reading token " +
                    "by unzipping `$fileToUnzip` into `$targetDir`.")
            project.exec {
                // Unzip with password "123", allow overriding, quietly,
                // into the target dir, the given archive.
                commandLine("unzip", "-P", "123", "-oq", "-d", targetDir, fileToUnzip)
            }
            val file = project.file("$targetDir/token.txt")
            file.readText()
        } else {
            githubToken
        }
    }
}

/**
 * Defines names of additional repositories commonly used in the framework projects.
 *
 * @see [applyStandard]
 */
@Suppress("unused")
object Repos {
    val oldSpine = PublishingRepos.mavenTeamDev.releases
    val oldSpineSnapshots = PublishingRepos.mavenTeamDev.snapshots

    val spine = PublishingRepos.cloudRepo.releases
    val spineSnapshots = PublishingRepos.cloudRepo.snapshots

    val cloudArchive = PublishingRepos.cloudArtifactRegistry.releases.replace("artifactregistry://", "https://")
    val cloudArchiveSnapshots = PublishingRepos.cloudArtifactRegistry.snapshots.replace("artifactregistry://", "https://")

    @Deprecated(
        message = "Sonatype release repository redirects to the Maven Central",
        replaceWith = ReplaceWith("sonatypeSnapshots"),
        level = DeprecationLevel.ERROR
    )
    const val sonatypeReleases = "https://oss.sonatype.org/content/repositories/snapshots"
    const val sonatypeSnapshots = "https://oss.sonatype.org/content/repositories/snapshots"
}

/**
 * Registers the standard set of Maven repositories.
 *
 * To be used in `buildscript` clauses when a fully-qualified call must be made.
 */
@Suppress("unused")
fun doApplyStandard(repositories: RepositoryHandler) {
    repositories.applyStandard()
}

/**
 * Registers the selected GitHub Packages repos as Maven repositories.
 *
 * To be used in `buildscript` clauses when a fully-qualified call must be made.
 *
 * @see applyGitHubPackages
 */
@Suppress("unused")
fun doApplyGitHubPackages(repositories: RepositoryHandler, project: Project) {
    repositories.applyGitHubPackages(project)
}

/**
 * Applies the repositories hosted at GitHub Packages, to which Spine artifacts were published.
 *
 * This method should be used by those wishing to have Spine artifacts published
 * to GitHub Packages as dependencies.
 */
fun RepositoryHandler.applyGitHubPackages(project: Project) {
    val baseTypes = gitHub("base-types")
    val gprCreds = baseTypes.credentials(project)

    gprCreds?.let {
        spineMavenRepo(it, baseTypes.releases)
        spineMavenRepo(it, baseTypes.snapshots)
    }
}

/**
 * Applies repositories commonly used by Spine Event Engine projects.
 *
 * Does not include the repositories hosted at GitHub Packages.
 *
 * @see applyGitHubPackages
 */
@Suppress("unused")
fun RepositoryHandler.applyStandard() {

    gradlePluginPortal()
    mavenLocal()

    val spineRepos = listOf(
        Repos.spine,
        Repos.spineSnapshots,
        Repos.cloudArchive,
        Repos.cloudArchiveSnapshots
    )

    spineRepos
        .map { URI(it) }
        .forEach {
            maven {
                url = it
                includeSpineOnly()
            }
        }

    mavenCentral()
    maven {
        url = URI(Repos.sonatypeSnapshots)
    }
}

/**
 * Registers the Maven repository with the passed [repoCredentials] for authorization.
 *
 * Only includes the Spine-related artifact groups.
 */
private fun RepositoryHandler.spineMavenRepo(
    repoCredentials: Credentials,
    repoUrl: String
) {
    maven {
        url = URI(repoUrl)
        includeSpineOnly()
        credentials {
            username = repoCredentials.username
            password = repoCredentials.password
        }
    }
}

/**
 * Narrows down the search for this repository to Spine-related artifact groups.
 */
private fun MavenArtifactRepository.includeSpineOnly() {
    content {
        includeGroupByRegex("io\\.spine.*")
    }
}
