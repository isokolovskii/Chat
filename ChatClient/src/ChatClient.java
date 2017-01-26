import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.util.Properties;

/** Класс клиентского приложения. Наследуется от {@link JFrame} и реализует интерфейс {@link Runnable} для работы с
 * многопоточностью
 *
 * @author Ivan Sokolovskiy
 *
 * @version 1.0
 *
 * @since Version 1.0
 *
 * @see JFrame
 * @see Runnable
 */
//todo Добавить диалоговое окно, спрашивающее имя пользователя
//todo Добавить отображение имён пользователей в чате
//todo Отображение имени пользователя когда он заходит в чат
//todo Добавить отображение времени сообщения
@SuppressWarnings("WeakerAccess")
public class ChatClient extends JFrame implements Runnable {

    /**
     * @see JTextArea
     * @see JTextField
     */
    protected final Socket socket;
    protected final DataInputStream inStream;
    protected final DataOutputStream outStream;
    protected final JTextArea outTextArea;
    protected final JTextField inTextField;
    protected boolean isOn;

    /**
     * Конструктор класса. Получаем на вход название окна, сокет соединения, входной и выходной поток данных.
     *
     * @param tittle Название окна. См. также {@link JFrame#title}
     * @param s Сокет соединения с сервером
     * @param in Входной поток данных
     * @param out Выходной поток данных
     *
     * @since Version 1.0
     *
     * @see Socket
     * @see JFrame
     * @see DataInputStream
     * @see DataOutputStream
     */
    public ChatClient(String tittle, Socket s, DataInputStream in, DataOutputStream out) {
        super(tittle);
        socket = s;
        inStream = in;
        outStream = out;

        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());
        cp.add(BorderLayout.CENTER, outTextArea = new JTextArea());
        outTextArea.setEnabled(false);
        cp.add(BorderLayout.SOUTH, inTextField = new JTextField());

        inTextField.addActionListener(e -> {
            try {
                outStream.writeUTF(inTextField.getText());
                outStream.flush();
            } catch (IOException e1) {
                e1.printStackTrace();
                isOn = false;
            }
            inTextField.setText("");
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                isOn = false;
                try {
                    outStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                try {
                    socket.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        });

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(600, 600);
        setVisible(true);
        inTextField.requestFocus();
        (new Thread(this)).start();
    }

    /**
     * Запускает процесс общение приложения с сервером
     *
     * @since Version 1.0
     *
     * @see Runnable
     * @see Runnable#run()
     */
    @Override
    public void run() {
        isOn = true;

        try {
            while (isOn) {
                String line = inStream.readUTF();
                outTextArea.append(line + "\n");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            inTextField.setVisible(false);
            validate();
        }
    }

    /** Входная точка программы. Создает соединение с сервером, создаёт окно приложения. Запускает приложение.
     *
     * @param args Аргументы командной строки. В программе не используются.
     *
     * @throws IOException Исключение возникает в случае отсутствия соединения с сервером.
     *
     * @since Version 1.0
     *
     * @see IOException
     * @see Socket
     * @see Properties
     * @see DataInputStream
     * @see DataOutputStream
     */
    public static void main(String[] args) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream("connection.properties"));
        String host = properties.getProperty("host");
        String port = properties.getProperty("port");

        Socket socket = new Socket(host, Integer.parseInt(port));
        DataInputStream dis;
        DataOutputStream dos = null;

        try {
            dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            new ChatClient("Chat" + host + ":" + port, socket, dis, dos);
        }
        catch (IOException e) {
            e.printStackTrace();
            try {
                //noinspection ConstantConditions
                if (dos != null)
                    dos.close();
            }
            catch (IOException e1) {
                e1.printStackTrace();
            }
            try {
                socket.close();
            }
            catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }
}
