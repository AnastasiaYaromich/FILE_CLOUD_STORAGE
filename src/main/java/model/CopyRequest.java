package model;

public class CopyRequest implements AbstractMessage {
    private final String fileName;

    public CopyRequest(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.COPY_REQUEST;
    }
}
