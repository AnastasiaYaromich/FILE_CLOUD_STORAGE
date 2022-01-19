package model;

import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class FilesList implements AbstractMessage {
 private final List<File> files;

    // Формируем список файлов.
    public FilesList(Path path) throws IOException {
      files = Files.list(path)
                .map(p -> p.toFile())
                .collect(Collectors.toList());
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.FILES_LIST;
    }
}
