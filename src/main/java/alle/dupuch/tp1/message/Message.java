package alle.dupuch.tp1.message;

import java.util.Objects;

public class Message {

    private MessageType type;
    private int sender;
    private int receiver;
    private boolean isSuccess = false;

    public Message(int sender, int receiver, MessageType type, boolean isSuccess) {

        this.sender = sender;
        this.receiver = receiver;
        this.type = type;
        this.isSuccess = isSuccess;
    }

    public int getReceiver() {
        return receiver;
    }

    public MessageType getType() {
        return type;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public int getSender() {
        return sender;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return sender == message.sender && receiver == message.receiver && type == message.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, sender, receiver);
    }
}
