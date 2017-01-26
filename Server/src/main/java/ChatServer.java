import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

/**
 * Класс серверной программы
 *
 * @author Ivan Sokolovskiy
 *
 * @since Version 1.0
 *
 * @version 1.0
 *
 * @see ChatHandler
 */
@SuppressWarnings("WeakerAccess")
public class ChatServer {

    /**
     * Конструктор сервера. Получает порт, создаёт сокет на заданном порту, слушает запросы и создаёт обработчика
     * запросов {@link ChatHandler} на каждое соединение.
     *
     * @param port Порт, который слушает сервер.
     *
     * @since Version 0.1
     *
     * @see ServerSocket
     * @see Socket
     * @see ChatHandler
     */
    public ChatServer(int port) {

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            //noinspection InfiniteLoopStatement
            while (true) {
                Socket s = serverSocket.accept();
                System.out.println("Accepted from " + s.getInetAddress());
                ChatHandler handler = new ChatHandler(s);
                handler.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Точка входна серверной программы. Запускает сам сервер.
     *
     * @param args Аргументы командной строки. В программе не используются
     * @throws IOException Искючение может возникнуть в случае отсутствия файла конфигурации сервера.
     *
     * @see Properties
     * @see FileInputStream
     * @see IOException
     */
    public static void main(String[] args) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream("connection.properties"));
        String port = properties.getProperty("port");
        System.out.println("Server started");
        new ChatServer(Integer.parseInt(port));
    }
}