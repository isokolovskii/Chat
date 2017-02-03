import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.Properties;

/** Класс клиентского приложения. Наследуется от {@link JFrame} и реализует интерфейс {@link Runnable} для работы с
 * многопоточностью
 *
 * @author Ivan Sokolovskiy
 *
 * @version 1.1
 *
 * @since Version 1.0
 *
 * @see JFrame
 * @see Runnable
 */

class ChatClient extends JFrame implements Runnable {

    /**
     * @see JTextArea
     * @see JTextField
     */
    private Socket socket;
    private DataInputStream inStream;
    private DataOutputStream outStream;
    private final JTextArea outTextArea;
    private final JTextField inTextField;
    private boolean isOn;
    private volatile boolean isConnected = false;

    private Properties properties = new Properties();

    /**
     * Конструктор класса. Получаем на вход название окна, сокет соединения, входной и выходной поток данных.
     *
     * @since Version 1.0
     *
     * @see Socket
     * @see JFrame
     * @see DataInputStream
     * @see DataOutputStream
     */
    private ChatClient() {
        super("Chat");

        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());
        JScrollPane scrollPane;
        cp.add(BorderLayout.CENTER, scrollPane = new JScrollPane(outTextArea = new JTextArea()));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
//        cp.add(BorderLayout.CENTER, outTextArea = new JTextArea());
        outTextArea.setEnabled(false);
        outTextArea.setDisabledTextColor(Color.BLACK);
        cp.add(BorderLayout.SOUTH, inTextField = new JTextField());

        inTextField.addActionListener(e -> {
            try {
                outStream.writeUTF(properties.getProperty("username")+ "#" + inTextField.getText() + "#" + new
                        Date().toString());
                outStream.flush();
            } catch (IOException e1) {
                e1.printStackTrace();
                isOn = false;
            }
            inTextField.setText("");
        });
        inTextField.setEnabled(false);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                super.windowOpened(e);
                File propFile = new File("settings.properties");
                if (!propFile.exists()) {
                    try {
                        if (!propFile.createNewFile()) {
                            System.err.println("Error loading settings file");
                        } else {
                            properties.setProperty("username", "username");
                            properties.setProperty("host", "localhost");
                            properties.setProperty("port", "8082");
                            properties.store(new FileOutputStream(propFile), "Application properties");
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                else try {
                    properties.load(new FileInputStream(propFile));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

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

            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                File propFile = new File("settings.properties");
                if (propFile.exists())
                    if (!propFile.delete())
                        System.err.println("Error while saving settings");
                try {
                    if (propFile.createNewFile()) {
                        properties.store(new FileOutputStream(propFile), "Application properties");
                    }
                } catch (IOException e1) {
                    System.err.println("Error while saving settings");
                    e1.printStackTrace();
                }
            }
        });

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Menu");

        menu.add(new JMenuItem("Connect"));
        menu.getItem(0).addActionListener(e -> {
            String host = properties.getProperty("host");
            String port = properties.getProperty("port");
            try {
                socket = new Socket(host, Integer.parseInt(port));
                try {
                    inStream = new DataInputStream(socket.getInputStream());
                    outStream = new DataOutputStream(socket.getOutputStream());
                    super.setTitle("Chat " + host + ":" + port);
                    inTextField.setEnabled(true);
                    isConnected = true;
                } catch (IOException e1) {
                    System.err.println("Error getting streams from server");
                    try {
                        outStream.close();
                    } catch (IOException e2) {
                        System.err.println("Error closing output stream");
                        e2.printStackTrace();
                    }
                    try {
                        socket.close();
                    } catch (IOException e2) {
                        System.err.println("Error closing socket");
                        e2.printStackTrace();
                    }
                }
            } catch (IOException e1) {
                System.err.println("Error connecting to server");
                e1.printStackTrace();
            }
        });

        menu.add(new JMenuItem("Disconnect"));
        menu.getItem(1).addActionListener(e -> {
            try {
                outStream.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            outStream = null;
            socket = null;
            inTextField.setEnabled(false);
            isConnected = false;
            this.setTitle("Chat");
        });

        menu.add(new JMenuItem("Settings"));
        menu.getItem(2).addActionListener(e -> new ClientSettings(this, this.getTitle() + " Settings", properties, isConnected));

        menuBar.add(menu);
        setJMenuBar(menuBar);


        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(600, 600);
        setVisible(true);
        inTextField.requestFocus();
        (new Thread(this)).start();
    }

    /**
     * Устанавливает переданный набор свойств как основной набор данного приложения
     * @param properties свойства приложения
     */
    void setProperties(Properties properties) {
        String oldName;
        String newName;
        if (!(oldName = this.properties.getProperty("username")).equals(newName = properties.getProperty("username"))
                &&
                isConnected)
            try {
                outStream.writeUTF(oldName + " changed his name into " + newName);
            } catch (IOException e) {
                System.err.println("Error sending new name to server");
                e.printStackTrace();
            }
        this.properties = properties;
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
                if (isConnected) {
                    String line = inStream.readUTF();
                    System.out.println(line);
                    String[] data = line.split("#");
                    if (data.length == 1) outTextArea.append(line + "\n");
                    else {
                        outTextArea.append(data[0] + "[" + data[data.length - 1] + "]: ");
                        for (int i = 1; i < data.length - 1; i++) outTextArea.append(data[i]);
                        outTextArea.append("\n");
                    }
                }
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
        new ChatClient();
    }
}


