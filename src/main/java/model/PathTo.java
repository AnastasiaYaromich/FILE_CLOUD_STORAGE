package model;

public class PathTo implements AbstractMessage {
    private final String pathTo;

    public PathTo(String pathTo) {
        this.pathTo = pathTo;
    }

    public String getPathTo() {
        return pathTo;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.PATH_TO;
    }
}
