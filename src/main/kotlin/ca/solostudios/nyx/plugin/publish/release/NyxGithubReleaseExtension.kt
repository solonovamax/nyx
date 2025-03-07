/*
 * Copyright (c) 2024-2025 solonovamax <solonovamax@12oclockpoint.com>
 *
 * The file NyxGithubReleaseExtension.kt is part of nyx
 * Last modified on 05-01-2025 12:06 a.m.
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * NYX IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ca.solostudios.nyx.plugin.publish.release

import ca.solostudios.nyx.internal.InternalNyxExtension
import ca.solostudios.nyx.internal.util.fileCollection
import ca.solostudios.nyx.internal.util.githubRelease
import ca.solostudios.nyx.internal.util.property
import ca.solostudios.nyx.project.NyxProjectInfoExtension
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.assign

/**
 * An extension to configure the GitHub release plugin.
 */
public class NyxGithubReleaseExtension(
    override val project: Project,
    private val info: NyxProjectInfoExtension,
) : InternalNyxExtension {
    /**
     * Whether to automatically generate the body for this release. If `body`
     * is specified, the body will be pre-pended to the automatically generated
     * notes.
     *
     * See:
     * https://docs.github.com/en/rest/releases/releases?apiVersion=2022-11-28#create-a-release.
     */
    public val generateReleaseNotes: Property<Boolean> = property()

    /**
     * Text describing the contents of the tag.
     *
     * See:
     * https://docs.github.com/en/rest/releases/releases?apiVersion=2022-11-28#create-a-release.
     */
    public val body: Property<String> = property()

    /**
     * `true` to create a draft (unpublished) release, `false` to create a
     * published one.
     *
     * See:
     * https://docs.github.com/en/rest/releases/releases?apiVersion=2022-11-28#create-a-release.
     */
    public val draft: Property<Boolean> = property()

    /**
     * `true` to identify the release as a prerelease. `false` to identify the
     * release as a full release.
     *
     * See:
     * https://docs.github.com/en/rest/releases/releases?apiVersion=2022-11-28#create-a-release.
     */
    public val prerelease: Property<Boolean> = property()

    /**
     * If these artifacts can be used to overwrite an existing release (delete,
     * then re-create).
     */
    public val overwrite: Property<Boolean> = property()

    /**
     * If these artifacts can be uploaded to an existing release.
     */
    public val uploadToExisting: Property<Boolean> = property()

    /**
     * If this is a dry run.
     *
     * A dry run will not upload anything.
     */
    public val dryRun: Property<Boolean> = property()

    /**
     * A file collection of all the assets published in the release.
     */
    public val releaseAssets: ConfigurableFileCollection = fileCollection()

    /**
     * Enables release note generation.
     *
     * @see generateSequence
     */
    public fun withGenerateReleaseNotes() {
        generateReleaseNotes = true
    }

    /**
     * Enables draft releases.
     *
     * @see draft
     */
    public fun withDraft() {
        draft = true
    }

    /**
     * Enables prereleases.
     *
     * @see prerelease
     */
    public fun withPrerelease() {
        prerelease = true
    }

    /**
     * Enables overwriting existing releases.
     *
     * @see overwrite
     */
    public fun withOverwrite() {
        overwrite = true
    }

    /**
     * Enables uploading to existing releases.
     *
     * @see uploadToExisting
     */
    public fun withUploadToExisting() {
        uploadToExisting = true
    }

    /**
     * Enables dry run.
     *
     * @see dryRun
     */
    public fun withDryRun() {
        dryRun = true
    }

    /**
     * Configures the release assets.
     */
    public fun releaseAssets(action: (ConfigurableFileCollection).() -> Unit) {
        releaseAssets.apply(action)
    }

    override fun configureProject() {
        val tokenProperty = project.providers.gradleProperty(GITHUB_TOKEN_GRADLE_PROPERTY)
        if (tokenProperty.isPresent)
            githubRelease.token(tokenProperty)
        else
            githubRelease.token(System.getenv("GITHUB_TOKEN"))

        val version = info.version

        githubRelease {
            owner = info.repository.projectOwner
            repo = info.repository.projectRepo
            tagName = "v$version"
            targetCommitish = null
            releaseName = "v$version"
        }

        if (generateReleaseNotes.isPresent)
            githubRelease.generateReleaseNotes = generateReleaseNotes

        if (draft.isPresent)
            githubRelease.draft = draft

        if (prerelease.isPresent)
            githubRelease.prerelease = prerelease

        if (overwrite.isPresent)
            githubRelease.overwrite = overwrite

        if (uploadToExisting.isPresent)
            githubRelease.allowUploadToExisting = uploadToExisting

        if (dryRun.isPresent)
            githubRelease.dryRun = dryRun

        if (!releaseAssets.isEmpty)
            githubRelease.releaseAssets(releaseAssets)

        if (generateReleaseNotes.isPresent)
            githubRelease.generateReleaseNotes = generateReleaseNotes
    }

    public companion object {
        /**
         * The gradle property used to retrieve the modrinth token.
         *
         * Please specify this property in
         * [the `gradle.properties` located in your `$GRADLE_USER_HOME`](https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_environment_variables).
         * By default, this is located at `~/.gradle/gradle.properties` on
         * Linux/MacOS, and `%USERPROFILE%\.gradle\gradle.properties` on Windows.
         */
        public const val GITHUB_TOKEN_GRADLE_PROPERTY: String = "github.token"

        /**
         * The name this extension is added with.
         */
        public const val NAME: String = "github"
    }
}
