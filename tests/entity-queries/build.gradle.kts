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

import io.spine.tools.protoc.MessageSelectorFactory.suffix

modelCompiler {

    generateValidation = true

    fields {
        generateFor("spine.tools.column.ProjectName", markAs("io.spine.tools.protoc.given.ProjectNameField"))

        generateFor(messages().inFiles(suffix("events.proto")), markAs("io.spine.base.EventMessageField"))
        generateFor(messages().inFiles(suffix("rejections.proto")), markAs("io.spine.base.EventMessageField"))
        generateFor(messages().entityState(), markAs("io.spine.query.EntityStateField"))
    }

    methods {
        applyFactory("io.spine.code.gen.java.UuidMethodFactory", messages().uuid())
    }

    entityQueries {
        generate(true)
    }

    interfaces {
        mark(messages().inFiles(suffix("commands.proto")), asType("io.spine.base.CommandMessage"))
        mark(messages().inFiles(suffix("events.proto")), asType("io.spine.base.EventMessage"))
        mark(messages().inFiles(suffix("rejections.proto")), asType("io.spine.base.RejectionMessage"))
        mark(messages().uuid(), asType("io.spine.base.UuidValue"))
        mark(messages().entityState(), asType("io.spine.base.EntityState"))
    }
}