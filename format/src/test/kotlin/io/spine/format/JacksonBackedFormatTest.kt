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

package io.spine.format

import com.google.common.collect.ImmutableList
import java.time.Instant
import java.util.*

/**
 * The abstract base for tests checking formats backed by the Jackson library.
 */
abstract class JacksonBackedFormatTest(format: Format<in Any>) :
    FormatTest<UserAccount>(format) {

    override fun createInstance(): UserAccount {
        return UserAccount.create(UUID.randomUUID().toString())
    }
}

data class UserAccount(
    val id: String,
    val creationTimestamp: Instant,     // Test `JavaTimeModule`.
    val emails: ImmutableList<EmailAddress>,  // Test `GuavaModule` with a custom item type.
    val gender: Optional<String>        // Test `Jdk8Module`.
) {
    companion object {
        fun create(id: String) = UserAccount(
            id,
            Instant.now(),
            ImmutableList.of(EmailAddress("j.doe@example.org"), EmailAddress("john@acme-corp.com")),
            gender = Optional.of("X")
        )
    }
}

// We don't want to bring Jakarta Mail just to test a custom type.
data class EmailAddress(val value: String)
