package server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import model.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

// Для того, чтобы создать handler обрабатывал входящие данные,
// наследуемся от SimpleChannelInboundHandler. Inbound означает,
// что обрабатываться будут входящие данные.
public class AbstractMessageHandler extends SimpleChannelInboundHandler<AbstractMessage> {
    private Path currentPath;
    private Path userRootPath;
    private Path selectedCopyFile;
    private Path selectedCutFile;

    public AbstractMessageHandler() {
        currentPath = Paths.get("ServerStorage");
    }

    // Когда клиент подключается в обработчике срабатывает channelActive.
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Отправляем клиенту список файлов, которые есть на сервере.
        System.out.println("chanelActive");
      //  ctx.writeAndFlush(new FilesList(userRootPath));
    }

    // Когда клиент прислал какое-то сообщение срабатывает channelRead0.
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractMessage message) throws Exception {
      //  log.debug("received: {}", message);
        // Для того, чтобы прочитать сообщение нужно определить его тип.
        switch (message.getMessageType()) {
            case AUTHENTICATION_REQUEST:
            // Клиент отправил запрос на авторизацию.
                AuthenticationRequest authenticationRequest = (AuthenticationRequest) message;
                // Ищем в базе данных клиента с данными логином и паролем.
                if(AuthService.authentication(authenticationRequest.getLogin(), authenticationRequest.getPassword()) == null) {
                    // Если клиент с данными логином и паролем в базе отсутствуют, отправляем сообщение, что
                    // клиент не зарегистрирован.
                    ctx.writeAndFlush(new UserInfo("Account not exist."));
                    break;
                } else {
                    // Если клиент с данными логином и паролем в базе присутствуют возвращаем путь к корневой папке клиента
                    // на сервере.
                    userRootPath = Paths.get(currentPath + "/" + authenticationRequest.getLogin());
                    ctx.writeAndFlush(new AuthenticationComplete(userRootPath.toString()));
                    ctx.writeAndFlush(new FilesList(userRootPath));
                    break;
                }
            case ADD_ACCOUNT:
                // Клиент отправил запрос на регистрацию.
                AddAccount addAccount = (AddAccount) message;
                // Проверка не зарегистрирован ли клиент с таким именем.
                if(AuthService.checkAccount(addAccount.getLogin()) == null) {
                    // Если клиент не зарегистирован создаем для него корневую
                    // директорию.
                    userRootPath = Paths.get(currentPath + "/" + addAccount.getLogin());
              //      System.out.println(userRootPath);
                    // Добавляем аккаунт клиента в базу данных.
                    AuthService.addAccount(addAccount.getLogin(), addAccount.getPassword(), userRootPath.toString());
                    // Создаем корневую директорию клиента на сервере.
                    Files.createDirectory(userRootPath);
                    // Отправляем клиенту сообщение, что регистриция завершена.
                    ctx.writeAndFlush(new RegistrationComplete(userRootPath.toString()));
                    // Возвращаем клиент обновленный список файлов на сервере.
                   ctx.writeAndFlush(new FilesList(userRootPath));
                } else {
                    // Если клиент с данным логином уже существует, отправляем сообщение, что данный клиент уже
                    // существует.
                    ctx.writeAndFlush(new UserInfo("Account already exist"));
                }
                break;
                // Если сообщение клиента является отправкой его текущей директории на сервере
            case CHANGE_DIRECTORY:
                ChangeDirectory changeDirectory = (ChangeDirectory) message;
                Path pathRoot = Paths.get(changeDirectory.getDirName());
                if(Files.isDirectory(pathRoot)) {
                    userRootPath = pathRoot;
                    ctx.writeAndFlush(new FilesList(userRootPath));
                    ctx.writeAndFlush(new UserInfo(userRootPath.toString()));
                } else {
                    try {
                        File file = new File(pathRoot.toString());
                        Desktop.getDesktop().open(file);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case CHANGE_DIRECTORY_UP:
                if(!userRootPath.getParent().equals(currentPath)) {
                    userRootPath = userRootPath.getParent();
                    ctx.writeAndFlush(new FilesList(userRootPath));
                    ctx.writeAndFlush(new UserInfo(userRootPath.toString()));
                }
                break;
            case CREATE_FOLDER:
                CreateFolder createFolder = (CreateFolder) message;
                Files.createDirectory(userRootPath.resolve(createFolder.getFolderName()));
               ctx.writeAndFlush(new FilesList(userRootPath));
                break;
            case CREATE_FILE:
                CreateFile createFile = (CreateFile) message;
                Files.createFile(userRootPath.resolve(createFile.getFileName()));
                ctx.writeAndFlush(new FilesList(userRootPath));
                break;
            case FILE_REQUEST:
                FileRequest req = (FileRequest) message;
                // Отправляем клиенту запрашиваемый файл.
                ctx.writeAndFlush(new FileMessage(userRootPath.resolve(req.getFileName())));
                break;
                // Если сообщение клиента является просьбой удалить файл -->
            case DELETE_REQUEST:
                DeleteRequest del = (DeleteRequest) message;
                // Удаляем файл.
                Files.delete(userRootPath.resolve(del.getFileName()));
                // Возвращаем клиенту обновленный список файлов на сервере.
                ctx.writeAndFlush(new FilesList(userRootPath));
                break;
            case COPY_REQUEST:
                CopyRequest copyRequest = (CopyRequest) message;
                selectedCopyFile = userRootPath.resolve(copyRequest.getFileName());
                break;
            case PASTE_REQUEST:
                PasteRequest pasteRequest = (PasteRequest) message;
                if(pasteRequest.getType().equals("Copy")) {
                    Files.copy(selectedCopyFile, userRootPath.resolve(selectedCopyFile.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                    ctx.writeAndFlush(new FilesList(userRootPath));
                }
                if(pasteRequest.getType().equals("Cut")) {
                    Files.move(selectedCutFile, userRootPath.resolve(selectedCutFile.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                    ctx.writeAndFlush(new FilesList(userRootPath));
                }
                break;
            case CUT_REQUEST:
                CutRequest cutRequest = (CutRequest) message;
                selectedCutFile = userRootPath.resolve(cutRequest.getFileName());
                break;
                // Если сообщение клиента является посылкой файла -->
            case FILE_MESSAGE:
                FileMessage fileMessage = (FileMessage) message;
                // Записываем файл на сервер.
                Files.write(userRootPath.resolve(fileMessage.getFileName()), fileMessage.getBytes());
                // Возвращаем клиенту обновленный список файлов на сервере.
                ctx.writeAndFlush(new FilesList(userRootPath));
                break;
            case REFRESH_CLIENT_VIEW:
                RefreshClientView refreshClientView = (RefreshClientView) message;
                ctx.writeAndFlush(new RefreshClientView());
                break;
        }
    }
}