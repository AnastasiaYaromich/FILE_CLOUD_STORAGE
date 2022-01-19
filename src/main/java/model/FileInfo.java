package model;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class FileInfo implements AbstractMessage {
    private final List<FileInfo> files;

    // Формируем список файлов.
    public FileInfo(Path path) throws IOException {
//        files = Files.list(path)
//                .map(p -> p.toFile())
//                .collect(Collectors.toList());
    }


    @Override
    public MessageType getMessageType() {
        return MessageType.FILE_INFO;
    }
}
