package model;

public class AuthenticationComplete implements AbstractMessage{

    private final String rootUserPath;

    public AuthenticationComplete(String rootUserPath) {
        this.rootUserPath = rootUserPath;
    }

    public String getRootUserPath() {
        return rootUserPath;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.AUTHENTICATION_COMPLETE;
    }
}
