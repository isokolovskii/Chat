import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Properties;

/**
 * Created by Ivan on 31.01.17, 10:11.
 * Created in package PACKAGE_NAME
 * Project Chat
 */
class ClientSettings extends JFrame {

    private final JTextField userNameField;
    private final JTextField ipField;
    private final JTextField portField;
    private Properties localProperties;

    ClientSettings(JFrame parentFrame, String title, Properties properties, boolean isConnected) throws
            HeadlessException {
        super(title);
        setLayout(new GridLayout(4, 2));

        JButton okButton = new JButton("Ok");
        JButton cancelButton = new JButton("Cancel");

        JLabel userNameLabel = new JLabel("Username: ");
        JLabel ipLabel = new JLabel("Chat server IP: ");
        JLabel portLabel = new JLabel("Chat server port: ");

        userNameField = new JTextField(properties.getProperty("username"));
        ipField = new JTextField(properties.getProperty("host"));
        portField = new JTextField(properties.getProperty("port"));

        if (isConnected) {
            ipField.setEnabled(false);
            portField.setEnabled(false);
        }

        parentFrame.setEnabled(false);

        okButton.addActionListener(e -> {
            localProperties = new Properties();
            localProperties.setProperty("username", userNameField.getText());
            localProperties.setProperty("host", ipField.getText());
            localProperties.setProperty("port", portField.getText());
            this.dispose();
        });

        cancelButton.addActionListener(e -> {
            localProperties = properties;
            this.dispose();
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                ((ChatClient)parentFrame).setProperties(localProperties);
                parentFrame.setEnabled(true);
            }
        });

        add(userNameLabel);
        add(userNameField);
        add(ipLabel);
        add(ipField);
        add(portLabel);
        add(portField);
        add(okButton);
        add(cancelButton);

        setSize(400, 200);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setVisible(true);
    }
}
