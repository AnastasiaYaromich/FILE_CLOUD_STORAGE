package server;
import java.sql.*;

public class AuthService {
    // Объект, позволяющий установить соеднинение между
    // приложением и базой данных.
    private static Connection connection;

   // Объект, с помощью которого отправляются запросы
    // в базу данных и получается рeзyльтат.
   private static Statement statement;

   // Метод, реализующий подключение к базе данных.
   public static void connect() {
       try {
           // Обращаемся к давайверу JDBS для того, чтобы
           // произошла его инициализация.
           Class.forName("org.sqlite.JDBC");
           // Осуществляем инициализацию сonnection.
           connection = DriverManager.getConnection("jdbc:sqlite:main.db");
           statement = connection.createStatement();
       } catch (Exception e) {
           e.printStackTrace();
       }
   }

   // Метод, реализующий подключение клиента на логину и паролю.
    public static String authentication(String login, String password) {
        String request = String.format("SELECT root FROM users WHERE login = '%s' AND password = '%s'", login, password);
        try {
            // Направляем запрос с БД о подключении клиента с переданным логином и паролем
            // и возвращаем рузельтата.
            ResultSet resultSet = statement.executeQuery(request);
            // Если запрос не равен null, возвращаем результат запроса.
            if (resultSet.next()) {
                return resultSet.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    //  Метод, реализующий регистрацию клиента.
    public static void addAccount(String login, String password, String root) throws SQLException {
       // Проверка не существует ли клиент с данным логином и паролем.
       String account = getAccount(login, password);
       // Если клиента с указанными логином и паролем не существует, регистрируем его в базе данных.
        if (account == null){
            try {
                String request = String.format("INSERT INTO users (login, password, root) VALUES " +
                        "('%s', '%s', '%s');", login, password, root);
                PreparedStatement ps = connection.prepareStatement(request);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        // Если клиента с указанными логином и паролем уже зарегистрирован в базе данных.
        if (account != null){
            if (account.equals(account)){
                throw new SQLException("Аккаунт уже зарегистрирован");
            }
        }
    }

    // Метод, реализующий поиск клиента по логину и паролю.
    public static String getAccount(String login, String password) {
       try {
           String request = String.format("SELECT login, password FROM users WHERE login = '%s' AND password = '%s' ;", login, password);
           ResultSet resultSet = statement.executeQuery(request);
           if (resultSet.next()) {
               String account = resultSet.getString(1);
               return account;
           }
       } catch (SQLException e) {
           e.printStackTrace();
       }
        return null;
    }

    public static String checkAccount(String login) {
       try {
           String request = String.format("SELECT login FROM users WHERE login = '%s';", login);
           ResultSet resultSet = statement.executeQuery(request);
           System.out.println(resultSet);
           if(resultSet.next()) {
               String account = resultSet.toString();
               return account;
           }
       } catch (Exception e) {
           e.printStackTrace();
       }
       return null;
    }

   // Метод, реализующий отключение от базы данных.
   public static void disconnect() {
       try {
           // Закрываем соединение с базой данных.
           connection.close();
       } catch (SQLException e) {
           e.printStackTrace();
       }
   }
}
