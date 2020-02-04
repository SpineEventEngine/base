/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.tools.protoc.nested;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.NullPointerTester;
import io.spine.tools.protoc.CompilerOutput;
import io.spine.tools.protoc.ExternalClassLoader;
import io.spine.tools.protoc.QueryableConfig;
import io.spine.tools.protoc.column.GenerateColumns;
import io.spine.tools.protoc.given.TestNestedClassFactory;
import io.spine.tools.protoc.nested.given.TestClassLoader;
import io.spine.type.MessageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static io.spine.tools.protoc.InsertionPoint.class_scope;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("`GenerateColumns` should")
class GenerateColumnsTest {

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester()
                .testAllPublicConstructors(GenerateColumns.class);
        new NullPointerTester()
                .testAllPublicInstanceMethods(newTask());
    }

    @Test
    @DisplayName("reject an empty factory name on construction")
    void rejectEmptyFactoryName() {
        String emptyFactoryName = "";
        assertThrows(IllegalArgumentException.class,
                     () -> new GenerateColumns(testClassLoader(), newConfig(emptyFactoryName)));
    }

    @Test
    @DisplayName("reject an effectively empty factory name on construction")
    void rejectEffectivelyEmptyName() {
        String effectivelyEmptyName = "    ";
        assertThrows(IllegalArgumentException.class,
                     () -> new GenerateColumns(testClassLoader(), newConfig(effectivelyEmptyName)));
    }

    @Test
    @DisplayName("generate compiler output for message type which is an entity state with columns")
    void generateOutput() {
        GenerateColumns columns = newTask();
        MessageType messageType = new MessageType(Order.getDescriptor());
        ImmutableList<CompilerOutput> output = columns.generateFor(messageType);

        assertThat(output).hasSize(1);

        CompilerOutput compilerOutput = output.get(0);
        String insertionPoint = compilerOutput.asFile()
                                              .getInsertionPoint();
        assertThat(insertionPoint).startsWith(class_scope.name());
    }

    @Nested
    @DisplayName("generate an empty output")
    class GenerateEmptyOutput {

        @Test
        @DisplayName("for a message type which is not an entity state")
        void forNonEntityState() {
            GenerateColumns columns = newTask();
            MessageType messageType = new MessageType(OrderId.getDescriptor());
            ImmutableList<CompilerOutput> output = columns.generateFor(messageType);

            assertThat(output).hasSize(0);
        }

        @Test
        @DisplayName("for a message type which is an entity state with no columns declared")
        void forStateWithoutColumns() {
            GenerateColumns columns = newTask();
            MessageType messageType = new MessageType(Task.getDescriptor());
            ImmutableList<CompilerOutput> output = columns.generateFor(messageType);

            assertThat(output).hasSize(0);
        }
    }

    private static GenerateColumns newTask() {
        return new GenerateColumns(testClassLoader(),
                                   newConfig(TestNestedClassFactory.class.getName()));
    }
    private static ExternalClassLoader<NestedClassFactory> testClassLoader() {
        return TestClassLoader.instance();
    }

    private static QueryableConfig newConfig(String factoryName) {
        return QueryableConfig.newBuilder().setValue(factoryName).build();
    }
}
