/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatweb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

/**
 *
 * @author Epicur
 */

public class ChatServer extends WebSocketServer {
    
    // ConcurrentHashMap to save the connections.
    private ConcurrentHashMap<WebSocket, String> connections = new ConcurrentHashMap<>();
    private Iterator<String> iterator;
    // Initialization of the server
    public ChatServer(int port) throws UnknownHostException {
        super(new InetSocketAddress(port));
    }
    public ChatServer(InetSocketAddress address) {
        super(address);
    }
    public static void main(String[] args) throws InterruptedException, IOException {
        int port = 50000; 
        ChatServer s = new ChatServer(port);
        s.start();
        System.out.println("The server has been started on port: " + s.getPort());
    }
    // Sends a message to all the clients
    public void sendToAll(String text) {
        Collection<WebSocket> con = connections();
        synchronized (con) {
            for (WebSocket c : con) {
                c.send(text);
            }
        }
    }
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        // Actions to perform whenever a connection is opened.
        System.out.println("Tenim una nova connexió");
        iterator = connections.values().iterator();
        while (iterator.hasNext()) {
            conn.send("/newuser " + iterator.next());
        }
    }

    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        // Actions to perform whenever a connection is closed
        String str = connections.get(conn);
        connections.remove(conn);
        this.sendToAll("/deleteuser " + str);
        this.sendToAll(str + " has left the room!");
        System.out.println(str + " has left the room!");
    }

    public void onMessage(WebSocket conn, String message) {
        // Actions to perform whenever a message is received
        if (message.contains("/newuser ")) {
            String[] str = message.split(" ");
            connections.put(conn, str[1]);
            this.sendToAll(message);
        } else {
            this.sendToAll(message);
        }
        System.out.println(message);
    }

    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
        if (conn != null) {
            // some errors like port binding failed may not be assignable to a specific websocket
        }
    }
}
