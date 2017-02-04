import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
 * @version 1.1
 *
 * @see ChatHandler
 */
class ChatServer extends Thread {

    private volatile ServerSocket serverSocket;
    private volatile boolean isOn = true;

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
    ChatServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            start();
        } catch (IOException e) {
            System.err.println("Error starting server");
            e.printStackTrace();
        }
    }

    /**
     * Запуск потока обработки входных сигналов сокета
     *
     * If this thread was constructed using a separate
     * <code>Runnable</code> run object, then that
     * <code>Runnable</code> object's <code>run</code> method is called;
     * otherwise, this method does nothing and returns.
     * <p>
     * Subclasses of <code>Thread</code> should override this method.
     *
     * @see #start()
     * @see #stop()
     */
    @Override
    public void run() {
        while (isOn) {
            try {
                Socket s = serverSocket.accept();
                System.out.println("Accepted from " + s.getInetAddress());
                ChatHandler handler = new ChatHandler(s);
                handler.start();
            } catch (IOException e) {
                System.err.println("Error while accepting socket");
                e.printStackTrace();
            }

        }
    }

    /**
     * @return Возвращает значение IP сервера
     */
    String getAddress() {
        return serverSocket.getInetAddress().toString();
    }

    /**
     * Метод остановки сервера. Приводит к образованию исключения в главном
     * потоке выполнения жизненного цикла сервера, приводя его к остановке.
     */
    synchronized void stopServer() {
        isOn = false;
        try {
            serverSocket.close();
            interrupt();
            serverSocket = null;
        } catch (IOException e) {
            System.err.println("Error closing server socket");
            e.printStackTrace();
        }
    }

    /**
     * Консольная версия серверной программы
     * @param args аргументы командной строки, не используется
     */
    public static void main(String[] args) {
        Properties properties = new Properties();
        File propFile = new File("config.properties");
        if (!propFile.exists()) {
            properties.setProperty("port", "8082");
            try {
                if (!propFile.createNewFile())
                    System.err.println("Error making config file");
                properties.store(new FileOutputStream(propFile), "Server config");
            } catch (IOException e) {
                System.err.println("Error loading config");
                e.printStackTrace();
            }
        }
        else try {
            properties.load(new FileInputStream(propFile));
        } catch (IOException e) {
            System.err.println("Error loading config file");
            e.printStackTrace();
        }
        System.out.println("Server started");
        new ChatServer(Integer.parseInt(properties.getProperty("port")));
    }
}