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

package io.spine.validate;

import com.google.protobuf.Timestamp;
import io.spine.base.Identifier;
import io.spine.test.validate.msg.builder.Attachment;
import io.spine.test.validate.msg.builder.Member;
import io.spine.test.validate.msg.builder.ProjectVBuilder;
import io.spine.test.validate.msg.builder.Task;
import io.spine.test.validate.msg.builder.TaskLabel;
import io.spine.test.validate.msg.builder.TaskVBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.protobuf.ByteString.copyFrom;
import static com.google.protobuf.util.Durations.fromSeconds;
import static com.google.protobuf.util.Timestamps.add;
import static io.spine.base.Identifier.newUuid;
import static io.spine.base.Time.getCurrentTime;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A test suite covering the {@link ValidatingBuilder} behavior.
 *
 * <p>Since most {@code ValidatingBuilders} are generated, the concrete test suits for each of them
 * are not required.
 *
 * <p>Any {@code ValidatingBuilder} implementation should pass these tests. When implementing your
 * own {@code ValidatingBuilder}, be sure to check if it fits the constraints stated below.
 */
@DisplayName("ValidatingBuilder should")
class ValidatingBuilderTest {

    private ProjectVBuilder builder;

    @BeforeEach
    void setUp() {
        builder = fill();
    }

    @Test
    @DisplayName("check required validated repeated fields")
    void check_required_validated_repeated_fields() {
        assertThrows(ValidationException.class, () -> builder.addTask(Task.getDefaultInstance()));
    }

    @Test
    @DisplayName("ensure required validated repeated fields")
    void ensure_required_validated_repeated_fields() {
        builder.clearTask();
        assertThrows(ValidationException.class, () -> builder.build());
    }

    @Test
    @DisplayName("check required validated map field values")
    void check_required_validated_map_field_values() {
        assertThrows(ValidationException.class,
                     () -> builder.putRole("Co-owner", Member.getDefaultInstance()));
    }

    @Test
    @DisplayName("ensure required validated map fields")
    void ensure_required_validated_map_fields() {
        builder.clearRole();
        assertThrows(ValidationException.class, () -> builder.build());
    }

    @Test
    @DisplayName("check validated repeated fields")
    void check_validated_repeated_fields() {
        assertThrows(ValidationException.class, () -> builder.addSubscriberEmail(""));
    }

    @Test
    @DisplayName("dispense with validated repeated fields")
    void dispense_with_validated_repeated_fields() {
        builder.clearSubscriberEmail();
        builder.build();
    }

    @Test
    @DisplayName("check validated map field values")
    void check_validated_map_field_values() {
        assertThrows(ValidationException.class,
                     () -> builder.putAttachment(newUuid(), Attachment.getDefaultInstance()));
    }

    @Test
    @DisplayName("dispense with validated map fields")
    void dispense_with_validated_map_fields() {
        builder.clearAttachment();
        builder.build();
    }

    @Test
    @DisplayName("accept any required repeated fields")
    void accept_any_required_repeated_fields() {
        builder.addMember(Member.getDefaultInstance());
    }

    @Test
    @DisplayName("ensure required repeated fields")
    void ensure_required_repeated_fields() {
        builder.clearMember();
        assertThrows(ValidationException.class, () -> builder.build());
    }

    @Test
    @DisplayName("accept any required map field value")
    void accept_any_required_map_field_value() {
        builder.putDeletedTask(newUuid(), timeInFuture());
    }

    @Test
    @DisplayName("ensure required map fields")
    void ensure_required_map_fields() {
        builder.clearDeletedTask();
        assertThrows(ValidationException.class, () -> builder.build());
    }

    @Test
    @DisplayName("accept any unchecked repeated fields")
    void accept_any_unchecked_repeated_fields() {
        builder.addDescription("");
    }

    @Test
    @DisplayName("dispense with unchecked repeated fields")
    void dispense_with_unchecked_repeated_fields() {
        builder.clearDescription();
        builder.build();
    }

    @Test
    @DisplayName("accept any unchecked map field value")
    void accept_any_unchecked_map_field_value() {
        builder.putLabel("empty", "none");
    }

    @Test
    @DisplayName("dispense with unchecked map fields")
    void dispense_with_unchecked_map_fields() {
        builder.clearLabel();
        builder.build();
    }

    @Test
    @DisplayName("not allow to mutate a (set_once) marked string field via the setter method")
    void testSetDoesNotAllowMutate() {
        TaskVBuilder builder = TaskVBuilder
                .newBuilder()
                .setId(Identifier.newUuid());
        assertThrows(ValidationException.class, () -> builder.setId(Identifier.newUuid()));
    }

    @Test
    @DisplayName("not allow to mutate a (set_once) marked string field via the `clear` method")
    void testSetOnceDoesNotAllowClear() {
        TaskVBuilder builder = TaskVBuilder
                .newBuilder()
                .setId(Identifier.newUuid());
        assertThrows(ValidationException.class, builder::clearId);
    }

    @Test
    @DisplayName("not allow to mutate a (set_once) marked string field via the `mergeFrom` method")
    void testSetOnceDesNotAllowMerge() {
        TaskVBuilder builder = TaskVBuilder
                .newBuilder()
                .setId(Identifier.newUuid());
        assertThrows(ValidationException.class, () -> builder.mergeFrom(sampleTask()));
    }

    @Test
    @DisplayName("not allow to mutate a (set_once) marked enum field via the setter method")
    void testSetOnceDoesNotAllowMutateEnumSetter() {
        TaskVBuilder builder = TaskVBuilder
                .newBuilder()
                .setLabel(TaskLabel.CRITICAL);
        assertThrows(ValidationException.class,
                     () -> builder.setLabel(TaskLabel.OF_LITTLE_IMPORTANCE));
    }

    @Test
    @DisplayName("not allow to mutate a (set_once) marked enum field via the `clear` method")
    void testSetOnceDoesNotAllowMutateEnumClear() {
        TaskVBuilder builder = TaskVBuilder
                .newBuilder()
                .setLabel(TaskLabel.IMPORTANT);
        assertThrows(ValidationException.class, builder::clearLabel);
    }

    @Test
    @DisplayName("not allow to mutate a (set_once) marked enum field via the `mergeFrom` method")
    void testSetOnceDoesNotAllowMutateEnumMergeFrom() {
        TaskVBuilder builder = TaskVBuilder
                .newBuilder()
                .setLabel(TaskLabel.CRITICAL);
        assertThrows(ValidationException.class, () -> builder.mergeFrom(sampleTask()));
    }

    @Test
    @DisplayName("not allow to mutate a (set_once) marked message field via the setter method")
    void testSetOnceDoesNotAllowMutateMessageSetter() {
        Member assignee = Member.getDefaultInstance();
        Member anotherAssignee = Member.newBuilder()
                                       .setId(Identifier.newUuid())
                                       .build();
        TaskVBuilder builder = TaskVBuilder
                .newBuilder()
                .setAssignee(assignee);
        assertThrows(ValidationException.class, () -> builder.setAssignee(anotherAssignee));
    }

    @Test
    @DisplayName("not allow to mutate a (set_once) marked message field via the clear method")
    void testSetOnceDoesNotAllowMutateMessageClear() {
        Member assignee = Member.getDefaultInstance();
        TaskVBuilder builder = TaskVBuilder
                .newBuilder()
                .setAssignee(assignee);
        assertThrows(ValidationException.class, builder::clearAssignee);
    }

    @Test
    @DisplayName("not allow to mutate a (set_once) marked message field via the `mergeFrom` method")
    void testSetOnceDoeNotAllowMutateMessageMergeFrom() {
        Member assignee = Member.getDefaultInstance();
        TaskVBuilder builder = TaskVBuilder
                .newBuilder()
                .setAssignee(assignee);
        assertThrows(ValidationException.class, () -> builder.mergeFrom(sampleTask()));
    }

    /**
     * Creates a valid {@link ProjectVBuilder} instance.
     */
    private static ProjectVBuilder fill() {
        ProjectVBuilder builder = ProjectVBuilder.newBuilder()
                                                 .addTask(task())
                                                 .addMember(member())
                                                 .putRole("Ownner", member())
                                                 .putDeletedTask(newUuid(), timeInPast());
        builder.build(); // Ensure no ValidationException is thrown.
        return builder;
    }

    private static Timestamp timeInPast() {
        return add(getCurrentTime(), fromSeconds(-1000L));
    }

    private static Timestamp timeInFuture() {
        return add(getCurrentTime(), fromSeconds(1000L));
    }

    private static Task task() {
        String id = newUuid();
        String name = "Task name" + id;
        return Task.newBuilder()
                   .setId(id)
                   .setName(name)
                   .build();
    }

    private static Task sampleTask() {
        Member member = Member.getDefaultInstance();
        return Task.newBuilder()
                   .setId(Identifier.newUuid())
                   .setAssignee(member)
                   .setLabel(TaskLabel.OF_LITTLE_IMPORTANCE)
                   .setName("Do something unimportant")
                   .build();
    }

    private static Member member() {
        Member.Builder member = Member.newBuilder()
                                      .setId(newUuid())
                                      .setName("John Smith")
                                      .setAvatarImage(copyFrom(new byte[]{1, 2, 3}));
        return member.build();
    }
}
