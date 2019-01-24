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

package io.spine.tools.gradle.compiler;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import org.checkerframework.checker.regex.qual.Regex;

/**
 * A {@link GeneratedInterfaceConfig} which configures message types defined in a Proto file with
 * a certain naming.
 */
final class PostfixInterfaceConfig extends AbstractGeneratedInterfaceConfig {

    private final String postfix;

    PostfixInterfaceConfig(@Regex String postfix) {
        this.postfix = postfix;
    }

    String fileSuffix() {
        return postfix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PostfixInterfaceConfig)) {
            return false;
        }
        PostfixInterfaceConfig config = (PostfixInterfaceConfig) o;
        return Objects.equal(postfix, config.postfix)
                && Objects.equal(interfaceName().orElse(null), interfaceName().orElse(null));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(postfix, interfaceName().orElse(null));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("postfix", postfix)
                          .add("interfaceName", interfaceName().orElse(null))
                          .toString();
    }
}
