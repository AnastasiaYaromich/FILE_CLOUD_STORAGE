package client;

import javafx.scene.image.ImageView;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class FileInfo {
    private final String fileName;
    private final boolean isDirectory;
    private final long size;
    private LocalDateTime lastModified;
    private String type;
    private ImageView typeImageView;


    public FileInfo(Path path) {
            fileName = path.getFileName().toString();
            isDirectory = Files.isDirectory(path);
            if (!isDirectory) {
                type = "F";
                typeImageView = new ImageView();
                size = path.toFile().length();
            } else {
                type = "DIR";
                typeImageView = null;
                size = -1;
            }
            try {
            lastModified = LocalDateTime.ofInstant(Files.getLastModifiedTime(path).toInstant(), ZoneOffset.ofHours(3));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String toString() {
        return getType() + " " + getFileName() + " " + getSize() + " " + getLastModified();
    }

    public String getType() {
       return type;
    }

    public ImageView getTypeImage() {
        return typeImageView;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public long getSize() {
        return size;
    }
}