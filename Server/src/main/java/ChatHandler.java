import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Incoming connection handler. Extends {@link Thread} class.
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
    private final DataOutputStream outStream;
    private boolean isOn;
    private String username;

    private static final List<ChatHandler> handlers = Collections.synchronizedList(new ArrayList<ChatHandler>());

    /**
     * ChatHandler constructor. Gets socket, opened in {@link ChatServer}.
     * Creates data stream for input and output:
     * {@link ChatHandler#inStream} and {@link ChatHandler#outStream}.
     *
     * @param s Socket of a connection
     *
     * @throws IOException Exception thrown while getting streams from opened socket.
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
     * Starts the connection handling process.
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
     * Sends message of one user to all others connected
     *
     * @param message Message got from user
     *
     * @since Version 1.0
     *
     * @see ChatServer
     */
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
