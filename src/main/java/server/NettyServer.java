package server;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyServer {
    public static void main(String[] args) {

        // Создаем множество обработчиков (handlers).
        HandlerProvider provider = new HandlerProvider();

        // Создаем пулл потоков для обработки подключающихся клиентов.
        EventLoopGroup auth = new NioEventLoopGroup(1);

        // Создаем пулл потоков для сетевого взаимодействия.
        EventLoopGroup worker = new NioEventLoopGroup();

        try {
            // Создаем ServerBootstrap для осуществления НАСТРОЙКИ СЕРВЕРА.
            ServerBootstrap bootstrap = new ServerBootstrap();

            // Подключаемся к базе дынных.
            AuthService.connect();

            // Говорим серверу использовать два пула, объявленных выше потоков,
            bootstrap.group(auth, worker)
                    // используем NioServerSocketChannel канал для подключения клиентов,
                    .channel(NioServerSocketChannel.class)
                    // настраиваем процесс общения с подключенным клиентом -->
                    // Когда клиент подключится информация о соединении с ним
                    // будет лежать в SocketChannel (адрес, потоки отправки и получения данных).
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        // Инициализируем подключенного клиента.
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            // Handler нужно добавить в конвеер (pipeline).
                            // Добавляем в конец канала getSerializePipeline(), который
                            // инициализирует handler.
                            // Для каждого клиента конвеер будет свой.
                            channel.pipeline().addLast(
                                    provider.getSerializePipeline()
                            );
                        }
                    });
            // Запускаем сервер. b.bind указывает, что сервер должен стартануть
            // на порту 8187. sync - сервер запускается.
            // Через ChannelFuture будем узнавать о дальнейшей судьбе сервера.
            ChannelFuture future = bootstrap.bind(8189).sync();
            log.debug("Server started...");

            // Здесь ожидаем остановки сервера. Это блокирующая операция.
            future.channel().closeFuture().sync(); // block
        } catch (Exception e) {
            log.error("e=", e);
        } finally {
            // После остановки сервера попадаем в данный блок и закрываем
            // два пулла потоков.
            auth.shutdownGracefully();
            worker.shutdownGracefully();
            //Закрываем соединение с базой данных
            AuthService.disconnect();
        }
    }
    // Если мы запустим весь предыдущий код на выполнения
    // сервер запустится, но никаких полезных операций выполнять не будет.
    // Для того, чтобы он мог это делать ему нужны обработчики - handlers.
    // Посылка приходит --> обработчик ее ловит и начинает
    // что-то с ней делать.
}