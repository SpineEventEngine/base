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

package io.spine.testing

import com.google.common.truth.Truth.assertThat
import io.spine.code.java.PackageName
import io.spine.io.Files2
import java.io.File
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class `'TempDir' should` {

    companion object {
        const val prefix = "TempDirTest"
        val tempDir: File = TempDir.withPrefix(prefix)
    }

    @Nested
    inner class `be created under the directory ` {

        @Test
        fun `from the 'System' property 'java-dot-io-dot-tmpdir'`() {
            assertThat(tempDir.toString())
                .contains(Files2.systemTempDir())
        }

        @Test
        fun `named after the package of 'TempDir' class`() {
            assertThat(tempDir.toString())
                .contains(PackageName.of(TempDir::class.java).toString())
        }
    }

    @Test
    fun `create an instance serving a test suite class`() {
        val thisClass = javaClass
        val tempDir = TempDir.forClass(thisClass)
        assertThat(tempDir.toString())
            .contains(thisClass.simpleName)
    }
}
