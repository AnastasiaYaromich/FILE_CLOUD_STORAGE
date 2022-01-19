package model;

public class ChangeDirectoryUp implements AbstractMessage {
    @Override
    public MessageType getMessageType() {
        return MessageType.CHANGE_DIRECTORY_UP;
    }
}
