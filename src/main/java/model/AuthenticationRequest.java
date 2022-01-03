package model;

public class AuthenticationRequest implements AbstractMessage{
    String login;
    String password;

    public AuthenticationRequest(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.AUTHENTICATION_REQUEST;
    }
}
