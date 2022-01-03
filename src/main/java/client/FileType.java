package client;

public enum FileType {
    FILE("F"), DIRECTORY("D");

    private String name;

    FileType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
