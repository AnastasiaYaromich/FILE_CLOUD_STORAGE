package client;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.Setter;
import lombok.SneakyThrows;
import model.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class MainController implements  MessageProcessor {
    // Панель клиента.
    @FXML public TableView<FileInfo> clientFiles;
    @FXML public TableView<File> serverFiles;
    @FXML public TextField pathClientField;
    @FXML public MenuItem pasteItem;
    @FXML public MenuItem createNew;
    @FXML public Menu menuFile;
    @FXML public AnchorPane createFilePanel;

    private Path selectedCopyFile;
    private Path selectedMoveFile;
    private Path serverRootClientPath;
    // Обмен командами.
    private ObjectDecoderInputStream is;
    private ObjectEncoderOutputStream os;
    // Обмен байтами.
    private BufferedInputStream bis;
    private BufferedOutputStream bos;
    // Иконки папок и файлов.
    private ImageView imageView;
    private Image image;

    @FXML AnchorPane mainPanel;
    @FXML TextField pathServerField;

    @Setter private MessageService messageService;
    @Setter private Stage stage;

    @Override
    public void processMessage(AbstractMessage message) {
        try {
            switch (message.getMessageType()) {
                case FILE_MESSAGE:
                    FileMessage fileMessage = (FileMessage) message;
                    Files.write(
                            messageService.getBaseDir().resolve(fileMessage.getFileName()),
                            fileMessage.getBytes()
                    );
                    // Обновляем список клиентских файлов -->
                    Platform.runLater(() -> {
                        try {
                            fillClientView(getClientFiles());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    break;
       //          Если сервер отправил список файлов на нем -->
                case FILES_LIST:
                    FilesList files = (FilesList) message;
                    // Обновляем список файлов на сервере -->
                    Platform.runLater(() -> {
                        fillServerView(files.getFiles());
                    });
                    break;
                case USER_INFO:
                    UserInfo info = (UserInfo) message;
                    pathServerField.setText(info.getInfo());
                    break;
                case REFRESH_CLIENT_VIEW:
                    RefreshClientView refreshClientView = (RefreshClientView) message;
                    Platform.runLater(() -> {
                        try {
                            fillClientView(getClientFiles());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    public void launch() throws IOException {
        TableColumn<FileInfo, String> typeImageColumn = new TableColumn<>("Type");
        typeImageColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType()));
        typeImageColumn.setPrefWidth(40);
        typeImageColumn.setCellFactory(column -> {
            return new TableCell<FileInfo, String>() {
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                        setGraphic(null);
                    } else {
                        if (item.equals("DIR")) {
                            image = new Image(getClass().getResourceAsStream("papka.png"));
                            ImageView imageView =new ImageView(image);
                            setGraphic(imageView);
                        } else {
                            if(item.equals("F")) {
                                image = new Image(getClass().getResourceAsStream("file3.png"));
                                ImageView imageView =new ImageView(image);
                                setGraphic(imageView);
                            }
                            else setGraphic(null);

                        }
                    }
                }
            };
        });
        clientFiles.getColumns().add(typeImageColumn);

        TableColumn<File, File> typeServColumn = new TableColumn<>("Type");
        typeServColumn.setCellValueFactory(param -> new SimpleObjectProperty<File>(param.getValue()));
        typeServColumn.setPrefWidth(40);
        typeServColumn.setCellFactory(column -> {
            return new TableCell<File, File>() {
                protected void updateItem(File item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                        setGraphic(null);
                    } else {
                        if (item.isDirectory()) {
                            image = new Image(getClass().getResourceAsStream("papka.png"));
                            ImageView imageView =new ImageView(image);
                            setGraphic(imageView);
                        } else {
                            if(item.isFile()) {
                                image = new Image(getClass().getResourceAsStream("file3.png"));
                                ImageView imageView = new ImageView(image);
                                setGraphic(imageView);
                            }
                            else setGraphic(null);

                        }
                    }
                }
            };
        });
        serverFiles.getColumns().add(typeServColumn);

        TableColumn<File, String> nameServColumn = new TableColumn<>("Name");
        nameServColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));
        nameServColumn.setPrefWidth(100);
        serverFiles.getColumns().add(nameServColumn);

        TableColumn<FileInfo, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));
        nameColumn.setPrefWidth(100);
        clientFiles.getColumns().add(nameColumn);

        TableColumn<File, Long> sizeServColumn = new TableColumn<>("Size");
        sizeServColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().length()));
        sizeServColumn.setPrefWidth(90);
        sizeServColumn.setCellFactory(column -> {
            return new TableCell<File, Long>() {
                protected void updateItem(Long item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        String text = String.format("%,d bytes", item);
                        if (item == 4096) {
                            text = "[DIR]";
                        }
                        setText(text);
                    }
                }
            };
        });
        serverFiles.getColumns().add(sizeServColumn);

        TableColumn<FileInfo, Long> sizeColumn = new TableColumn<>("Size");
        sizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        sizeColumn.setPrefWidth(90);
        sizeColumn.setCellFactory(column -> {
            return new TableCell<FileInfo, Long>() {
                protected void updateItem(Long item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        String text = String.format("%,d bytes", item);
                        if (item == -1) {
                            text = "[DIR]";
                        }
                        setText(text);
                    }
                }
            };
        });
        clientFiles.getColumns().add(sizeColumn);

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        TableColumn<FileInfo, String> dateColumn = new TableColumn<>("Last modified");
        dateColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified().format(dateTimeFormatter)));
        dateColumn.setPrefWidth(120);
        clientFiles.getColumns().add(dateColumn);

        TableColumn<File, File> dateServColumn = new TableColumn<>("Last modified");
        dateServColumn.setCellValueFactory(param -> new SimpleObjectProperty<File>(param.getValue()));
        dateServColumn.setPrefWidth(120);
        dateServColumn.setCellFactory(column -> {
            return new TableCell<File, File>() {
                protected void updateItem(File item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        if(item != null) {
                            Date lm = new Date(item.lastModified());
                            String lasmod = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(lm);
                            setText(lasmod);
                        }
                    }
                }
            };
        });
        serverFiles.getColumns().add(dateServColumn);

        try {
            fillClientView(getClientFiles());
            clientFiles.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    FileInfo file = clientFiles.getSelectionModel().getSelectedItem();
                    Path path = messageService.getBaseDir().resolve(file.getFileName());
                    if (file.isDirectory()) {
                        messageService.setBaseDir(path);
                        try {
                            fillClientView(getClientFiles());
                        } catch (IOException ioException) {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "\n" +
                                    "Unable to open this directory. Access denied!");
                            messageService.setBaseDir(path.getParent());
                            try {
                                fillClientView(getClientFiles());
                            } catch (IOException exception) {
                                exception.printStackTrace();
                            }
                            alert.showAndWait();
                        }
                    }
                }
            });

            serverFiles.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    File file = serverFiles.getSelectionModel().getSelectedItem();
                    try {
                        messageService.sendMessage(new ChangeDirectory(file.toString()));
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void fillServerView(List<File> list) {
        serverFiles.getItems().clear();
        serverFiles.getItems().addAll(list);
        serverFiles.getItems().sort(new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return (o1.isDirectory() ? 1 : -1) - (o2.isDirectory() ? 1 : -1);
            }
        });
        serverFiles.getItems().sort(new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return new Long(o1.length() - o2.length()).intValue();
            }
        });
    }

    public void fillClientView(List<FileInfo> list) {
        pathClientField.setText(messageService.getBaseDir().toString());
        clientFiles.getItems().clear();
        clientFiles.getItems().addAll(list);
        clientFiles.getItems().sort(new Comparator<FileInfo>() {
            @Override
            public int compare(FileInfo o1, FileInfo o2) {
                return new Long(o1.getSize() - o2.getSize()).intValue();
            }
        });
    }

    public List<FileInfo> getClientFiles() throws IOException {
        pathClientField.setText(messageService.getBaseDir().toString());
        return Files.list(messageService.getBaseDir())
                .map(FileInfo::new)
                .collect(Collectors.toList());
    }

    public List<File> getServerFiles(Path path) throws IOException {
        File file = new File(path.toString());
        File[] listFiles = file.listFiles();
        List<File> list = Arrays.asList(listFiles);
        return list;
    }

    public void upload(ActionEvent actionEvent) throws IOException {
        FileInfo file = clientFiles.getSelectionModel().getSelectedItem();
        Path filePath = messageService.getBaseDir().resolve(file.getFileName());
        messageService.getOs().writeObject(new FileMessage(filePath));
    }

    public void download(ActionEvent actionEvent) throws IOException {
        File file = serverFiles.getSelectionModel().getSelectedItem();
        System.out.println(file);
        messageService.getOs().writeObject(new FileRequest(file.getName()));
    }

    public void exit(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void btnPathUp(ActionEvent actionEvent) throws IOException {
        Path pathUp = Paths.get(pathClientField.getText()).getParent();
        if (pathUp.compareTo(Paths.get(System.getProperty("user.home"))) >= 0) {
            messageService.setBaseDir(pathUp);
            fillClientView(getClientFiles());
        }
    }

    public void btnPathServerUp(ActionEvent actionEvent) throws IOException {
        try {
            messageService.sendMessage(new ChangeDirectoryUp());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void backToAuthPanel(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("auth.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.show();
        System.out.println(stage);
        AuthController authController = loader.getController();
        authController.setMessageService(messageService);
        messageService.setMessageProcessor(authController);
        authController.setStage(stage);

    }

    public void copyAction(ActionEvent actionEvent) throws IOException {
        if (serverFiles.getSelectionModel().getSelectedItem() != null) {
            File file = serverFiles.getSelectionModel().getSelectedItem();;
            messageService.getOs().writeObject(new CopyRequest(file.getName()));
            try {
                pasteItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        try {
                            messageService.getOs().writeObject(new PasteRequest(file.getName(), "Copy"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Unable to copy file!");
                alert.showAndWait();
            }
        }
            if (clientFiles.getSelectionModel().getSelectedItem() != null) {
                FileInfo fileInfo = clientFiles.getSelectionModel().getSelectedItem();
                if (selectedCopyFile == null && (fileInfo == null || fileInfo.isDirectory())) {
                    return;
                }
                if (selectedCopyFile == null) {
                    selectedCopyFile = messageService.getBaseDir().resolve(fileInfo.getFileName());
                }
                try {
                    pasteItem.setOnAction(new EventHandler<ActionEvent>() {
                        @SneakyThrows
                        @Override
                        public void handle(ActionEvent event) {
                            try {
                                Files.copy(selectedCopyFile, messageService.getBaseDir().resolve(selectedCopyFile.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                                fillClientView(getClientFiles());
                                selectedCopyFile = null;
                            } catch (IOException e) {
                                Alert alert = new Alert(Alert.AlertType.ERROR, "Unable to paste file!");
                                alert.showAndWait();
                            }
                        }
                    });
                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Unable to copy file!");
                    alert.showAndWait();
                }
            }
        }

    public void deleteAction(ActionEvent actionEvent)  {
        if (serverFiles.getSelectionModel().getSelectedItem() != null) {
            File file = serverFiles.getSelectionModel().getSelectedItem();
            try {
                messageService.getOs().writeObject(new DeleteRequest(file.getName()));
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Unable to delete file on Server!");
                alert.showAndWait();
            }
        } else if (clientFiles.getSelectionModel().getSelectedItem() != null) {
            FileInfo fileInfo = clientFiles.getSelectionModel().getSelectedItem();
            if (!fileInfo.isDirectory()) {
                try {
                    Files.delete(messageService.getBaseDir().resolve(fileInfo.getFileName()));
                    fillClientView(getClientFiles());
                } catch (IOException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Unable to delete file on Client!");
                    alert.showAndWait();
                }
            }
        }
    }

    public void cutAction(ActionEvent actionEvent) throws IOException {
        if (serverFiles.getSelectionModel().getSelectedItem() != null) {
            File file = serverFiles.getSelectionModel().getSelectedItem();;
            messageService.getOs().writeObject(new CutRequest(file.getName()));
            try {
                pasteItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        try {
                            messageService.getOs().writeObject(new PasteRequest(file.getName(), "Cut"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Unable to copy file!");
                alert.showAndWait();
            }
        }
        FileInfo fileInfo = clientFiles.getSelectionModel().getSelectedItem();
        if(selectedMoveFile == null && (fileInfo == null || fileInfo.isDirectory())) {
            return;
        }
        if(selectedMoveFile == null) {
            selectedMoveFile = messageService.getBaseDir().resolve(fileInfo.getFileName());
        }
        try {
            pasteItem.setOnAction(new EventHandler<ActionEvent>() {
                @SneakyThrows
                @Override
                public void handle(ActionEvent event) {
                    try {
                        Files.move(selectedMoveFile, messageService.getBaseDir().resolve(selectedMoveFile.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                        fillClientView(getClientFiles());
                        selectedMoveFile = null;
                    } catch (IOException e) {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Unable to paste file!");
                        alert.showAndWait();
                    }
                }
            });
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Unable to cut file!");
            alert.showAndWait();
        }
    }

    public void createFolder(ActionEvent actionEvent) throws IOException {
        Stage folderStage = new Stage();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(this.getClass().getResource("folder.fxml"));
        Parent parent = loader.load();
        FolderController controller = loader.getController();
        controller.setStage(folderStage);
        controller.setMessageService(messageService);
        controller.launch();
        Scene scene = new Scene(parent);
        folderStage.setScene(scene);
        folderStage.getIcons().add(new Image(getClass().getResourceAsStream("listik.png")));
        folderStage.setTitle("New folder");
        folderStage.show();
    }

    public void createFile(ActionEvent actionEvent) throws IOException {
        Stage fileStage = new Stage();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(this.getClass().getResource("file.fxml"));
        Parent parent = loader.load();
        FileController controller = loader.getController();
        controller.setStage(fileStage);
        controller.setMessageService(messageService);
        controller.launch();
        Scene scene = new Scene(parent);
        fileStage.setScene(scene);
        fileStage.getIcons().add(new Image(getClass().getResourceAsStream("listik.png")));
        fileStage.setTitle("New file");
        fileStage.show();
    }

    public void aboutFileStorage(ActionEvent actionEvent) throws IOException {
        Stage aboutStage = new Stage();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(this.getClass().getResource("aboutProgram.fxml"));
        Parent parent = loader.load();
        AboutProgramController controller = loader.getController();
        controller.setStage(aboutStage);
        controller.setMessageService(messageService);
        Scene scene = new Scene(parent);
        aboutStage.setScene(scene);
        aboutStage.getIcons().add(new Image(getClass().getResourceAsStream("listik.png")));
        aboutStage.setTitle("About File Storage");
        aboutStage.show();
    }
}




