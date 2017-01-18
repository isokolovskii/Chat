package com.isokolovskiy.chat;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class ChatHandler extends Thread {

    protected Socket socket;
    protected DataInputStream inStream;
    protected DataOutputStream outStream;
    protected boolean isOn;

    protected static List<ChatHandler> handlers = Collections.synchronizedList(new ArrayList<ChatHandler>());

    public ChatHandler(Socket s) throws IOException {
        socket = s;
        inStream = new DataInputStream(new BufferedInputStream(s.getInputStream()));
        outStream = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
    }

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
            e.printStackTrace();
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
