/*
 * Copyright 2023, TeamDev. All rights reserved.
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

package io.spine.type

import com.google.protobuf.Descriptors.FileDescriptor
import io.spine.type.ApiOption.beta
import io.spine.type.ApiOption.experimental
import io.spine.type.ApiOption.internal
import io.spine.type.ApiOption.spi
import kotlin.jvm.optionals.getOrNull

/**
 * Obtains the value of the `internal_all` option, if the option is present in the file.
 * If the option is not present, assumes `false`.
 */
public fun FileDescriptor.allTypesAreBeta(): Boolean? = optionValueOrNull(beta())

/**
 * Obtains the value of the `internal_all` option, if the option is present in the file.
 * If the option is not present, assumes `false`.
 */
public fun FileDescriptor.allTypesAreInternal(): Boolean? = optionValueOrNull(internal())

/**
 * Obtains the value of the `spi_all` option, if the option is present in the file.
 * If the option is not present, assumes `false`.
 */
public fun FileDescriptor.allTypesAreSpi(): Boolean? = optionValueOrNull(spi())

/**
 * Obtains the value of the `experimental_all` option, if the option is present in the file.
 * If the option is not present, assumes `false`.
 */
public fun FileDescriptor.allTypesAreExperimental(): Boolean? = optionValueOrNull(experimental())

private fun FileDescriptor.optionValueOrNull(opt: ApiOption): Boolean? =
    opt.findIn(this).getOrNull()
