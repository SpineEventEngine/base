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

package io.spine.internal.gradle.fs

import java.io.File
import java.net.URI
import java.nio.file.FileSystem
import java.nio.file.Files.createTempDirectory
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.WatchEvent
import java.nio.file.WatchKey
import java.nio.file.WatchService

/**
 * A path to a temporary folder, which is not created until it is really used.
 *
 * After the first usage, the instances of this type delegate all calls to the internally
 * created instance of [Path] created with [createTempDirectory].
 */
class LazyTempPath(private val prefix: String) : Path {

    private lateinit var tempPath: Path

    private fun delegate(): Path {
        if (!::tempPath.isInitialized) {
            tempPath = createTempDirectory(prefix)
        }
        return tempPath
    }

    override fun compareTo(other: Path?): Int {
        return delegate().compareTo(other)
    }

    override fun iterator(): MutableIterator<Path> {
        return delegate().iterator()
    }

    override fun register(
        watcher: WatchService?,
        events: Array<out WatchEvent.Kind<*>>?,
        vararg modifiers: WatchEvent.Modifier?
    ): WatchKey {
        return delegate().register(watcher, events, *modifiers)
    }

    override fun register(watcher: WatchService?, vararg events: WatchEvent.Kind<*>?): WatchKey {
        return delegate().register(watcher, *events)
    }

    override fun getFileSystem(): FileSystem {
        return delegate().fileSystem
    }

    override fun isAbsolute(): Boolean {
        return delegate().isAbsolute
    }

    override fun getRoot(): Path {
        return delegate().root
    }

    override fun getFileName(): Path {
        return delegate().fileName
    }

    override fun getParent(): Path {
        return delegate().parent
    }

    override fun getNameCount(): Int {
        return delegate().nameCount
    }

    override fun getName(index: Int): Path {
        return delegate().getName(index)
    }

    override fun subpath(beginIndex: Int, endIndex: Int): Path {
        return delegate().subpath(beginIndex, endIndex)
    }

    override fun startsWith(other: Path): Boolean {
        return delegate().startsWith(other)
    }

    override fun startsWith(other: String): Boolean {
        return delegate().startsWith(other)
    }

    override fun endsWith(other: Path): Boolean {
        return delegate().endsWith(other)
    }

    override fun endsWith(other: String): Boolean {
        return delegate().endsWith(other)
    }

    override fun normalize(): Path {
        return delegate().normalize()
    }

    override fun resolve(other: Path): Path {
        return delegate().resolve(other)
    }

    override fun resolve(other: String): Path {
        return delegate().resolve(other)
    }

    override fun resolveSibling(other: Path): Path {
        return delegate().resolveSibling(other)
    }

    override fun resolveSibling(other: String): Path {
        return delegate().resolveSibling(other)
    }

    override fun relativize(other: Path): Path {
        return delegate().relativize(other)
    }

    override fun toUri(): URI {
        return delegate().toUri()
    }

    override fun toAbsolutePath(): Path {
        return delegate().toAbsolutePath()
    }

    override fun toRealPath(vararg options: LinkOption?): Path {
        return delegate().toRealPath(*options)
    }

    override fun toFile(): File {
        return delegate().toFile()
    }
}
