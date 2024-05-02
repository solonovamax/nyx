package ca.solostudios.nyx.ext.mc

import ca.solostudios.nyx.api.ConfiguresProject
import ca.solostudios.nyx.api.HasProject
import ca.solostudios.nyx.util.capitalizeWord
import ca.solostudios.nyx.util.configurations
import ca.solostudios.nyx.util.loom
import ca.solostudios.nyx.util.property
import ca.solostudios.nyx.util.sourceSets
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import org.slf4j.kotlin.getLogger
import org.slf4j.kotlin.warn
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.writeText

public class LoomExtension(override val project: Project) : ConfiguresProject, HasProject {
    private val logger by getLogger()

    public val allocatedMemory: Property<Int> = property<Int>().convention(2)

    @Nested
    public val mixin: MixinExtension = MixinExtension(project)

    /**
     * Adds an access widener at `src/main/resources/`[name]`.accesswidener`.
     *
     * The file will be created if it does not exist (this is to avoid an error with fabric loom)
     *
     * @param name The name of the access widener, defaulting to `project.name`.
     */
    public fun accessWidener(name: String = project.name) {
        loom {
            // src/main/resources/$name.accesswidener
            val accessWidenerPaths = sourceSets["main"].resources.srcDirs.map { it.resolve("$name.accesswidener").toPath() }
            if (accessWidenerPaths.none { it.exists() }) {
                // try creating access widener file
                val firstAccessWidener = accessWidenerPaths.first()

                logger.warn {
                    """
                        Can't find an access widener in any resource directory named '$name.accesswidener'.
                        Created one at $firstAccessWidener for you.

                        If this is not desired, please either remove the file and create it in another resource directory, or remove loom.accessWidener() from your buildscript.
                    """.trimIndent()
                }

                firstAccessWidener.parent.createDirectories()
                firstAccessWidener.createFile()
                firstAccessWidener.writeText(
                    """
                        |# Auto-generated access widener
                        |accessWidener v2 named
                        |
                        |
                    """.trimMargin("|")
                )
            }
            accessWidenerPath = accessWidenerPaths.first { it.exists() }.toFile()
        }
    }

    override fun onLoad() {
        mixin.onLoad()

        configurations {
            val include by named("include")

            addInclusionConfigurations(include, "include")

            if (findByName("shadow") != null) {
                val shadow by named("shadow")

                addInclusionConfigurations(shadow, "shadow")
            }
        }
    }

    /**
     * Configures the java compiler
     */
    public fun mixin(action: Action<MixinExtension>) {
        action.execute(mixin)
    }

    /**
     * Configures the java compiler
     */
    public fun mixin(action: (MixinExtension).() -> Unit) {
        mixin.apply(action)
    }

    private fun ConfigurationContainer.addInclusionConfigurations(inclusionConfiguration: Configuration, nameAddition: String) {
        // generate configurations such as
        // apiInclude
        // implementationInclude
        // modApiInclude
        // modImplementationInclude
        matching { it.name.contains("implementation", ignoreCase = true) || it.name.contains("api", ignoreCase = true) }.all {
            val baseConfiguration = this
            val shadowConfigurationName = name + nameAddition.capitalizeWord()
            val shadowConfiguration by register(shadowConfigurationName) {
                inclusionConfiguration.extendsFrom(this)
                baseConfiguration.extendsFrom(this)
            }
        }
    }

    override fun configureProject() {
        mixin.configureProject()
    }
}
