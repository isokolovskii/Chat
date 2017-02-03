import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * GUI интерфейс серверной программы.
 *
 * @author Ivan Sokolovskiy
 *
 * @since Version 1.1
 *
 * @version 1.1
 *
 * @see ChatServer
 * @see JFrame
 */
class ServerWindow extends JFrame {
    private final JButton startButton;
    private JButton stopButton;

    private final JLabel ipLabel;
    private JTextField portField;

    private final Properties properties = new Properties();
    private ChatServer server;

    /**
     * @throws HeadlessException Исключение, вызов которого идет от родителя
     */
    private ServerWindow() throws HeadlessException {
        super("Chat server");
        addWindowListener(new WindowAdapter() {
            /**
             * Invoked when a window has been opened.
             *
             * @param e Событие окна
             */
            @Override
            public void windowOpened(WindowEvent e) {
                super.windowOpened(e);
                File propFile = new File("config.properties");
                if (!propFile.exists()) {
                    try {
                        if (!propFile.createNewFile()) {
                            System.err.println("Error loading config file");
                        } else {
                            properties.setProperty("port", "8082");
                            properties.store(new FileOutputStream(propFile), "Server configurations");
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
                portField.setText(properties.getProperty("port"));
            }

            /**
             * Invoked when a window is in the process of being closed.
             * The close operation can be overridden at this point.
             *
             * @param e событие окна
             */
            @Override
            public void windowClosing(WindowEvent e) {
                properties.setProperty("port", portField.getText());
                File propFile = new File("config.properties");
                if (propFile.exists())
                    if (!propFile.delete())
                        System.err.println("Error while saving config");
                try {
                    if (propFile.createNewFile()) {
                        properties.store(new FileOutputStream(propFile), "Server configurations");
                    }
                } catch (IOException e1) {
                    System.err.println("Error while saving config");
                    e1.printStackTrace();
                }
            }
        });

        ipLabel = new JLabel("");
        JLabel portLabel = new JLabel("Port: ");
        portField = new JTextField();

        startButton = new JButton("Start server");
        startButton.addActionListener(e -> {
            stopButton.setEnabled(true);
            startButton.setEnabled(false);
            portField.setEnabled(false);

            try {
                if (server.isAlive()) server.interrupt();
            } catch (NullPointerException e1) {
               // e1.printStackTrace();
            }
            server = new ChatServer(Integer.parseInt(portField.getText()));
            ipLabel.setText("Server ip: " + server.getAddress());
            System.out.println("Server started");
        });

        stopButton = new JButton("Stop server");
        stopButton.addActionListener(e -> {
            stopButton.setEnabled(false);
            startButton.setEnabled(true);
            portField.setEnabled(true);

            try {
                server.stopServer();
            } catch (NullPointerException e1) {
                e1.printStackTrace();
            }
            server = null;
            System.out.println("Server stopped");
        });
        stopButton.setEnabled(false);

        getContentPane().setLayout(new GridLayout(3, 2));

        add(ipLabel);
        add(new JLabel(""));
        add(portLabel);
        add(portField);
        add(startButton);
        add(stopButton);

        setSize(500, 200);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
    }

    /**
     * Входная точка работы серверной программы.
     *
     * @param args Консольные параметры, не используются
     */
    public static void main(String[] args) {
        new ServerWindow();
    }
}
