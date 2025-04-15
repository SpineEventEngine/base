/*
 * Copyright 2025, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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

@file:JvmName("Files")

package io.spine.format

import io.spine.format.Format.ProtoJson
import io.spine.io.replaceExtension
import java.io.File

/**
 * Tells if this file is of one of the supported [formats][Format].
 */
public fun File.hasSupportedFormat(): Boolean =
    Format.entries.any { it.matches(this) }

/**
 * Ensures that the file has the [primary extension][Format.extensions] of the given [format].
 *
 * @return `this` instance if the extension matches,
 *   a new instance with the required extension otherwise.
 */
@Suppress("ReturnCount") // Prefer earlier exits for better readability.
public fun File.ensureFormatExtension(format: Format<*>): File {
    if (format.matches(this)) {
        return this
    }
    val primary = format.extension
    // Handle the special case of `.pb.json`.
    val pbJson = ProtoJson.extension
    if (path.endsWith(pbJson)) {
        val newPath = path.replace(pbJson, primary)
        return File(newPath)
    }
    return replaceExtension(primary)
}
