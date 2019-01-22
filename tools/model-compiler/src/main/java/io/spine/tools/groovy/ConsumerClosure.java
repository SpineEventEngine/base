/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.tools.groovy;

import groovy.lang.Closure;

import java.io.Serializable;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ConsumerClosure<T> extends Closure<Void> {

    private static final long serialVersionUID = 0L;

    private final Action<? super T> action;

    private ConsumerClosure(Action<? super T> action) {
        super(ConsumerClosure.class);
        this.action = action;
    }

    public static <T> ConsumerClosure<T> closure(Action<? super T> action) {
        checkNotNull(action);
        return new ConsumerClosure<>(action);
    }

    @SuppressWarnings("unused")
        // This is the method called when the closure is invoked.
        // Accessed via Groovy reflection.
    private void doCall(T thisInstance) {
        action.accept(thisInstance);
    }

    @FunctionalInterface
    public interface Action<T> extends Consumer<T>, Serializable {}
}
