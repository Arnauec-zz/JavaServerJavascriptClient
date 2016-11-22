/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 * java -cp ~/NetBeansProjects/Chat/build/classes/ chat.ChatServerOld 50000
 */
package chatweb;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Epicur
 */
public class ChatServerOld {

    private MySocket clientSocket;
    private Iterator<MySocket> keyIterator;
    private ConcurrentHashMap<MySocket, String> connections;
    private MyServerSocket serverSocket;

    public ChatServerOld(String port) {
        int portNumber = Integer.parseInt(port);

        connections = new ConcurrentHashMap<>();
        serverSocket = new MyServerSocket(portNumber);

        while (true) {

            clientSocket = serverSocket.accept();
            String nick = clientSocket.read_str();
            connections.put(clientSocket, nick);

            System.out.println("Hem afegit un nou socket a la colecció, nom d'usuari: " + nick);

            addAllUsers(clientSocket);
            addUser(nick, clientSocket);

            System.out.println("Tenim una nova connexió a " + clientSocket.socket.getInetAddress() + " en el port " + clientSocket.socket.getPort());

            new Thread() {
                public void run() {

                    String inputLine;
                    
                    // Input Thread
                    while ((inputLine = clientSocket.read_str()) != null) {
                        // L'iterator ha de començar des del principi cada cop que que es comença el while
                        keyIterator = connections.keySet().iterator();
                        while (keyIterator.hasNext()) {
                            keyIterator.next().write_str(inputLine);
                        }
                    }
                    System.out.println("S'ha tancata una connexió");

                    removeUser(clientSocket);
                }
            }.start();
        }
    }

    public void addAllUsers(MySocket sock) {
        Iterator iterator = connections.values().iterator();
        while (iterator.hasNext()) {
            sock.write_str("/newuser " + iterator.next());
        }
    }

    public void addUser(String str, MySocket sock) {
        MySocket sockActual = null;
        Iterator<MySocket> iterador = connections.keySet().iterator();
        while (iterador.hasNext()) {
            sockActual = iterador.next();
            if (sockActual != sock) {
                sockActual.write_str("/newuser " + str);
            }
        }
    }

    public void removeUser(MySocket sock) {
        Iterator<MySocket> iteratorMySocket = connections.keySet().iterator();
        while (iteratorMySocket.hasNext()) {
            iteratorMySocket.next().write_str("/deleteuser " + connections.get(sock));
        }
           }

    public static void main(String[] args) {
        System.out.println("S'ha iniciat el servidor al port " + args[0]);
        new ChatServerOld(args[0]);
    }

}
