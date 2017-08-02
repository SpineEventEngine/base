package io.spine.protobuf;

import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import com.google.protobuf.Timestamp;
import io.spine.test.Tests;
import io.spine.time.Time;
import org.junit.Test;

import static io.spine.test.TestValues.newUuidValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("ThrowableNotThrown")
public class MessageFieldExceptionShould {

    @Test
    public void construct_instance_with_formatted_message() {
        final String param1 = "Букварь";
        final String param2 = "blue";
        final String param3 = String.valueOf(3);
        final StringValue protobufMessage = newUuidValue();
        final MessageFieldException exception =
                new MessageFieldException(protobufMessage,
                                          "Reading log is: %s %s %s",
                                          param1, param2, param3);

        assertEquals(protobufMessage, exception.getProtobufMessage());
        final String exceptionMessage = exception.getMessage();
        assertTrue(exceptionMessage.contains(param1));
        assertTrue(exceptionMessage.contains(param2));
        assertTrue(exceptionMessage.contains(param3));
    }

    @Test
    public void contains_instance_without_text() {
        final Timestamp protobufMessage = Time.getCurrentTime();
        final MessageFieldException exception = new MessageFieldException(protobufMessage);

        assertEquals(protobufMessage, exception.getProtobufMessage());
        assertTrue(exception.getMessage().isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void allow_null_params() {
        new MessageFieldException(Empty.getDefaultInstance(), Tests.<String>nullRef());
    }
}
