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

package io.spine.tools.comparables

import io.spine.base.FieldPath
import io.spine.core.External
import io.spine.core.Subscribe
import io.spine.core.Where
import io.spine.option.CompareByOption
import io.spine.protobuf.unpack
import io.spine.protodata.TypeName
import io.spine.protodata.TypeOptionDiscovered
import io.spine.protodata.plugin.View
import io.spine.protodata.plugin.ViewRepository
import io.spine.server.entity.alter
import io.spine.server.route.EventRouting

/**
 * A view on a comparable message type.
 */
internal class ComparableTypeView : View<TypeName, ComparableType, ComparableType.Builder>() {

    @Subscribe
    internal fun on(
        @External @Where(field = "option.name", equals = "compare_by") event: TypeOptionDiscovered
    ) = alter {
        name = event.type
        declaredIn = event.file
        val option = event.option.value.unpack<CompareByOption>()
        option.fieldList.map {
            it.split(".")
        }.map {
            FieldPath.newBuilder()
                .addAllFieldName(it)
                .buildPartial()
        }.forEach {
            addField(it)
        }
        reverseOrder = option.descending
    }

    /**
     * Repository for the [ComparableTypeView].
     */
    internal class Repo : ViewRepository<TypeName, ComparableTypeView, ComparableType>() {

        override fun setupEventRouting(routing: EventRouting<TypeName>) {
            super.setupEventRouting(routing)
            routing.unicast(TypeOptionDiscovered::class.java) { m -> m.type }
        }
    }
}
