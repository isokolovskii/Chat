import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Класс обработки соединения пользователя с сервером. Наследуется от класса {@link Thread}
 *
 * @author Ivan Sokolovskiy
 *
 * @since Version 1.0
 *
 * @version 1.1
 *
 * @see java.lang.Thread
 * @see ChatServer
 */
class ChatHandler extends Thread {

    private final Socket socket;
    private DataInputStream inStream;
    private DataOutputStream outStream;
    private boolean isOn;
    private String username;

    private static final List<ChatHandler> handlers = Collections.synchronizedList(new ArrayList<ChatHandler>());

    /**
     * Конструктор класса ChatHandler. Получает сокет, открытый в классе {@link ChatServer}. Создаёт потоки ввода и
     * вывода {@link ChatHandler#inStream} и {@link ChatHandler#outStream}.
     *
     * @param s Сокет, по которому осуществляется подключение
     *
     * @throws IOException Исключения может возникнуть в процессе получение вхожного и выходного потока данных у сокета
     *
     * @since Version 1.0
     *
     * @see IOException
     * @see ChatServer
     * @see DataInputStream
     * @see DataOutputStream
     * @see Socket
     */
    ChatHandler(Socket s) throws IOException {
        socket = s;
        inStream = new DataInputStream(new BufferedInputStream(s.getInputStream()));
        outStream = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
    }

    /**
     * Запускает процесс обработки соединения с пользователем
     *
     * @since Version 1.0
     *
     * @see Thread#run()
     * @see Thread
     * @see Runnable
     */
    @Override
    public void run() {
        isOn = true;
        try {
            handlers.add(this);
            while (isOn) {
                String msg = inStream.readUTF();
                if (msg.split("#").length == 1) {
                    String[] parts = msg.split(" ");
                    username = parts[parts.length - 1];
                }
                broadcast(msg);
            }
        } catch (IOException e) {
            broadcast(username + " left chat.");
            System.out.println(username + " " + socket.getInetAddress() + " disconnected");
        } finally {
            handlers.remove(this);
            try {
                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Отправляет сообщение, полученное от пользователя остальным участником чата.
     *
     * @param message Сообщение, которое было получено сервером и необходимое для отправки остальным пользователям
     *
     * @since Version 1.0
     *
     * @see ChatServer
     */
    @SuppressWarnings("SynchronizeOnNonFinalField")
    private void broadcast(String message) {
        synchronized (handlers) {
            for (ChatHandler handler : handlers) {
                try {
                    synchronized (handler.outStream) {
                        handler.outStream.writeUTF(message);
                    }
                    handler.outStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    isOn = false;
                }
            }
        }
    }
}
