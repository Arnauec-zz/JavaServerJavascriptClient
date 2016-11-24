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
    
    // ConcurrentHashMap per guardar les connexions i el seu iterador.
    private ConcurrentHashMap<WebSocket, String> connexions = new ConcurrentHashMap<>();
    private Iterator<String> iterator;
    
    // Inicialitza un servidor en el port especificat.
    public ChatServer(int port) throws UnknownHostException {
        super(new InetSocketAddress(port));
    }

    public ChatServer(InetSocketAddress address) {
        super(address);
    }
    
    
    public static void main(String[] args) throws InterruptedException, IOException {

        int port = 50000; // 843 flash policy port
        
        // Inicialitzem el servidor
        ChatServer s = new ChatServer(port);
        s.start();
        
        System.out.println("Hem iniciat el servidor en el port: " + s.getPort());
    }
    
    
    // Envia un missatge a tots els clients
    public void sendToAll(String text) {
        Collection<WebSocket> con = connections();
        synchronized (con) {
            for (WebSocket c : con) {
                c.send(text);
            }
        }
    }
    
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        
        // Quan s'obre una connexió iterem el HashMap i enviem a tots els usuaris existents
        // que tenim un nou usuari connectat amb nom d'usuari xxxx
        
        iterator = connexions.values().iterator();
        while (iterator.hasNext()) {
            conn.send("/newuser " + iterator.next());
        }
    }

    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        
        // Quan es tanca una connexió, esborrem la connexió del HashMap i enviem
        // un missatge a tots els usuaris connectats amb la comana /deleteuser xxxx
        // per tal de que l'esborrin del seu JList i també els enviem un missatge
        // normal informant-los que l'usuari h deixat la sala.
        
        String str = connexions.get(conn);
        connexions.remove(conn);
        this.sendToAll("/deleteuser " + str);
        this.sendToAll("En/La " + str + " ha sortit de la sala!");
        System.out.println("En/La " + str + " ha sortit de la sala!");
    }

    public void onMessage(WebSocket conn, String message) {
        
        // Quan rebem un missatge nou, si el missatge conté /newuser, és a dir,
        // és una comana per afegir un usuari al HashMap, l'afegim, si no, 
        // rebotem el missatge a tots els usuaris.
        
        if (message.contains("/newuser ")) {
            String[] str = message.split(" ");
            connexions.put(conn, str[1]);
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
