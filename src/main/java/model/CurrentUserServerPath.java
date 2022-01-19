package model;
import lombok.Getter;

import java.nio.file.Path;

public class CurrentUserServerPath implements AbstractMessage {
    @Getter
    private final Path currentUserRootPath;

    public CurrentUserServerPath(Path currentUserRootPath) {
        this.currentUserRootPath = currentUserRootPath;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.CURRENT_USER_ROOT_PATH;
    }
}
