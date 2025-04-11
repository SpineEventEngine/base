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

package io.spine.format.parse

import com.google.protobuf.Timestamp
import io.kotest.matchers.shouldBe
import io.spine.base.Time
import io.spine.string.stringify
import java.io.File
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir

@DisplayName("`TextReader` should")
internal class TextReaderSpec {

    @TempDir
    private lateinit var tempDir: File
    private lateinit var file: File

    @Test
    fun `read a text file`() {
        file = File(tempDir, "verse.txt")
        val text = """
            Нам не дано предугадать,
            Как слово наше отзовется, —
            И нам сочувствие дается,
            Как нам дается благодать…            
            """.trimIndent()
        file.writeText(text)
        parseFile<String>(file) shouldBe text
    }

    @Test
    fun `reject other types than 'String' when reading a text file`() {
        file = File(tempDir, "time.txt")
        val time = Time.currentTime()
        file.writeText(time.stringify())
        assertThrows<IllegalStateException> {
            parseFile<Timestamp>(file)
        }
    }
}
