package model;

public class RegistrationComplete implements AbstractMessage {
    private final String rootUserPath;

    public RegistrationComplete(String rootUserPath) {
        this.rootUserPath = rootUserPath;
    }

    public String getRootUserPath() {
        return rootUserPath;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.REGISTRATION_COMPLETE;
    }
}
