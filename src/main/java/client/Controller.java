package client;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import model.*;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Controller implements Initializable {
    // Панель клиента.
    @FXML public TableView<FileInfo> clientFiles;
    @FXML public TableView<File> serverFiles;
    @FXML public TextField pathClientField;
    @FXML public MenuItem pasteItem;
    @FXML public MenuItem createNew;
    @FXML public Menu menuFile;
    @FXML public AnchorPane createFilePanel;

    private Path baseDir;
    String serverRootPath;
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

    // Панель сервера.
    @FXML Label authLabel;
    @FXML AnchorPane authPanel;
    @FXML TextField loginField;
    @FXML PasswordField passwordField;
    @FXML Button authButton;
    @FXML Button regButton;
    @FXML AnchorPane regPanel;
    @FXML TextField rootRegField;
    @FXML AnchorPane mainPanel;
    @FXML TextField loginRegField;
    @FXML PasswordField passwordRegField;
    @FXML TextField pathServerField;

    @SneakyThrows
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        authPanel.setVisible(true);
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
                        if(item == 4096) {
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
            baseDir = Paths.get(System.getProperty("user.home"));
          //  clientFiles.getItems().addAll(getClientFiles());
            fillClientView(getClientFiles());
            clientFiles.setOnMouseClicked(e -> {
                        if (e.getClickCount() == 2) {
                            FileInfo file = clientFiles.getSelectionModel().getSelectedItem();
                            Path path = baseDir.resolve(file.getFileName());
                            if (file.isDirectory()) {
                                baseDir = path;
                                try {
                                    fillClientView(getClientFiles());
                                } catch (IOException ioException) {
                                    Alert alert = new Alert(Alert.AlertType.ERROR, "\n" +
                                            "Unable to open directory. Access denied!");
                                    baseDir = path.getParent();
                                    try {
                                        fillClientView(getClientFiles());
                                    } catch (IOException exception) {
                                        exception.printStackTrace();
                                    }
                                    alert.showAndWait();
                                }
                            } //else {
                                // ВОЗНИКЛА ПРОБЛЕМА С ОТКРЫТИЕМ ФАЙЛА НА СТОРОНЕ КЛИЕНТА.
//                                File selectedFile = new File(file.getFileName());
//                                try {
//                                    Desktop.getDesktop().open(selectedFile);
//                                } catch (IOException ioException) {
//                                    ioException.printStackTrace();
//                                }
                          //  }
                        }
                    });

                        serverFiles.setOnMouseClicked(e -> {
                            if (e.getClickCount() == 2) {
                                File file = serverFiles.getSelectionModel().getSelectedItem();
                                Path pathRoot = serverRootClientPath.resolve(file.getName());
                                if (file.isDirectory()) {
                                    serverRootClientPath = pathRoot;
                                    System.out.println(serverRootClientPath);
                                    try {
                                        fillServerView(getServerFiles(serverRootClientPath));
                                        sendCurrentServDir();
                                    } catch (IOException ioException) {
                                        ioException.printStackTrace();
                                    }
                                } else {
                                    File selectedFile = new File(file.toString());
                                    try {
                                        Desktop.getDesktop().open(selectedFile);
                                    } catch (IOException ioException) {
                                        ioException.printStackTrace();
                                    }
                                }
                            }
                        });


            Socket socket = new Socket("localhost", 8174);
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());
            Thread thread = new Thread(this::read);
            thread.setDaemon(true);
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void read() {
        try {
            while (true) {
                // Получаем сообщение
                AbstractMessage msg = (AbstractMessage) is.readObject();
                System.out.println(msg.getMessageType());
                // Определяем тип сообщения
                switch (msg.getMessageType()) {
                    // Если сервер прислал файл -->
                    case FILE_MESSAGE:
                        FileMessage fileMessage = (FileMessage) msg;
                        Files.write(
                                baseDir.resolve(fileMessage.getFileName()),
                                fileMessage.getBytes()
                        );
                        // Обновляем список клиентских файлов.
                        Platform.runLater(() -> {
                            try {
                              //  fillServerView(getServerFiles(serverRootClientPath));
                                fillClientView(getClientFiles());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                        break;
                    // Если сервер отправил список файлов на нем -->
                    case FILES_LIST:
                        FilesList files = (FilesList) msg;
                        // Обновляем список файлов на сервере.
                        Platform.runLater(() -> {
                            fillServerView(files.getFiles());
                        });
                        break;
                    case USER_INFO:
                        UserInfo userInfo = (UserInfo) msg;
                        if(!userInfo.getInfo().equals("Account already exist")){
                            System.out.println(userInfo.getInfo());
                            regPanel.setVisible(false);
                            mainPanel.setVisible(true);
                        }
                        if(userInfo.getInfo().equals("Account not exist")) {
                            System.out.println("Account not exist");
                        }
                        break;
                    case AUTHENTICATION_COMPLETE:
                        AuthenticationComplete authenticationComplete = (AuthenticationComplete) msg;
                        serverRootPath = authenticationComplete.getRootUserPath();
                        System.out.println(serverRootPath);
                        serverRootClientPath = Paths.get(serverRootPath);
                        serverFiles.getItems().addAll(getServerFiles(serverRootClientPath));
                        authPanel.setVisible(false);
                        mainPanel.setVisible(true);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fillServerView(List<File> list) {
        pathServerField.setText(serverRootClientPath.toString());
        serverFiles.getItems().clear();
        serverFiles.getItems().addAll(list);
        serverFiles.getItems().sort(new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return new Long(o1.length() - o2.length()).intValue();
            }
        });
    }

    private void fillClientView(List<FileInfo> list) {
        pathClientField.setText(baseDir.toString());
        clientFiles.getItems().clear();
        clientFiles.getItems().addAll(list);
        clientFiles.getItems().sort(new Comparator<FileInfo>() {
            @Override
            public int compare(FileInfo o1, FileInfo o2) {
                return new Long(o1.getSize() - o2.getSize()).intValue();
            }
        });
    }

    private List<FileInfo> getClientFiles() throws IOException {
        pathClientField.setText(baseDir.toString());
        return Files.list(baseDir)
                .map(FileInfo::new)
                .collect(Collectors.toList());
    }

    private List<File> getServerFiles(Path path) throws IOException {
        pathServerField.setText(path.toString());
        File file = new File(path.toString());
        File[] listFiles = file.listFiles();
        List<File> list = Arrays.asList(listFiles);
        return list;
    }

    public void upload(ActionEvent actionEvent) throws IOException {
        FileInfo file = clientFiles.getSelectionModel().getSelectedItem();
        System.out.println(file);
        Path filePath = baseDir.resolve(file.getFileName());
        os.writeObject(new FileMessage(filePath));
    }

    public void download(ActionEvent actionEvent) throws IOException {
        File file = serverFiles.getSelectionModel().getSelectedItem();
        System.out.println(file);
        os.writeObject(new FileRequest(file.getName()));
    }

    private void sendCurrentServDir() throws IOException {
        os.writeObject(new PathTo(serverRootClientPath.toString()));
    }

    public void exit(ActionEvent actionEvent) {
     //   exit.setAccelerator(KeyCombination.keyCombination("CTRL+E"));
        Platform.exit();
    }


    public void btnPathUp(ActionEvent actionEvent) throws IOException {
        Path pathUp = Paths.get(pathClientField.getText()).getParent();
        if (pathUp.compareTo(Paths.get(System.getProperty("user.home"))) >= 0) {
            baseDir = pathUp;
            fillClientView(getClientFiles());
        }
    }

    public void btnPathServerUp(ActionEvent actionEvent) throws IOException {
        Path pathUp = Paths.get(pathServerField.getText()).getParent();
        Path path = Paths.get(serverRootPath);
        if(pathUp.compareTo(path) >= 0) {
            serverRootClientPath = pathUp;
            fillServerView(getServerFiles(serverRootClientPath));
            sendCurrentServDir();
        }
    }

    public void tryToAuth(ActionEvent actionEvent) {
        try {
            os.writeObject(new AuthenticationRequest(loginField.getText(), passwordField.getText()));
            loginField.clear();
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToReg(ActionEvent actionEvent) {
        authPanel.setVisible(false);
        regPanel.setVisible(true);
    }

    public void tryToRegister(ActionEvent actionEvent) {
        try {
                String login = loginRegField.getText();
                String password = passwordRegField.getText();
                os.writeObject(new AddAccount(login, password));
                loginField.clear();
                passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void backToAuthPanel(ActionEvent actionEvent) {
        mainPanel.setVisible(false);
        authPanel.setVisible(true);
    }

    public void backToAuthForm(ActionEvent actionEvent) {
        regPanel.setVisible(false);
        authPanel.setVisible(true);
    }

    public void copyAction(ActionEvent actionEvent) {
        FileInfo fileInfo = clientFiles.getSelectionModel().getSelectedItem();
        if(selectedCopyFile == null && (fileInfo == null || fileInfo.isDirectory())) {
            return;
        }

        if(selectedCopyFile == null) {
            selectedCopyFile = baseDir.resolve(fileInfo.getFileName());
            Alert alert = new Alert(Alert.AlertType.ERROR, "Nothing to copy!");
            alert.showAndWait();
            return;
        }
        if(selectedCopyFile != null) {
            try {
                pasteItem.setOnAction(new EventHandler<ActionEvent>() {
                    @SneakyThrows
                    @Override
                    public void handle(ActionEvent event) {
                        try {
                            Files.copy(selectedCopyFile, baseDir.resolve(selectedCopyFile.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                            selectedCopyFile = null;
                            fillClientView(getClientFiles());
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
                os.writeObject(new DeleteRequest(file.getName()));
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Unable to delete file on Server!");
                alert.showAndWait();
            }
        } else if (clientFiles.getSelectionModel().getSelectedItem() != null) {
            FileInfo fileInfo = clientFiles.getSelectionModel().getSelectedItem();
            if (!fileInfo.isDirectory() || fileInfo != null) {
                try {
                    Files.delete(baseDir.resolve(fileInfo.getFileName()));
                    fillClientView(getClientFiles());
                } catch (IOException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Unable to delete file on Client!");
                    alert.showAndWait();
                }
            }
        }
    }

    public void cutAction(ActionEvent actionEvent)  {
        FileInfo fileInfo = clientFiles.getSelectionModel().getSelectedItem();
        if(selectedMoveFile == null && (fileInfo == null || fileInfo.isDirectory())) {
            return;
        }
        if(selectedMoveFile == null) {
            selectedMoveFile = baseDir.resolve(fileInfo.getFileName());
            return;
        }
            try {
                pasteItem.setOnAction(new EventHandler<ActionEvent>() {
                                          @SneakyThrows
                                          @Override
                                          public void handle(ActionEvent event) {
                                              try {
                                              Files.move(selectedMoveFile, baseDir.resolve(selectedMoveFile.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                                              selectedMoveFile = null;
                                              fillClientView(getClientFiles());
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


    public void createFileOnClient(ActionEvent actionEvent) {
        Label secondLabel = new Label("Enter a name for the new file");
        TextField textField = new TextField("");
        textField.setPrefWidth(50);
        textField.setPrefHeight(30);
        StackPane secondaryLayout = new StackPane();
        secondaryLayout.getChildren().addAll(secondLabel, textField);
        Scene secondScene = new Scene(secondaryLayout, 300, 150);
        secondScene.setUserAgentStylesheet("/DarkTheme.css");
        Stage newWindow = new Stage();
        newWindow.setTitle("Choose file name");
        newWindow.setScene(secondScene);
        newWindow.show();

        String fileExtension = "txt";
        try {
            System.out.println(baseDir);
            File file = new File(baseDir + "/" + "New text document" + "." + fileExtension);
            file.createNewFile();
            fillClientView(getClientFiles());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}



