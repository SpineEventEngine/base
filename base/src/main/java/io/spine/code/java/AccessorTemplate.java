/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.code.java;

import com.google.common.base.Objects;
import com.google.errorprone.annotations.Immutable;

import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class AccessorTemplate implements Serializable {

    private static final long serialVersionUID = 0L;

    private final String prefix;
    private final String suffix;

    private AccessorTemplate(String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public static AccessorTemplate prefixed(String prefix) {
        checkNotNull(prefix);
        return new AccessorTemplate(prefix, "");
    }

    public static AccessorTemplate prefixedAndSuffixed(String prefix, String suffix) {
        checkNotNull(prefix);
        checkNotNull(suffix);
        return new AccessorTemplate(prefix, suffix);
    }

    public String format(FieldName field) {
        String name = String.format(template(), field.capitalize());
        return name;
    }

    private String template() {
        return prefix + "%s" + suffix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AccessorTemplate template = (AccessorTemplate) o;
        return Objects.equal(template(), template.template());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(prefix, suffix);
    }

    @Override
    public String toString() {
        return template();
    }
}
