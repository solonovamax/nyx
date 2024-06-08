package ca.solostudios.nyx.ext.project

import ca.solostudios.nyx.api.ConfiguresProject
import ca.solostudios.nyx.api.HasProject
import ca.solostudios.nyx.util.convention
import ca.solostudios.nyx.util.formatAsName
import ca.solostudios.nyx.util.listProperty
import ca.solostudios.nyx.util.property
import ca.solostudios.nyx.util.tasks
import ca.solostudios.nyx.util.toStringOrEmpty
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.publish.maven.MavenPomDeveloper
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.withType

public class ProjectInfoExtension(override val project: Project) : ConfiguresProject, HasProject {
    /**
     * The name of the project to be used when publishing.
     *
     * Defaults to the `project.name`, formatted in "Title Case"
     * eg.
     * - `my-project` -> "My Project"
     * - `some-project-name` -> "Some Project Name"
     *
     * Note:
     * - `-kt` is replaced with "Kotlin"
     */
    public val name: Property<String> = property<String>().convention(project) {
        project.name.formatAsName()
    }

    /**
     * The group id used when publishing.
     *
     * Defaults to `project.group`.
     */
    public var group: String
        get() = project.group.toStringOrEmpty()
        set(value) {
            project.group = value
        }

    /**
     * The module name used when publishing.
     *
     * This is used for the artifact coordinates once it is published.
     *
     * Defaults to `project.name`
     */
    public val module: Property<String> = property<String>().convention(project) {
        project.name
    }

    /**
     * The version used when publishing.
     *
     * Defaults to `project.version`.
     */
    public var version: String
        get() = project.version.toStringOrEmpty()
        set(value) {
            project.version = value
        }

    /**
     * The description used when publishing.
     *
     * Defaults to `project.description`.
     */
    public var description: String
        get() = project.description.toStringOrEmpty()
        set(value) {
            project.description = value
        }

    /**
     * The url of the organization used when publishing.
     */
    public val organizationUrl: Property<String> = property()

    /**
     * The name of the organization used when publishing.
     */
    public val organizationName: Property<String> = property()

    /**
     * The list of developers used when publishing.
     */
    public val developers: ListProperty<(MavenPomDeveloper) -> Unit> = listProperty()

    /**
     * Information about the repository used when publishing.
     */
    @Nested
    public val repository: RepositoryInfo = RepositoryInfo(project)

    /**
     * Information about the license used when publishing.
     */
    @Nested
    public val license: LicenseInfoExtension = LicenseInfoExtension(project)

    /**
     * Adds a developer to the publishing information
     *
     * @param developer Configuration for the developer that is added
     */
    public fun developer(developer: Action<MavenPomDeveloper>) {
        this.developers.add { developer.execute(it) }
    }

    /**
     * Adds a developer to the publishing information
     *
     * @param developer Configuration for the developer that is added
     * @receiver The developer to be added
     */
    public fun developer(developer: (MavenPomDeveloper).() -> Unit) {
        this.developers.add(developer)
    }

    /**
     * Configures the repository info
     */
    public fun repository(action: Action<RepositoryInfo>) {
        action.execute(repository)
    }

    /**
     * Configures the repository info
     */
    public fun repository(action: (RepositoryInfo).() -> Unit) {
        repository.apply(action)
    }

    /**
     * Configures the license info
     */
    public fun license(action: Action<LicenseInfoExtension>) {
        action.execute(license)
    }

    /**
     * Configures the license info
     */
    public fun license(action: (LicenseInfoExtension).() -> Unit) {
        license.apply(action)
    }

    override fun onLoad() {}

    override fun configureProject() {
        tasks {
            withType<AbstractArchiveTask>().configureEach {
                archiveBaseName = project.name
            }
        }
    }
}
