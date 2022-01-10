package model;

public class DeleteRequest implements AbstractMessage {

    private final String fileName;

    public DeleteRequest(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.DELETE_REQUEST;
    }
}
