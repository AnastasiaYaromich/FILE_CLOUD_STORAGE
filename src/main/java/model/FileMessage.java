package model;

import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Data
public class FileMessage implements AbstractMessage {

    private final String fileName;
    private final long size;
    private final byte[] bytes;


    public FileMessage(Path path) throws IOException {
        fileName = path.getFileName().toString();
        size = Files.size(path);
        bytes = Files.readAllBytes(path);
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.FILE_MESSAGE;
    }
}