import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

@SuppressWarnings("WeakerAccess")
public class ChatServer {

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

    public static void main(String[] args) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream("connection.properties"));
        String port = properties.getProperty("port");
        System.out.println("Server started");
        new ChatServer(Integer.parseInt(port));
    }
}