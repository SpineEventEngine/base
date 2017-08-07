package io.spine.protobuf;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * Signals an error working with a Protobuf message field.
 *
 * @author Alexander Yevsyukov
 */
public class MessageFieldException extends RuntimeException {

    private static final long serialVersionUID = 0L;
    private final GeneratedMessageV3 protobufMessage;

    /**
     * Constructs a {@code MessageFieldException} with the formatted message text.
     *
     * @param protobufMessage
     *        a Protobuf message object working with a field of which caused an error
     * @param errorMessageFormat
     *        a format string for the error message
     * @param params
     *        error message parameters
     */
    public MessageFieldException(Message protobufMessage,
                                 String errorMessageFormat,
                                 Object... params) {
        super(format(checkNotNull(errorMessageFormat), params));
        this.protobufMessage = (GeneratedMessageV3) checkNotNull(protobufMessage);
    }

    /**
     * Constructs a {@code MessageFieldException} without no message text.
     *
     * @param protobufMessage
     *        a Protobuf message object working with a field of which caused an error
     */
    public MessageFieldException(Message protobufMessage) {
        this(protobufMessage, "");
    }

    /**
     * Obtains a Protobuf message working with a field of which caused an error
     */
    public Message getProtobufMessage() {
        return protobufMessage;
    }
}
