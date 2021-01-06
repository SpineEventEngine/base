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

package io.spine.tools.protoc;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.protobuf.Message;
import io.spine.base.CommandMessage;
import io.spine.base.EntityColumn;
import io.spine.base.EventMessage;
import io.spine.base.Identifier;
import io.spine.base.RejectionMessage;
import io.spine.base.SubscribableField;
import io.spine.base.UuidValue;
import io.spine.test.protoc.EducationalInstitution;
import io.spine.test.protoc.Kindergarten;
import io.spine.test.protoc.Outer;
import io.spine.test.protoc.School;
import io.spine.test.protoc.University;
import io.spine.test.protoc.Wrapped;
import io.spine.test.tools.protoc.Movie;
import io.spine.test.tools.protoc.WeatherForecast;
import io.spine.tools.protoc.test.PIUserEvent;
import io.spine.tools.protoc.test.UserInfo;
import io.spine.type.MessageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("InnerClassMayBeStatic")
@DisplayName("`ProtocPlugin` should")
final class ProtocPluginTest {

    private static final String EVENT_INTERFACE_FQN =
            "io.spine.tools.protoc.PICustomerEvent";
    private static final String COMMAND_INTERFACE_FQN =
            "io.spine.tools.protoc.PICustomerCommand";
    private static final String USER_COMMAND_FQN =
            "io.spine.tools.protoc.PIUserCommand";

    @Test
    @DisplayName("generate marker interfaces")
    void generateMarkerInterfaces() throws ClassNotFoundException {
        checkMarkerInterface(EVENT_INTERFACE_FQN);
    }

    @Test
    @DisplayName("implement marker interface in the generated messages")
    void implementMarkerInterfacesInGeneratedMessages() {
        assertThat(PICustomerNotified.getDefaultInstance())
                .isInstanceOf(PICustomerEvent.class);
        assertThat(PICustomerEmailReceived.getDefaultInstance())
                .isInstanceOf(PICustomerEvent.class);
    }

    @Test
    @DisplayName("generate marker interfaces for the separate messages")
    void generateMarkerInterfacesForSeparateMessages() throws ClassNotFoundException {
        checkMarkerInterface(COMMAND_INTERFACE_FQN);
    }

    @Test
    @DisplayName("implement interface in the generated messages with `IS` option")
    void implementInterfaceInGeneratedMessagesWithIsOption() {
        assertThat(PICustomerCreated.getDefaultInstance()).isInstanceOf(PICustomerEvent.class);
        assertThat(PICreateCustomer.getDefaultInstance()).isInstanceOf(PICustomerCommand.class);
    }

    @Test
    @DisplayName("use `IS` and `EVERY IS` together")
    void useIsInPriorityToEveryIs() {
        assertThat(PIUserCreated.getDefaultInstance()).isInstanceOf(PIUserEvent.class);
        assertThat(PIUserNameUpdated.getDefaultInstance()).isInstanceOf(PIUserEvent.class);

        assertThat(UserName.getDefaultInstance()).isInstanceOf(PIUserEvent.class);
        assertThat(UserName.getDefaultInstance()).isInstanceOf(UserInfo.class);
    }

    @Test
    @DisplayName("resolve packages from src proto if the packages are not specified")
    void resolvePackagesFromSrcProtoIfNotSpecified() throws ClassNotFoundException {
        Class<?> cls = checkMarkerInterface(USER_COMMAND_FQN);
        assertTrue(cls.isAssignableFrom(PICreateUser.class));
    }

    @Test
    @DisplayName("skip non specified message types")
    void skipNonSpecifiedMessageTypes() {
        Class<?> cls = CustomerName.class;
        Class[] interfaces = cls.getInterfaces();
        assertEquals(1, interfaces.length);
        assertSame(CustomerNameOrBuilder.class, interfaces[0]);
    }

    @Test
    @DisplayName("mark as event messages")
    void skipStandardTypesIfIgnored() {
        assertThat(UserCreated.getDefaultInstance()).isInstanceOf(EventMessage.class);
        assertThat(UserNotified.getDefaultInstance()).isInstanceOf(EventMessage.class);

        assertThat(TypicalIdentifier.getDefaultInstance()).isInstanceOf(UuidValue.class);
    }

    @Test
    @DisplayName("mark as command messages")
    void markCommandMessages() {
        assertThat(CreateUser.getDefaultInstance()).isInstanceOf(CommandMessage.class);
        assertThat(NotifyUser.getDefaultInstance()).isInstanceOf(CommandMessage.class);
    }

    @Test
    @DisplayName("mark as rejection messages")
    void markRejectionMessages() {
        assertThat(Rejections.UserAlreadyExists.getDefaultInstance())
                .isInstanceOf(RejectionMessage.class);
        assertThat(Rejections.UserAlreadyExists.getDefaultInstance())
                .isInstanceOf(UserRejection.class);
    }

    @Test
    @DisplayName("mark messages with already existing interface types")
    @SuppressWarnings("UnnecessaryLocalVariable")
        // Compile-time verification.
    void implementHandcraftedInterfaces() {
        assertThat(Rejections.UserAlreadyExists.getDefaultInstance())
                .isInstanceOf(UserRejection.class);
        assertFalse(Message.class.isAssignableFrom(UserRejection.class));
        String id = Identifier.newUuid();
        Rejections.UserAlreadyExists message = Rejections.UserAlreadyExists
                .newBuilder()
                .setId(id)
                .build();
        UserRejection rejection = message;
        assertEquals(id, rejection.getId());
    }

    @Test
    @DisplayName("mark nested message declarations by `(is)` option")
    void markNestedTypes() {
        assertThat(Outer.Inner.class).isAssignableTo(Wrapped.class);
    }

    @Test
    @DisplayName("mark nested message declarations by `(every_is)` option")
    void markEveryNested() {
        assertThat(Kindergarten.class).isAssignableTo(EducationalInstitution.class);
        assertThat(School.class).isAssignableTo(EducationalInstitution.class);
        assertThat(School.Elementary.class).isAssignableTo(EducationalInstitution.class);
        assertThat(School.HighSchool.class).isAssignableTo(EducationalInstitution.class);
        assertThat(University.class).isAssignableTo(EducationalInstitution.class);
        assertThat(University.College.class).isAssignableTo(EducationalInstitution.class);
    }

    @Test
    @DisplayName("mark top-level message declarations with accordance with `modelCompiler.generatedInterfaces`")
    void markMessagesByFilePattern() {
        assertThat(WeatherForecast.class).isAssignableTo(DocumentMessage.class);
        assertThat(WeatherForecast.Temperature.getDefaultInstance())
                .isNotInstanceOf(DocumentMessage.class);
    }

    @Test
    @DisplayName("generate a custom method for an `.endsWith()` pattern")
    void generateCustomPatternBasedMethod() {
        MessageType expectedType =
                new MessageType(MessageEnhancedWithSuffixGenerations.getDescriptor());
        assertEquals(expectedType, MessageEnhancedWithSuffixGenerations.ownType());
    }

    @Test
    @DisplayName("mark a message with interface using `.endsWith()` pattern")
    void markMessageWithInterfaceUsingEndsWithPattern() {
        assertThat(MessageEnhancedWithSuffixGenerations.getDefaultInstance())
                .isInstanceOf(SuffixedMessage.class);
    }

    @Test
    @DisplayName("generate a random UUID message")
    void generateRandomUuidMethod() {
        assertNotEquals(TypicalIdentifier.generate(), TypicalIdentifier.generate());
    }

    @Test
    @DisplayName("create instance of UUID identifier")
    void createInstanceOfUuidIdentifier() {
        String uuid = Identifier.newUuid();
        assertEquals(TypicalIdentifier.of(uuid), TypicalIdentifier.of(uuid));
    }

    @Nested
    @DisplayName("mark a message with the interface using")
    final class MarkMessages {

        @Test
        @DisplayName("regex pattern")
        void regex() {
            assertThat(MessageEnhancedWithRegexGenerations.getDefaultInstance())
                    .isInstanceOf(RegexedMessage.class);
        }

        @Test
        @DisplayName("prefix pattern")
        void prefix() {
            assertThat(MessageEnhancedWithPrefixGenerations.getDefaultInstance())
                    .isInstanceOf(PrefixedMessage.class);
        }

        @Test
        @DisplayName("suffix pattern")
        void postfix() {
            assertThat(MessageEnhancedWithSuffixGenerations.getDefaultInstance())
                    .isInstanceOf(SuffixedMessage.class);
        }
    }

    @Nested
    @DisplayName("generate a custom method for a message using")
    final class GenerateMethods {

        @Test
        @DisplayName("prefix pattern")
        void prefixBasedMethod() {
            MessageType expectedType =
                    new MessageType(MessageEnhancedWithPrefixGenerations.getDescriptor());
            assertEquals(expectedType, MessageEnhancedWithPrefixGenerations.ownType());
        }

        @Test
        @DisplayName("regex pattern")
        void regexBasedMethod() {
            MessageType expectedType =
                    new MessageType(MessageEnhancedWithRegexGenerations.getDescriptor());
            assertEquals(expectedType, MessageEnhancedWithRegexGenerations.ownType());
        }

        @Test
        @DisplayName("suffix pattern")
        void suffixBasedMethod() {
            MessageType expectedType =
                    new MessageType(MessageEnhancedWithSuffixGenerations.getDescriptor());
            assertEquals(expectedType, MessageEnhancedWithSuffixGenerations.ownType());
        }
    }

    @Nested
    @DisplayName("generate a custom nested class for a message using")
    final class GenerateNestedClasses {

        @Test
        @DisplayName("prefix pattern")
        void basedOnNamePrefix() {
            Class<?> ownClass = MessageEnhancedWithPrefixGenerations.SomeNestedClass.messageClass();
            assertEquals(MessageEnhancedWithPrefixGenerations.class, ownClass);
        }

        @Test
        @DisplayName("regex pattern")
        void basedOnNameMatchingRegex() {
            Class<?> ownClass = MessageEnhancedWithRegexGenerations.SomeNestedClass.messageClass();
            assertEquals(MessageEnhancedWithRegexGenerations.class, ownClass);
        }

        @Test
        @DisplayName("suffix pattern")
        void basedOnNameSuffix() {
            Class<?> ownClass = MessageEnhancedWithSuffixGenerations.SomeNestedClass.messageClass();
            assertEquals(MessageEnhancedWithSuffixGenerations.class, ownClass);
        }
    }

    @Nested
    @DisplayName("generate methods for `MFGTMessage` using")
    final class MultiFactoryGeneration {

        @Test
        @DisplayName("`UuidMethodFactory`")
        void uuidMethodFactory() {
            assertNotEquals(MFGTMessage.generate(), MFGTMessage.generate());
            String uuid = Identifier.newUuid();
            assertEquals(MFGTMessage.of(uuid), MFGTMessage.of(uuid));
        }

        @Test
        @DisplayName("`TestMethodFactory`")
        void testMethodFactory() {
            assertEquals(new MessageType(MFGTMessage.getDescriptor()), MFGTMessage.ownType());
        }
    }

    @Test
    @DisplayName("generate columns for a queryable entity type")
    void generateColumns() {
        EntityColumn column = Movie.Column.title();
        String expectedName = "title";
        assertEquals(expectedName, column.name().value());
    }

    @Test
    @DisplayName("generate fields for a subscribable message type")
    void generateFields() {
        SubscribableField field = MovieTitleChanged.Field.oldTitle().value();
        String expectedFieldPath = "old_title.value";
        assertEquals(expectedFieldPath, field.getField().toString());
    }

    @CanIgnoreReturnValue
    private static Class<?> checkMarkerInterface(String fqn) throws ClassNotFoundException {
        Class<?> cls = Class.forName(fqn);
        assertTrue(cls.isInterface());
        assertTrue(Message.class.isAssignableFrom(cls));

        Method[] declaredMethods = cls.getDeclaredMethods();
        assertEquals(0, declaredMethods.length);
        return cls;
    }
}
