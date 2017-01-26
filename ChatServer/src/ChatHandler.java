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
 * @version 1.0
 *
 * @see java.lang.Thread
 * @see ChatServer
 */
@SuppressWarnings("WeakerAccess")
public class ChatHandler extends Thread {

    protected final Socket socket;
    protected DataInputStream inStream;
    protected DataOutputStream outStream;
    protected boolean isOn;

    protected static final List<ChatHandler> handlers = Collections.synchronizedList(new ArrayList<ChatHandler>());

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
    public ChatHandler(Socket s) throws IOException {
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
                broadcast(msg);
            }
        } catch (IOException e) {
            System.out.println("User left");
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
