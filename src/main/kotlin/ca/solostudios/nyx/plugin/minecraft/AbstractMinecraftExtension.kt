/*
 * Copyright (c) 2024-2025 solonovamax <solonovamax@12oclockpoint.com>
 *
 * The file AbstractMinecraftExtension.kt is part of nyx
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

package ca.solostudios.nyx.plugin.minecraft

import ca.solostudios.nyx.internal.InternalNyxExtension
import ca.solostudios.nyx.internal.util.listProperty
import ca.solostudios.nyx.internal.util.mapProperty
import ca.solostudios.nyx.internal.util.property
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property

/**
 * An abstract extension to configure the Minecraft environment.
 */
public abstract class AbstractMinecraftExtension(
    project: Project,
) : InternalNyxExtension {
    /**
     * The amount of memory to be allocated to minecraft.
     */
    public val allocatedMemory: Property<Int> = project.property<Int>().convention(2)

    /**
     * Any additional jvm arguments to be used when launching minecraft.
     */
    public val additionalJvmArgs: ListProperty<String> = project.listProperty<String>()
        .convention(listOf("-XX:+UseZGC"))

    /**
     * Any additional jvm properties to be used when launching minecraft.
     */
    public val additionalJvmProperties: MapProperty<String, String> = project.mapProperty<String, String>()
        .convention(mapOf())

    internal abstract fun setDefaultMixinRefmapName(defaultName: String)

    internal abstract fun addMixinConfig(name: String)

    public companion object {
        /**
         * The name this extension is added with.
         */
        public const val NAME: String = "minecraft"
    }
}
